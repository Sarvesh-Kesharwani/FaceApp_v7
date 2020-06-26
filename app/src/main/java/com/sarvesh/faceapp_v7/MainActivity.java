package com.sarvesh.faceapp_v7;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    public String HOST = "192.168.43.205";
    public int Port = 1998;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                        SendOperation sendop = new SendOperation();
                        sendop.execute();
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

    class SendOperation extends AsyncTask<Void, Void, Void>
    {
        Socket s8;
        PrintWriter pw8;

        @Override
        protected Void doInBackground(Void... voids) {

            while(s8 == null)
            {
                try {
                    s8 = new Socket(HOST, Port);
                    pw8 = new PrintWriter(s8.getOutputStream());

                    pw8.write("1");
                    pw8.close();
                    s8.close();
                } catch (IOException e) {
                    System.out.println("Fail");
                    e.printStackTrace();
                }
            }
            return null;
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

}
