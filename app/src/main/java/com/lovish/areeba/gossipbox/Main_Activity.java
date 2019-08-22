package com.lovish.areeba.gossipbox;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class Main_Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private RecyclerView mConvList;
    private TextView name;
    private CircleImageView dp;

    private FloatingActionButton find_friend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GossipBox");


        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(isConnected==false)
        {
            Intent wait=new Intent(Main_Activity.this,waiting.class );
            finish();
            startActivity(wait);
        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //floating button working
        find_friend=(FloatingActionButton)findViewById(R.id.fab);
        find_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent friend_activity=new Intent(Main_Activity.this,find_friends.class );
                startActivity(friend_activity);
            }
        });





        View header =navigationView.getHeaderView(0);
        name=(TextView)header.findViewById(R.id.main_user_name);
        dp=(CircleImageView) header.findViewById(R.id.main_profile_pic);

        //message view
        mConvList = (RecyclerView) findViewById(R.id.conv_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //in reverse
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser==null)
        {
            Intent login=new Intent(Main_Activity.this,Login_Page.class );
            startActivity(login);
            finish();
        }
        else
        {
            database= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());


            database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String user=dataSnapshot.child("name").getValue().toString();
                    String pic=dataSnapshot.child("thumb_image").getValue().toString();
                    //Toast.makeText(Main_Activity.this,user,Toast.LENGTH_LONG).show();
                    name.setText(user);
                    if(!pic.equals("default"))
                    {
                        Picasso.get().load(pic).placeholder(R.drawable.acc).into(dp);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mAuth.getCurrentUser().getUid());

            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mAuth.getCurrentUser().getUid());


            FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(
                    Conv.class,
                    R.layout.single_user,
                    ConvViewHolder.class,
                    mConvDatabase.orderByChild("timestamp")
            ) {
                @Override
                protected void populateViewHolder(final ConvViewHolder convViewHolder, final Conv conv, int i) {



                    final String list_user_id = getRef(i).getKey();

                    Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);


                    lastMessageQuery.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            String data = dataSnapshot.child("message").getValue().toString();
                            String type = dataSnapshot.child("type").getValue().toString();
                            String time = dataSnapshot.child("time").getValue().toString();
                            convViewHolder.setMessage(data,type,time);
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
                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                            convViewHolder.setName(userName);
                            convViewHolder.setUserImage(userThumb);
                            convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent chatIntent = new Intent(Main_Activity.this,chat_page.class);
                                    chatIntent.putExtra("user_id", list_user_id);
                                    chatIntent.putExtra("user_name", userName);
                                    startActivity(chatIntent);

                                }
                            });
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            };

            mConvList.setAdapter(firebaseConvAdapter);
        }
    }


    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, String type,String name){

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_message);


            if (type.equals("text")) {
                userStatusView.setText(message);
            }
            else {
                userStatusView.setText(name+".jpg");
            }







        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.acc).into(userImageView);

        }


    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();


        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.profile) {
            Intent profile=new Intent(Main_Activity.this,Profile_page.class);
            startActivity(profile);
        }
        else if (id == R.id.find_friend) {
            Intent find_friend=new Intent(Main_Activity.this,find_friends.class);
            startActivity(find_friend);
        }
        else if (id == R.id.log_out) {

            FirebaseAuth.getInstance().signOut();
            Intent logout=new Intent(Main_Activity.this,Login_Page.class );
            startActivity(logout);
            finish();

        } else if (id == R.id.developer) {

            Intent developer=new Intent(Main_Activity.this, com.lovish.areeba.gossipbox.developer.class);
            startActivity(developer);

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
