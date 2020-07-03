package com.sarvesh.faceapp_v7;

import android.content.Intent;
import android.database.Cursor;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Permissions extends AppCompatActivity implements RecyclerViewClickInterface {

    public String HOST = "192.168.43.205";//serveousercontent.com
    public int Port = 1998;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;

    //representing data
    private PermissionAdapter adapter;
    private RecyclerView recyclerView;
    List<CardData> CardList = new ArrayList<>();

    //Database
    DatabaseHandler mDatabaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Navigation Coding Start
        setContentView(R.layout.activity_permissions);
        toolbar = findViewById(R.id.permission_toolBar);
        toolbar.setTitle("Permissions");
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
        try{
            CardList = getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new PermissionAdapter(CardList, getApplicationContext(), this);
        new ItemTouchHelper(itemTouchSimpleCallback).attachToRecyclerView(recyclerView);
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
        ///get data from localDB
        Cursor data = mDatabaseHandler.getData();
        //data.moveToFirst();

        if(data == null)
        {   displayLongToast("database ref is empty!");
            return null;}
        if(data.getCount() == 0)
        {   displayLongToast("No Members Found!");
            return null;}

        //converting .db file into list, which will be passed to recycler view in OnCreate().
        while(data.moveToNext())
        {
            String name = data.getString(data.getColumnIndex("NAME"));
            byte [] photo_image = data.getBlob(data.getColumnIndex("PHOTO"));

            boolean status = false;
            if(data.getInt(data.getColumnIndex("STATUS")) == 1)
                status = true;
            else if(data.getInt(data.getColumnIndex("STATUS")) == 0)
                status = false;
            else
                Log.d("status","INvalid status input!");

            displayLongToast("data retrieved successfully from db.");
            list.add(new CardData(photo_image, name, status,false));
        }
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

    @Override
    public void onSyncClick(int position) {
        Log.d("syncupdate","working! Position is:"+ position);
        displayLongToast("Updating to server.");
        //send data to server
        SyncApp syncApp = new SyncApp();
        syncApp.execute(new Integer(position));

    }

    @Override
    public void onPermissionSwitch(int position) {
        Log.d("syncupdate","switching permission.");
        displayLongToast("Updating to LocalDB.");

        Cursor Localdb = mDatabaseHandler.getData();
        Localdb.moveToPosition(position);
        if(Localdb.getInt(Localdb.getColumnIndex("STATUS")) == 1)
        {
            Log.d("status","Old Status is:"+String.valueOf(Localdb.getInt(Localdb.getInt(Localdb.getColumnIndex("STATUS")))));
            UpdateData(Localdb.getInt(Localdb.getColumnIndex("ID")),
                                false,
                                      Localdb.getBlob(Localdb.getColumnIndex("PHOTO")),
                                      Localdb.getString(Localdb.getColumnIndex("NAME")));
        }
        else
        {
            UpdateData(Localdb.getInt(Localdb.getColumnIndex("ID")),
                    true,
                    Localdb.getBlob(Localdb.getColumnIndex("PHOTO")),
                    Localdb.getString(Localdb.getColumnIndex("NAME")));
        }
    }

    /* class MyAndroidThread implements Runnable
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
    */

   private class SyncApp extends AsyncTask<Integer, Void, Void>
    {
        Socket skt;
        PrintWriter printWriter;

        @Override
        protected Void doInBackground(Integer... Params) {
            Integer param1 = Params[0];
            int mCardPosition = param1.intValue();

            Log.d("status","syncapp called.");
            SyncApp(mCardPosition);
            return null;
        }

        void SyncApp(int CardPosition)
        {
            while(skt == null)
            {
                try {
                    skt = new Socket(HOST, Port);
                    printWriter = new PrintWriter(skt.getOutputStream());
                    BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(skt.getInputStream()));

                    Cursor Localdb = mDatabaseHandler.getData();
                    Log.d("status","Card Position is: "+String.valueOf(CardPosition));
                    Localdb.moveToPosition(CardPosition);

                    //retreving name from LocalDB
                    String PersonName = Localdb.getString(Localdb.getColumnIndex("NAME"));
                    Log.d("status","PersonName is:"+PersonName);
                    //retreving photoByteArray from LocalDB
                    byte[] PhotobyteArray = Localdb.getBlob(Localdb.getColumnIndex("PHOTO"));

                    //if person is allowed
                    if(Localdb.getInt(Localdb.getColumnIndex("STATUS")) == 1)
                    {
                        DataOutputStream dos = new DataOutputStream(skt.getOutputStream());
                        //sending update delimiter
                        printWriter.write("?UPDATE");


                        //sending name_length and name
                        //retreving bytes from personName
                        byte[] nameBytes = PersonName.getBytes();
                        int nameBytesLength = nameBytes.length;//no of charaters in the name
                        String nameBytesLengthString = Integer.toString(nameBytesLength);

                        //sending name length
                        if (nameBytesLength < 100) {
                            if (nameBytesLength <= 9)
                                printWriter.write('0' + nameBytesLengthString);
                            else
                                printWriter.write(nameBytesLengthString);

                            //sending name
                            printWriter.write(PersonName);
                        }
                        else
                            displayLongToast("Name is too long!");



                        //sending photo_length and photo_file
                        //sending photo size
                        printWriter.write(String.valueOf(PhotobyteArray.length) + '$');

                        //sending photo file
                        dos.write(PhotobyteArray, 0, PhotobyteArray.length);

                        printWriter.close();
                        dos.close();

                        //receving name& photo received ACK.
                        String ACK = mBufferIn.readLine();
                        displayLongToast(ACK);
                        skt.close();
                    }

                    //if person is not allowed
                    else if(Localdb.getInt(Localdb.getColumnIndex("STATUS")) == 0)
                    {
                        printWriter.write("?DELETE");

                        //sending name_length and name
                        //retreving bytes from personName
                        byte[] nameBytes = PersonName.getBytes();
                        int nameBytesLength = nameBytes.length;//no of charaters in the name
                        String nameBytesLengthString = Integer.toString(nameBytesLength);

                        //sending name length
                        if (nameBytesLength < 100)
                        {
                            if (nameBytesLength <= 9)
                                printWriter.write('0' + nameBytesLengthString);
                            else
                                printWriter.write(nameBytesLengthString);

                            //sending name
                            printWriter.write(PersonName);
                        }
                        else
                            displayLongToast("Name is too long!");

                        printWriter.close();

                        //receving ACK
                        String ACK = mBufferIn.readLine();
                        displayLongToast(ACK);

                        skt.close();
                    }
                    else
                        Log.d("status","Invalid status input!");
                } catch (IOException e) {
                    System.out.println("Fail");
                    e.printStackTrace();
                }
            }
        }
    }

    public void UpdateData(int id, boolean status, byte[] photoBlob, String name)
    {
        int statusInt;
        if(status)
            statusInt = 1;
        else
            statusInt = 0;

        boolean updateData = mDatabaseHandler.updateData(id, photoBlob, name, statusInt);

        if(updateData){
            displayLongToast("Data Successfully Updated Locally.");
        }
        else{
            displayLongToast("Something went wrong with updating local DB!");
        }
    }

    public void DeleteData(int id)
    {
        boolean updateData = mDatabaseHandler.deleteData(id);

        if(updateData){
            displayLongToast("Data Locally Deleted Successfully.");
        }
        else{
            displayLongToast("Something went wrong with updating local DB!");
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
                    pw9.write("2");
                    pw9.flush();
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

    ItemTouchHelper .SimpleCallback itemTouchSimpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Cursor Localdb = mDatabaseHandler.getData();
            Localdb.moveToPosition(viewHolder.getAdapterPosition());
                mDatabaseHandler.deleteData(Localdb.getInt(Localdb.getColumnIndex("ID")));
                CardList.remove(viewHolder.getAdapterPosition());
                adapter.notifyDataSetChanged();

        }
    };
}