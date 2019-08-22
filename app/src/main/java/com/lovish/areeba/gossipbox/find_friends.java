package com.lovish.areeba.gossipbox;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.lovish.areeba.gossipbox.R.id.user_layout;

public class find_friends extends AppCompatActivity {

    private TextInputEditText friend_name;
    private RecyclerView users;

    private Toolbar friend_toolbar;

    private DatabaseReference mUsersDatabase;

    private Button search;
    private FirebaseRecyclerAdapter<user, Userholder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);


        friend_name=(TextInputEditText)findViewById(R.id.find_friend_textbox);

        friend_toolbar=(Toolbar)findViewById(R.id.user_appbar);
        setSupportActionBar(friend_toolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        friend_name=(TextInputEditText)findViewById(R.id.find_friend_textbox);


        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        search=(Button)findViewById(R.id.search_button);
        users=(RecyclerView)findViewById(R.id.user_list);

        users.setHasFixedSize(true);
        users.setLayoutManager(new LinearLayoutManager(this));


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference database= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        if(currentUser!=null)
        {
           // Toast.makeText(find_friends.this,"online",Toast.LENGTH_LONG).show();
            database.child("online").setValue(true);

        }
    }

    ///on back button call prent activity




    //retrieve in real time
    @Override
    protected void onStart() {
        super.onStart();


        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference database= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());


        final RelativeLayout ll=(RelativeLayout)findViewById(R.id.user_layout);


        //if(!TextUtils.isEmpty(friend_name.getText().toString())) {
           // Toast.makeText(find_friends.this,"search button is working",Toast.LENGTH_LONG).show();
            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!TextUtils.isEmpty(friend_name.getText().toString()))
                    {
                        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<user, Userholder>(

                                user.class,
                                R.layout.single_user,
                                Userholder.class,
                                mUsersDatabase.orderByChild("name").startAt(friend_name.getText().toString()).endAt(friend_name.getText().toString() + "\uf8ff")

                        ) {
                            @Override
                            protected void populateViewHolder(Userholder usersViewHolder, user users, int position) {

                                final String user_id = getRef(position).getKey();
                                if(user_id!=currentUser.getUid())
                                {
                                    usersViewHolder.setDisplayName(users.getName());

                                    usersViewHolder.setUserImage(users.getThumb_image(), getApplicationContext());



                                    usersViewHolder.view.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent user_activity = new Intent(find_friends.this, user_activity.class);
                                            user_activity.putExtra("user_id", user_id);
                                            startActivity(user_activity);

                                        }
                                    });

                                }
                                else
                                {
                                    //usersViewHolder.ll.setVisibility(View.GONE);
                                    //RelativeLayout ll=(RelativeLayout)findViewById(R.id.user_layout);
                                    //ll.setVisibility(View.GONE);
                                    //usersViewHolder.
                                    //ll.setVisibility(View.GONE);
                                    usersViewHolder.setIsRecyclable(false);
                                }
                            }
                        };


                        users.setAdapter(firebaseRecyclerAdapter);


                    }
                }
            });

        }

    //}

    public static class Userholder extends RecyclerView.ViewHolder{

        View view;

        public Userholder(@NonNull View itemView) {
            super(itemView);
            //single user whole details
            view=itemView;
        }


        public void setDisplayName(String name){

            TextView userNameView = (TextView) view.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }


        public void setUserImage(String thumb_image, Context ctx){

            CircleImageView userImageView = (CircleImageView) view.findViewById(R.id.user_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.acc).into(userImageView);

        }

    }

}
