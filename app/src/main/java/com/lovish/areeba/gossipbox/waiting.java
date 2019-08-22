package com.lovish.areeba.gossipbox;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class waiting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);


        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(waiting.this, "5 sec", Toast.LENGTH_SHORT).show();
                        ConnectivityManager cm =
                                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                        boolean isConnected = activeNetwork != null &&
                                activeNetwork.isConnectedOrConnecting();
                        if(isConnected==false)
                        {
                            //Toast.makeText(waiting.this,"Network not connected",Toast.LENGTH_LONG).show();
                            //finish();
                            //System.exit(0);
                            //  run();
                        }
                        else
                        {
                            Intent conn=new Intent(waiting.this,Main_Activity.class );
                            finish();
                            cancel();
                            startActivity(conn);
                        }
                    }
                });

            }
        }, 0, 100);

    }
}
