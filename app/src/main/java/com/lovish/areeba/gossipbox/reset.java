package com.lovish.areeba.gossipbox;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
public class reset extends AppCompatActivity {
    private Button reset;
    private TextInputEditText email;
    private Toolbar reset_toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        reset_toolbar=(Toolbar)findViewById(R.id.user_appbar);
        setSupportActionBar(reset_toolbar);
        getSupportActionBar().setTitle("Reset Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        email=(TextInputEditText)findViewById(R.id.email_id);
        reset=(Button)findViewById(R.id.reset_button);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input_email=email.getText().toString();
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.sendPasswordResetEmail(input_email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(reset.this,"Email Sent",Toast.LENGTH_LONG).show();
                            Intent login=new Intent(reset.this,Login_Page.class );
                            finish();
                            startActivity(login);
                        }
                    }
                });
            }
        });
    }
}