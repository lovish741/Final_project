package com.lovish.areeba.gossipbox;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class user_activity extends AppCompatActivity {


    private ImageView pic;
    private TextView name;
    private Button message_button;
    private DatabaseReference database;

    private String id;
    private String display_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_activity);

        FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();



        id=getIntent().getStringExtra("user_id");

        pic=(ImageView)findViewById(R.id.profile_pic);
        name=(TextView)findViewById(R.id.name);
        message_button=(Button)findViewById(R.id.message);

        database= FirebaseDatabase.getInstance().getReference().child("Users").child(id);


        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                display_name=dataSnapshot.child("name").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                name.setText(display_name);
                Picasso.get().load(image).placeholder(R.drawable.acc).into(pic);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        message_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent chat=new Intent(user_activity.this,chat_page.class);
                chat.putExtra("user_id",id);
                chat.putExtra("user_name",display_name);
                startActivity(chat);

            }
        });


        //user_id.setText(id);

    }
}
