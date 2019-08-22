package com.lovish.areeba.gossipbox;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.widget.Toast.LENGTH_LONG;

public class chat_page extends AppCompatActivity {

    private String chatUserId;
    private String chatUserName;
    private android.support.v7.widget.Toolbar chatToolbar;
    private DatabaseReference database;
    private FirebaseAuth User;
    private String current_user_id;

    private StorageReference mImageStorage;


    private TextView name;
    private CircleImageView pic;


    private EditText current_message;
    private Button send_button;
    private ImageButton gallery_button;
    private final List<message> messageList=new ArrayList<>();
    private final List<String> keylist=new ArrayList<>();
    private LinearLayoutManager layout;
    private RecyclerView all_messages;

    private adapter_for_message message_adapter;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent parent_activity = new Intent(chat_page.this,Main_Activity.class);
        finish();
        //clear all previous activity
        parent_activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(parent_activity);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);

        chatToolbar=(android.support.v7.widget.Toolbar)findViewById(R.id.chat_page_bar);
        setSupportActionBar(chatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);



        chatUserId=getIntent().getStringExtra("user_id");
        chatUserName=getIntent().getStringExtra("user_name");

        User=FirebaseAuth.getInstance();
        current_user_id=User.getCurrentUser().getUid().toString();
        database= FirebaseDatabase.getInstance().getReference();
        getSupportActionBar().setTitle(chatUserName);
        mImageStorage = FirebaseStorage.getInstance().getReference();

        LayoutInflater inflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar=inflater.inflate(R.layout.chat_page_bar,null);

        getSupportActionBar().setCustomView(action_bar);


        name=(TextView)findViewById(R.id.bar_name);
        pic=(CircleImageView)findViewById(R.id.bar_pic);

        current_message=(EditText)findViewById(R.id.chat_message_view);
        send_button=(Button)findViewById(R.id.send_button);
        gallery_button=(ImageButton)findViewById(R.id.send_photo);

        message_adapter=new adapter_for_message(messageList,this,chatUserId,keylist);

        layout=new LinearLayoutManager(this);



        all_messages=(RecyclerView)findViewById(R.id.message_list);

        all_messages.setHasFixedSize(true);
        all_messages.setLayoutManager(layout);

        all_messages.setAdapter(message_adapter);

        //database.child("Chat").child(current_user_id).child(chatUserId).child("seen").setValue(true);





        //app_bar setting loding name and image of friend
        name.setText(chatUserName);
        database.child("Users").child(chatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String image=dataSnapshot.child("thumb_image").getValue().toString();

                Picasso.get().load(image).placeholder(R.drawable.acc).into(pic);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //message send
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message=current_message.getText().toString();
                if(!TextUtils.isEmpty(message))
                {
                    String current_user_ref = "messages/" + current_user_id + "/" + chatUserId;
                    String chat_user_ref = "messages/" + chatUserId + "/" + current_user_id;

                    DatabaseReference user_message_push = database.child("messages")
                            .child(current_user_id).child(chatUserId).push();

                    String push_id = user_message_push.getKey();

                    Map messageMap = new HashMap();
                    messageMap.put("message", message);
                    messageMap.put("seen", false);
                    messageMap.put("type", "text");
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("from", current_user_id);

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                    messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                    current_message.setText("");

                    database.updateChildren(messageUserMap);


          //        database.child("Chat").child(chatUserId).child(current_user_id).child("seen").setValue(false);
                    database.child("Chat").child(chatUserId).child(current_user_id).child("timestamp").setValue(ServerValue.TIMESTAMP);

                    database.child("Chat").child(current_user_id).child(chatUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);


                }

            }
        });


        //send photo
        gallery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery=new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);

                //finish();

                startActivityForResult(Intent.createChooser(gallery,"Select photo"),1);

                /*final int size = messageList.size();
                for (int i=0;i<size;i++)
                {
                    messageList.remove(i);
                    keylist.remove(i);

                }*/

            }
        });


        /*database.child("Chat").child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(chatUserId))
                {
                    Map chatmap=new HashMap();
                    chatmap.put("seen",false);
                    chatmap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + current_user_id + "/" + chatUserId, chatmap);
                    chatUserMap.put("Chat/" + chatUserId + "/" + current_user_id, chatmap);

                    database.updateChildren(chatUserMap);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        final DatabaseReference img=FirebaseDatabase.getInstance().getReference();
        if(requestCode==1 && resultCode==RESULT_OK)
        {
            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + chatUserId + "/" + current_user_id;
            final String chat_user_ref = "messages/" + current_user_id + "/" + chatUserId;

            DatabaseReference user_message_push = img.child("messages")
                    .child(current_user_id).child(chatUserId).push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("message_images").child( push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        //Toast.makeText(chat_page.this,"function called",Toast.LENGTH_LONG).show();

                        String download_url=task.getResult().getDownloadUrl().toString();
                        Map message=new HashMap();
                        message.put("message",download_url);
                        message.put("seen",false);
                        message.put("type","image");
                        message.put("time",ServerValue.TIMESTAMP);
                        message.put("from",current_user_id);


                        Map user=new HashMap();
                        user.put(current_user_ref+"/"+push_id,message);
                        user.put(chat_user_ref+"/"+push_id,message);
                        current_message.setText("");

                        //Toast.makeText(chat_page.this,download_url,Toast.LENGTH_LONG).show();
                        img.updateChildren(user);

                        database.child("Chat").child(chatUserId).child(current_user_id).child("timestamp").setValue(ServerValue.TIMESTAMP);

                        database.child("Chat").child(current_user_id).child(chatUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);



                        //download for current user
                        final File rootPath = new File(Environment.getExternalStorageDirectory(), "GossipBox");
                        if(!rootPath.exists()) {
                            rootPath.mkdirs();
                        }
                        final File localFile=new File(rootPath,push_id+".jpg");
                        mImageStorage.child("message_images").child( push_id + ".jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Local temp file has been created
                                Toast.makeText(chat_page.this,"Dwnloaded",LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                                Toast.makeText(chat_page.this,"Not Dwnloaded",LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });










        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();


        finish();
        startActivity(getIntent());

    }

    @Override
    protected void onStart() {
        super.onStart();


        /*database.child("messages").child(current_user_id).child(chatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
    });*/

        //load messages
        database.child("messages").child(current_user_id).child(chatUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                message msg=dataSnapshot.getValue(message.class);
                messageList.add(msg);
                keylist.add(dataSnapshot.getKey());
                message_adapter.notifyDataSetChanged();

                //scroll to bottom
                all_messages.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                finish();
                startActivity(getIntent());

                //update time stamp of chat with last message time
                Query mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(current_user_id).child(chatUserId).limitToLast(1);
                mMessageDatabase.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        //Long time = dataSnapshot.child("time").getValue();
                        long time= (long) dataSnapshot.child("time").getValue();
                        database.child("Chat").child(current_user_id).child(chatUserId).child("timestamp").setValue(time);
                        //String type = dataSnapshot.child("type").getValue().toString();
                        //convViewHolder.setMessage(data,conv.isSeen());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                //finish();
                //startActivity(getIntent());
                /*adapter_for_message.
                messageList.
                keylist.remove(dataSnapshot.getKey());
                message_adapter.notifyItemRemoved(dataSnapshot);*/
//                message msg=dataSnapshot.getValue(message.class);
//                messageList.remove(msg);
//                keylist.remove(dataSnapshot.getKey());

              //  message_adapter.notify();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
