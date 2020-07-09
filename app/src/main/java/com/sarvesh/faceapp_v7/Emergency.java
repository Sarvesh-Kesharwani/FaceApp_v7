package com.sarvesh.faceapp_v7;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Emergency extends  AppCompatActivity{

    public String HOST = "serveousercontent.com";//serveousercontent.com192.168.43.205
    public int Port = 1998;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;

    //Refresh to grab data from server
    Button OpenGateButton;
    Button CloseGateButton;
    Button TimedOpenCloseButton;
    ImageView DoorImageView;

    ProgressBar progressBar;
    //intenet
    boolean connected = false;

    //GateActionCode
    int GateActionCode = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressBar = (ProgressBar) findViewById(R.id.emegency_progressBar);

        //Navigation Coding Start
        setContentView(R.layout.activity_emergency);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout = this.findViewById(R.id.permission_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(Emergency.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = findViewById(R.id.emergency_navigation_view);
        //setting navigation_header
        if(Register.UserID == 1)
        {
            View navView = navigationView.inflateHeaderView(R.layout.navigation_header_admin);}

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        //////
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_register:
                        Intent intent1 = new Intent(Emergency.this, Register.class);
                        startActivity(intent1);
                        break;
                    case R.id.nav_permissions:
                        Intent intent2 = new Intent(Emergency.this, Permissions.class);
                        startActivity(intent2);
                        break;
                    case R.id.nav_unknown_activity:
                        Intent intent3 = new Intent(Emergency.this, Unknown.class);
                        startActivity(intent3);
                        break;
                    case R.id.nav_emergency_toggle:
                        Intent intent4 = new Intent(Emergency.this, Emergency.class);
                        startActivity(intent4);
                        break;
                    case R.id.nav_logout:
                        Intent intent5 = new Intent(Emergency.this, MainActivity.class);
                        startActivity(intent5);
                        break;
                }
                return false;
            }
        });
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            {
                connected = true;
            }
        } else {
            connected = false;
        }

        OpenGateButton = findViewById(R.id.open_button);
        CloseGateButton = findViewById(R.id.close_button);
        TimedOpenCloseButton = findViewById(R.id.timed_open_button);

        OpenGateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GateActionCode = 1;
                EmergenyActions emergenyActions = new EmergenyActions();
                emergenyActions.execute(GateActionCode);
            }
        });

        CloseGateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GateActionCode = 2;
                EmergenyActions emergenyActions = new EmergenyActions();
                emergenyActions.execute(GateActionCode);
            }
        });

        TimedOpenCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GateActionCode = 3;
                EmergenyActions emergenyActions = new EmergenyActions();
                emergenyActions.execute(GateActionCode);
            }
        });
}


private class EmergenyActions extends AsyncTask<Integer, Integer, Integer> {
    Socket skt1, skt2, skt3;
    PrintWriter printWriter;
    String PreActionMessage, PostActionMessage;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setMax(100);
        progressBar.setProgress(0);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressBar.setProgress(values[0]);
    }

    @Override
    protected Integer doInBackground(Integer... Params) {
        int gate_action_code = Params[0];
        try {
            if(gate_action_code == 1)
            {OpenGateButton();}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        publishProgress(100);
    }

    void OpenGate() {
        while (skt1 == null) {
            try {
                skt1 = new Socket(HOST, Port);
                printWriter = new PrintWriter(skt1.getOutputStream());
                InputStream sin = skt1.getInputStream();
                BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(skt1.getInputStream()));

                if (!skt1.isConnected()) {
                    displayLongToast("Can't connect to server! Reopen Permission Tab or Restart The App");
                    if (!isCancelled()) {
                        cancel(true);
                    }
                }

                publishProgress(10);
                //sending delimiter
                Log.d("receve", "sending delimiter.");
                printWriter.write("?EMEGNC");
                printWriter.flush();

                printWriter.write("OPEN_GATE");
                printWriter.flush();
                publishProgress(50);

                PreActionMessage = mBufferIn.readLine();
                PostActionMessage = mBufferIn.readLine();
                publishProgress(100);

                printWriter.close();
                mBufferIn.close();
                skt1.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void CloseGate() {
        while (skt1 == null) {
            try {
                skt1 = new Socket(HOST, Port);
                printWriter = new PrintWriter(skt1.getOutputStream());
                InputStream sin = skt1.getInputStream();
                BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(skt1.getInputStream()));

                if (!skt1.isConnected()) {
                    displayLongToast("Can't connect to server! Reopen Permission Tab or Restart The App");
                    if (!isCancelled()) {
                        cancel(true);
                    }
                }

                publishProgress(10);
                //sending delimiter
                Log.d("receve", "sending delimiter.");
                printWriter.write("?EMEGNC");
                printWriter.flush();

                printWriter.write("OPEN_GATE");
                printWriter.flush();
                publishProgress(50);

                PreActionMessage = mBufferIn.readLine();
                PostActionMessage = mBufferIn.readLine();
                publishProgress(100);

                printWriter.close();
                mBufferIn.close();
                skt1.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void TimedOpenGate() {
        while (skt1 == null) {
            try {
                skt1 = new Socket(HOST, Port);
                printWriter = new PrintWriter(skt1.getOutputStream());
                InputStream sin = skt1.getInputStream();
                BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(skt1.getInputStream()));

                if (!skt1.isConnected()) {
                    displayLongToast("Can't connect to server! Reopen Permission Tab or Restart The App");
                    if (!isCancelled()) {
                        cancel(true);
                    }
                }

                publishProgress(10);
                //sending delimiter
                Log.d("receve", "sending delimiter.");
                printWriter.write("?EMEGNC");
                printWriter.flush();

                printWriter.write("OPEN_GATE");
                printWriter.flush();
                publishProgress(50);

                PreActionMessage = mBufferIn.readLine();
                PostActionMessage = mBufferIn.readLine();
                publishProgress(100);

                printWriter.close();
                mBufferIn.close();
                skt1.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
@Override
public void onBackPressed() {
    super.onBackPressed();
}

private void displayLongToast(String ToastMessage) {
    Toast.makeText(Emergency.this, ToastMessage, Toast.LENGTH_LONG).show();
}

private void displayShortToast(String ToastMessage) {
    Toast.makeText(Emergency.this, ToastMessage, Toast.LENGTH_SHORT).show();
}

@Override
public boolean onOptionsItemSelected(@NonNull MenuItem item) {

    if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
        return true;
    }
    return super.onOptionsItemSelected(item);
    }
}