package com.sarvesh.faceapp_v7;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    public String HOST = "serveousercontent.com";//serveousercontent.com
    public int Port = 1998;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Navigation Coding Start
        drawerLayout = findViewById(R.id.main_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId())
                {
                    case R.id.nav_register:
                        Intent intent1 = new Intent(MainActivity.this, Register.class);
                        startActivity(intent1);
                        ReadyAppend readyAppend = new ReadyAppend();
                        readyAppend.execute();
                        break;
                    case R.id.nav_permissions:
                        Intent intent2= new Intent(MainActivity.this, Permissions.class);
                        startActivity(intent2);
                        break;
                    case R.id.nav_unknown_activity:
                        Intent intent3 = new Intent(MainActivity.this, Unknown.class);
                        startActivity(intent3);
                        break;
                    case R.id.nav_emergency_toggle:
                        Intent intent4 = new Intent(MainActivity.this, Emergency.class);
                        startActivity(intent4);
                        break;


                }
                return false;
            }
        });
    }


    class ReadyAppend extends AsyncTask<Void, Void, Void>
    {
        Socket s8;
        PrintWriter pw8;

        @Override
        protected Void doInBackground(Void... voids) {
            ReadyAppend();
            return null;
        }


        void ReadyAppend()
        {
            while(s8 == null)
            {
                try {
                    s8 = new Socket(HOST, Port);
                    pw8 = new PrintWriter(s8.getOutputStream());
                    BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(s8.getInputStream()));
                    pw8.write("?OPE");
                    pw8.write("1");
                    pw8.flush();

                    String command = String.valueOf(mBufferIn.readLine());
                    Log.d("uh","Commandi: "+command);

                    //*********Display Toast using threads***************************************//
                    MyAndroidThread myTask = new MyAndroidThread(MainActivity.this,command);
                    Thread t1 = new Thread(myTask, "Sarvesh");
                    t1.start();

                    try {
                        Thread.sleep(1);
                        t1.interrupt();
                        Thread.sleep(5);
                    }
                    catch (InterruptedException e) {
                        System.out.println("Caught:" + e);
                    }
                    //****************************************************************************//

                    pw8.close();
                    s8.close();
                } catch (IOException e) {
                    System.out.println("Fail");
                    e.printStackTrace();
                }
            }
        }
    }

    class MyAndroidThread implements Runnable
    {
        AppCompatActivity activity;
        String command;
        public MyAndroidThread(AppCompatActivity activity, String Command)
        {
            this.activity = activity;
            command = Command;
        }

        @Override
        public void run()
        {

            //perform heavy task here and finally update the UI with result this way -
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(getApplicationContext(),command,Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    class ReadyDelete extends AsyncTask<Void, Void, Void>
    {
        Socket s9;
        PrintWriter pw9;

        @Override
        protected Void doInBackground(Void... voids) {
            ReadyDelete();
            return null;
        }
        void ReadyDelete()
        {
            while(s9 == null)
            {
                try {
                    s9 = new Socket(HOST, Port);
                    pw9 = new PrintWriter(s9.getOutputStream());
                    BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(s9.getInputStream()));

                    pw9.write("2");
                    pw9.flush();

                    String ACK = mBufferIn.readLine();
                    if(ACK.equals("?ACK"))
                    {
                        displayLongToast(String.valueOf(mBufferIn.readLine()));
                    }
                    pw9.close();
                    pw9.flush();
                    s9.close();
                } catch (IOException e) {
                    System.out.println("Fail");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayToast(String ToastMessage)
    {
        Toast.makeText(MainActivity.this,ToastMessage,Toast.LENGTH_SHORT).show();
    }
    private void displayLongToast(String ToastMessage)
    {
        Toast.makeText(getApplicationContext(),ToastMessage,Toast.LENGTH_LONG).show();
    }

}
