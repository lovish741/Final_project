package com.lovish.areeba.gossipbox;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.widget.Toast.LENGTH_LONG;
import static com.lovish.areeba.gossipbox.R.drawable.sender_message_background;

public class adapter_for_message extends RecyclerView.Adapter<adapter_for_message.MessageViewHolder> {

    private List<message> mMessageList;
    private DatabaseReference mUserDatabase;
    private Context context;
    private DatabaseReference database;
    private String chatUserId;
    private List<String> keyList;


    public adapter_for_message(List<message> mMessageList, chat_page c,String chatUserId,List<String> keyList) {

        this.mMessageList = mMessageList;
        this.context = c;

        this.chatUserId=chatUserId;
        this.keyList=keyList;

    }



//    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_message_layout ,parent, false);



        return new MessageViewHolder(v);

    }

 /*   @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }*/


    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView timeText;
        public TextView timeImage;
        public CircleImageView profileImage;
        public ImageView shared_image;
        public Button download_button;
        View v;

        public MessageViewHolder(View view) {
            super(view);

            v=view;
            messageText = (TextView) view.findViewById(R.id.message_text);
            timeText=(TextView) view.findViewById(R.id.message_time);
            profileImage = (CircleImageView) view.findViewById(R.id.message_pic);
            shared_image=(ImageView)view.findViewById(R.id.shared_image);
            download_button=(Button)view.findViewById(R.id.download);
            timeImage = (TextView) view.findViewById(R.id.image_time);
            //messageImage = (ImageView) view.findViewById(R.id.message_image_layout);

        }
    }

  //  @Override
    @SuppressLint("ResourceAsColor")
    public void onBindViewHolder(final MessageViewHolder viewHolder, final int i) {

        String currentuser=FirebaseAuth.getInstance().getCurrentUser().getUid();

        message c = mMessageList.get(i);
        final String from = c.getFrom();
        String message_type=c.getType();

        final String key=keyList.get(i);
        if(from.equals(currentuser)){
            viewHolder.messageText.setBackgroundColor(Color.WHITE);
            viewHolder.messageText.setTextColor(Color.BLACK);
        }
        /*else {
            viewHolder.messageText.setBackgroundColor(0xFF008000);
            viewHolder.messageText.setTextColor(Color.WHITE);
        }*/
        //String message_type = c.getType();


        //deletion of message
        viewHolder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText("lov", chat_page,Toast.LENGTH_LONG).show();
                //Toast.makeText(adapter_for_message.this,"hi",LENGTH_LONG).show();
                //Toast.makeText(context,"delete",LENGTH_LONG).show();
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Want to delete message")
                        .setTitle("confirm Delete");

                database= FirebaseDatabase.getInstance().getReference();
                //Toast.makeText(context,"delete",LENGTH_LONG).show();
                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        //delete message
                        database.child("messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(chatUserId).child(key).removeValue();
//                        mMessageList.remove(i);
//                        keyList.remove(i);
                    }
                });

                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Toast.makeText(context,"not delete",LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });
                //AlertDialog dialog = builder.create();

                builder.show();
            }
        });

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                //viewHolder.displayName.setText(name);

                Picasso.get().load(image)
                        .placeholder(R.drawable.acc).into(viewHolder.profileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM ,yyyy");
            String date=formatter.format(c.getTime());

            DateFormat df = new SimpleDateFormat("hh:mm a");
            String time = df.format(c.getTime());


            //viewHolder.messageText.setText(c.getMessage());
            viewHolder.timeText.setText(time+"  "+date);
            viewHolder.timeImage.setText(time+"  "+date);


            if(message_type.equals("text"))
            {
                viewHolder.messageText.setText(c.getMessage());
                viewHolder.shared_image.setVisibility(View.INVISIBLE);
                viewHolder.timeImage.setVisibility(View.INVISIBLE);
                viewHolder.download_button.setVisibility(View.INVISIBLE);
            }
            else
            {



                final File rootPath = new File(Environment.getExternalStorageDirectory(), "GossipBox");
                if(!rootPath.exists()) {
                    rootPath.mkdirs();
                }
                final File f = new File(rootPath,keyList.get(i)+".jpg");

                Bitmap b = null;
                try {
                    b = BitmapFactory.decodeStream(new FileInputStream(f.getAbsolutePath()));
                    viewHolder.messageText.setVisibility(View.INVISIBLE);
                    viewHolder.download_button.setVisibility(View.INVISIBLE);
                    viewHolder.shared_image.setImageBitmap(b);
                    viewHolder.shared_image.getLayoutParams().height=250;
                    viewHolder.timeText.setVisibility(View.INVISIBLE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //download button
                    viewHolder.messageText.setText(keyList.get(i));
                    viewHolder.shared_image.setVisibility(View.INVISIBLE);
                    viewHolder.timeImage.setVisibility(View.INVISIBLE);
                    viewHolder.download_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference filepath=storageRef.child("message_images").child(keyList.get(i)+".jpg");

                                final File localFile=new File(rootPath,keyList.get(i)+".jpg");

                                filepath.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        // Local temp file has been created
                                        Toast.makeText(context,"Downloaded",LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                        Toast.makeText(context,localFile.toString(),LENGTH_LONG).show();
                                    }
                                });
                            }




                    });
                }



            }



    }




//    @Override
    public int getItemCount() {


        return mMessageList.size();
    }

}
