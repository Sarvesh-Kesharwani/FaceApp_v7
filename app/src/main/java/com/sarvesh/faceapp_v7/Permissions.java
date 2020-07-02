package com.sarvesh.faceapp_v7;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

public class Permissions extends AppCompatActivity{

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private PermissionAdapter adapter;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Navigation Coding Start
        setContentView(R.layout.activity_permissions);

        toolbar = findViewById(R.id.permission_toolBar);
        toolbar.setTitle("Permission");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = this.findViewById(R.id.permission_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(Permissions.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = findViewById(R.id.permission_navigation_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_register:
                        Intent intent1 = new Intent(Permissions.this, Register.class);
                        startActivity(intent1);
                        break;
                    case R.id.nav_permissions:
                        Intent intent2 = new Intent(Permissions.this, Permissions.class);
                        startActivity(intent2);
                        break;
                    case R.id.nav_unknown_activity:
                        Intent intent3 = new Intent(Permissions.this, Unknown.class);
                        startActivity(intent3);
                        break;
                    case R.id.nav_emergency_toggle:
                        Intent intent4 = new Intent(Permissions.this, Emergency.class);
                        startActivity(intent4);
                        break;
                }
                return false;
            }
        });

        //RecyclerView Code
        List<CardData> list = new ArrayList<>();
        list = getData();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new PermissionAdapter(list, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(Permissions.this));
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    private List<CardData> getData()
    {
        List<CardData> list = new ArrayList<>();
        list.add(new CardData("First Exam",
                "May 23, 2015",
                "Best Of Luck"));
        list.add(new CardData("Second Exam",
                "June 09, 2015",
                "b of l"));
        list.add(new CardData("My Test Exam",
                "April 27, 2017",
                "This is testing exam .."));

        return list;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}