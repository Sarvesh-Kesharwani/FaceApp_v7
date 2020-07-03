package com.sarvesh.faceapp_v7;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

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

    //Database
    DatabaseHandler mDatabaseHandler;
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

        mDatabaseHandler = new DatabaseHandler(this);

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
        //find person data from LocalDB using his name and add to list and pass to recycler view.
        //list.add(new CardData(null,"name surname",true,false));

        ///get data from localDB
        Cursor data = mDatabaseHandler.getData();
        data.moveToFirst();

        if(data == null)
        {   displayLongToast("database ref is empty!");
            return null;}
        if(data.getCount() == 0)
        {   displayLongToast("database is empty!");
            return null;}

        String name = data.getString(data.getColumnIndex("NAME"));
        byte [] photo_image = data.getBlob(data.getColumnIndex("PHOTO"));
        boolean status = true;

        displayLongToast("data retrieved successfully from db.");
        list.add(new CardData(photo_image, name, status,false));
        return list;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayLongToast(String ToastMessage)
    {
        Toast.makeText(Permissions.this,ToastMessage,Toast.LENGTH_LONG).show();
    }
}