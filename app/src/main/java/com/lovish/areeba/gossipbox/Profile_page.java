package com.lovish.areeba.gossipbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class Profile_page extends AppCompatActivity {


    private FirebaseUser current_user;
    private DatabaseReference database;

    private Toolbar profile_toolbar;
    private CircleImageView image;
    private TextView name;
    private Button change_dp;
    private static final int no_of_image=1;

    //firebase storage
    private StorageReference store_profile_pic;


    private ProgressDialog pic_upload_progess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        profile_toolbar=(Toolbar)findViewById(R.id.user_appbar);
        setSupportActionBar(profile_toolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        current_user= FirebaseAuth.getInstance().getCurrentUser();


        String user_id=current_user.getUid();

        database= FirebaseDatabase.getInstance().getReference().child("Users").child(current_user.getUid());



        store_profile_pic= FirebaseStorage.getInstance().getReference();



        image=(CircleImageView)findViewById(R.id.setting_profile_pic);
        name=(TextView)findViewById(R.id.setting_display_name);
        change_dp=(Button)findViewById(R.id.setting_change_dp);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {




                String user=dataSnapshot.child("name").getValue().toString();
                String dp=dataSnapshot.child("image").getValue().toString();
                String thumbnail=dataSnapshot.child("thumb_image").getValue().toString();


                name.setText(user);

                if(!dp.equals("default"))
                {
                    Picasso.get().load(dp).placeholder(R.drawable.acc).into(image);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //dp change
        change_dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery=new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery, "Select Photo"),no_of_image);




            }
        });




    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==no_of_image &&  resultCode==RESULT_OK)
        {

            pic_upload_progess=new ProgressDialog(Profile_page.this);
            pic_upload_progess.setTitle("Uploading Image");
            pic_upload_progess.setMessage("Please wait while Uploading");
            pic_upload_progess.setCanceledOnTouchOutside(false);
            pic_upload_progess.show();


            Uri uri=data.getData();


            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(uri)
                    .setAspectRatio(  1,1)
                    .setMinCropResultSize(500,500)
                    .start(Profile_page.this);



        }


        //check image come from crop activity ony
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();


                //File thumb_filepath=new File(resultUri.getPath());
                final File thumb_filepath=new File(resultUri.getPath());
                String user_id=current_user.getUid();

                Bitmap thumb = null;
                try {
                    thumb = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_data = baos.toByteArray();


                StorageReference file=store_profile_pic.child("profile_pics").child(user_id+".jpg");
                final StorageReference file_bitmap=store_profile_pic.child("thumb_pics").child(user_id+".jpg");


                file.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            final String downloadUrl=task.getResult().getDownloadUrl().toString();

                            //UploadTask upload=file_bitmap.putBytes(thumb);
                            UploadTask upload=file_bitmap.putBytes(thumb_data);

                            upload.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downloadUrl=thumb_task.getResult().getDownloadUrl().toString();
                                    if(thumb_task.isSuccessful())
                                    {
                                        Map image_data=new HashMap<>();
                                        image_data.put("image",downloadUrl);
                                        image_data.put("thumb_image",thumb_downloadUrl);

                                        database.updateChildren(image_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    pic_upload_progess.dismiss();
                                                    Toast.makeText(Profile_page.this,"Uploaded Successfully",Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        pic_upload_progess.dismiss();
                                        Toast.makeText(Profile_page.this,"Error Uploading",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });





                        }
                        else
                        {

                        }
                    }
                });

            }

            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();

            }
        }

    }
}
