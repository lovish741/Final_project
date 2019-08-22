package com.lovish.areeba.gossipbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class Login_Page extends AppCompatActivity {

    private TextInputEditText email;
    private TextInputEditText password;
    private Button login_button;
    private Button login_reset;


    private Button reg_button;


    private FirebaseAuth mAuth;

    private ProgressDialog login_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login__page);






        mAuth=FirebaseAuth.getInstance();


        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(isConnected==false)
        {
            Intent wait=new Intent(Login_Page.this,waiting.class );
            finish();
            startActivity(wait);
        }

        login_button_submit();

        reg_button_submit();

        login_reset=(Button)findViewById(R.id.login_reset);
        login_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reset=new Intent(Login_Page.this,reset.class );

                startActivity(reset);

                /*final AlertDialog.Builder builder = new AlertDialog.Builder(Login_Page.this);
                // Get the layout inflater
                LayoutInflater inflater = Login_Page.this.getLayoutInflater();

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder.setView(inflater.inflate(R.layout.reset_layout, null))
                        // Add action buttons
                        .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                EditText email=(EditText)findViewById(R.id.reset_email);
                                    /*FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(Login_Page.this,"Email Sent",Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });*/

                  /*                  FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString());
                                //Toast.makeText(Login_Page.this,"Email Sent",Toast.LENGTH_LONG).show();

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });


                builder.show();*/

            }
        });

    }

    public void login_button_submit()
    {
        email=(TextInputEditText)findViewById(R.id.login_email_id);
        password=(TextInputEditText)findViewById(R.id.login_password);
        login_button=(Button)findViewById(R.id.login_login_button);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fire_email=email.getText().toString();
                String fire_password=password.getText().toString();

                if(TextUtils.isEmpty(fire_email))
                {
                    Toast.makeText(Login_Page.this,"Please Enter Email Id",Toast.LENGTH_LONG).show();
                }
                else if(TextUtils.isEmpty(fire_password))
                {
                    Toast.makeText(Login_Page.this,"Please Enter Password",Toast.LENGTH_LONG).show();
                }
                else if(!TextUtils.isEmpty(fire_email)||!TextUtils.isEmpty(fire_password))
                {


                    login_progress=new ProgressDialog(Login_Page.this);
                    login_progress.setTitle("Loging In");
                    login_progress.setMessage("Please wait while Matching Credentials");
                    login_progress.setCanceledOnTouchOutside(false);
                    login_progress.show();


                    mAuth.signInWithEmailAndPassword(fire_email,fire_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            login_progress.dismiss();
                            if(task.isSuccessful())
                            {
                                Intent main_activity=new Intent(Login_Page.this,Main_Activity.class);
                                startActivity(main_activity);
                                finish();
                            }
                            else
                            {
                                //error in login
                                if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                                {
                                    Toast.makeText(Login_Page.this,"Wrong password",Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    Toast.makeText(Login_Page.this,"Account doesnot exist",Toast.LENGTH_LONG).show();
                                }
                                password.setText("");
                            }
                        }
                    });
                }


            }
        });
    }

    public void reg_button_submit()
    {
        reg_button = (Button) findViewById(R.id.login_reg_button);

        reg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent= new Intent(Login_Page.this,Reg_activity.class);
                startActivity(reg_intent);
            }
        });

    }

}
