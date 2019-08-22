package com.lovish.areeba.gossipbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;


public class Reg_activity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar RegisterPageToolBar;

    private FirebaseAuth mAuth;

    private TextInputEditText username;
    private TextInputEditText email;
    private TextInputEditText password;
    private Button create_button;

    private FirebaseDatabase database;

    private ProgressDialog reg_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_activity);


        RegisterPageToolBar=(android.support.v7.widget.Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(RegisterPageToolBar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth=FirebaseAuth.getInstance();


        username=(TextInputEditText) findViewById(R.id.register_username);
        email=(TextInputEditText)findViewById(R.id.register_email_id);
        password=(TextInputEditText)findViewById(R.id.register_password);

        create_button=(Button)findViewById(R.id.register_create_button);

        create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fire_username=username.getText().toString();
                final String fire_email=email.getText().toString();
                final String fire_password=password.getText().toString();

                if(TextUtils.isEmpty(fire_username))
                {
                    Toast.makeText(Reg_activity.this,"Enter username",Toast.LENGTH_LONG).show();
                }

                else if(TextUtils.isEmpty(fire_email))
                {
                    Toast.makeText(Reg_activity.this,"Enter email id",Toast.LENGTH_LONG).show();
                }

                else if(TextUtils.isEmpty(fire_password))
                {
                    Toast.makeText(Reg_activity.this,"Enter password",Toast.LENGTH_LONG).show();
                }

                else if(!TextUtils.isEmpty(fire_username)||!TextUtils.isEmpty(fire_email)||!TextUtils.isEmpty(fire_password))
                {

                    reg_progress=new ProgressDialog(Reg_activity.this);
                    reg_progress.setTitle("Creating");
                    reg_progress.setMessage("Please wait Account is Creating...");
                    reg_progress.setCanceledOnTouchOutside(false);
                    reg_progress.show();

                    mAuth.createUserWithEmailAndPassword(fire_email,fire_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                                String User_id=user.getUid();

                                database=FirebaseDatabase.getInstance();
                                DatabaseReference mref=database.getReference().child("Users").child(User_id);

                                HashMap<String,String> user_data=new HashMap<>();
                                user_data.put("name",fire_username);
                                user_data.put("image","default");
                                user_data.put("thumb_image","default");

                                mref.setValue(user_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            reg_progress.dismiss();
                                            Intent main_activity=new Intent(Reg_activity.this,Main_Activity.class);
                                            startActivity(main_activity);
                                            finish();
                                        }
                                    }
                                });
                                /**/
                            }
                            else
                            {
                                //error

                                reg_progress.dismiss();
                                if(task.getException() instanceof FirebaseAuthUserCollisionException)
                                {
                                    Toast.makeText(Reg_activity.this,"Account Already Exist",Toast.LENGTH_LONG).show();
                                }

                                else if (task.getException() instanceof FirebaseAuthWeakPasswordException)
                                {
                                    Toast.makeText(Reg_activity.this,"Password must contain 6 character",Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }

            }
        });
    }



}