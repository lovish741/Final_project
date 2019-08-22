package com.lovish.areeba.gossipbox;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GossipBox extends Application {


    private DatabaseReference database;
    private FirebaseAuth current_user;

    @Override
    public void onCreate() {
        super.onCreate();

        current_user=FirebaseAuth.getInstance();

        if(current_user.getCurrentUser()!=null)
        {
            database= FirebaseDatabase.getInstance().getReference().child("Users").child(current_user.getCurrentUser().getUid());


            database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot!=null)
                    {
                        //on diconnect make status offine
                        database.child("online").onDisconnect().setValue(false);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
}
