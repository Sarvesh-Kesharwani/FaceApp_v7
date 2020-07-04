package com.sarvesh.faceapp_v7;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Permissions extends AppCompatActivity implements RecyclerViewClickInterface {

    public String HOST = "192.168.43.205";//serveousercontent.com
    public int Port = 1998;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;

    //progress bar
    ProgressBar progressBar;

    //representing data
    private PermissionAdapter adapter;
    private RecyclerView recyclerView;
    List<CardData> CardList = new ArrayList<>();

    //Refresh with swip down
    //SwipeRefreshLayout swipeRefreshLayout;

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
        /*swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // add items to database
                //add dabase entries to list
                //notify recyclerview to update those adds
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });*/

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
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

            displayShortToast("Data Retreived Successfully From LocalDB.");
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
    private void displayShortToast(String ToastMessage)
    {
        Toast.makeText(Permissions.this,ToastMessage,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSyncClick(int position) {
        Log.d("status","working! Position is:"+ position);
        displayLongToast("Updating to server.");
        //send data to server
        SyncApp syncApp = new SyncApp();
        syncApp.execute(new Integer(position));

    }

    @Override
    public void onPermissionSwitch(int position) {
        Log.d("syncupdate","switching permission.");
        displayShortToast("Updating to LocalDB.");

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

   private class SyncApp extends AsyncTask<Integer, Integer, Integer>
    {
        Socket skt;
        PrintWriter printWriter;
        String ToastMessage;

        boolean Error = false;
        String ErrorMessage;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setMax(100);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
            if(Error)
                displayShortToast(ErrorMessage);
        }

        @Override
        protected Integer doInBackground(Integer... Params) {
            Integer param1 = Params[0];
            int mCardPosition = param1.intValue();

            Log.d("status","syncapp called.");
            try{
                SyncApp(mCardPosition);
            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            displayShortToast(ToastMessage);
        }

        void SyncApp(int CardPosition)
        {
            while(skt == null)
            {
                try {
                    skt = new Socket(HOST, Port);
                    printWriter = new PrintWriter(skt.getOutputStream());
                    BufferedReader mBufferIn;

                    Cursor Localdb = mDatabaseHandler.getData();
                    Localdb.moveToPosition(CardPosition);

                    //retreving name from LocalDB
                    String PersonName = Localdb.getString(Localdb.getColumnIndex("NAME"));
                    Log.d("status","PersonName is:"+PersonName);
                    //retreving photoByteArray from LocalDB
                    byte[] PhotobyteArray = Localdb.getBlob(Localdb.getColumnIndex("PHOTO"));

                    //if person is allowed
                    if(Localdb.getInt(Localdb.getColumnIndex("STATUS")) == 1)//////////////////////**************
                        {
                            Log.d("status","person is allowed sending name&photo.");
                            DataOutputStream dos = new DataOutputStream(skt.getOutputStream());

                            //sending name_length and name
                            //retreving bytes from personName
                            byte[] nameBytes = PersonName.getBytes();
                            int nameBytesLength = nameBytes.length;//no of charaters in the name
                            String nameBytesLengthString = Integer.toString(nameBytesLength);

                            //sending name length
                            if (nameBytesLength < 100)
                            {
                                //sending update delimiter
                                printWriter.write("?UPDATE");
                                printWriter.flush();

                                if (nameBytesLength <= 9)
                                    {
                                         printWriter.write('0' + nameBytesLengthString);
                                         printWriter.flush();
                                    }
                                else
                                    {
                                        printWriter.write(nameBytesLengthString);
                                        printWriter.flush();
                                    }

                                //sending name
                                printWriter.write(PersonName);
                                printWriter.flush();
                            }
                            else
                            {
                                ToastMessage = "Name is too long!";
                                continue;
                            }

                            //sending photo_length and photo_file
                            //sending photo size
                            printWriter.write(String.valueOf(PhotobyteArray.length) + '$');
                            printWriter.flush();
                            //sending photo file
                            dos.write(PhotobyteArray, 0, PhotobyteArray.length);
                            dos.flush();

                            //receving name& photo received ACK.
                            mBufferIn = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                            Log.d("status","shutting down output");
                            skt.shutdownOutput();
                            Log.d("status","recieving ACK");
                            String ACK;
                            String ResultMessage;
                            if(skt.isOutputShutdown())
                            {
                                ACK  = mBufferIn.readLine();
                                ResultMessage = mBufferIn.readLine();
                                Log.d("status","ACK is:"+ACK);
                                Log.d("status","ResultMessage is:"+ResultMessage);
                                ToastMessage = ACK;
                                ToastMessage = ResultMessage;

                            }
                            else
                                Log.d("status","Output isn't down!");
                            skt.close();
                        }

                    //if person is not allowed
                    else if(Localdb.getInt(Localdb.getColumnIndex("STATUS")) == 0)///////////////////**************
                    {
                        Log.d("status","person not allowed sending name only");

                        //sending name_length and name
                        //retreving bytes from personName
                        byte[] nameBytes = PersonName.getBytes();
                        int nameBytesLength = nameBytes.length;//no of charaters in the name
                        String nameBytesLengthString = Integer.toString(nameBytesLength);

                        //sending name length
                        if (nameBytesLength < 100)
                        {
                            printWriter.write("?DELETE");
                            printWriter.flush();

                            if (nameBytesLength <= 9)
                            {
                                printWriter.write('0' + nameBytesLengthString);
                                printWriter.flush();
                            }
                            else
                            {
                                printWriter.write(nameBytesLengthString);
                                printWriter.flush();
                            }
                            //sending name
                            printWriter.write(PersonName);
                            printWriter.flush();
                        }
                        else
                        {
                            Log.d("status","name is too long");
                            displayShortToast("Name is too long!");
                        }

                        //shuting down outputStream
                        Log.d("status","buffer input stream");
                        mBufferIn = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                        Log.d("status","shutting down output");
                        skt.shutdownOutput();

                        //receving name received ACK.
                        Log.d("status","recieving ACK");
                        String ACK;
                        String ResultMessage;
                        if(skt.isOutputShutdown())
                        {
                            ACK  = mBufferIn.readLine();
                            //receving delete result.
                            ResultMessage = mBufferIn.readLine();
                            Log.d("status","ACK is:"+ACK);
                            Log.d("status","DeleteResult is:"+ResultMessage);
                            ToastMessage = ACK;
                        }
                        else
                        {Log.d("status","Output isn't down!");}

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

    ItemTouchHelper.SimpleCallback itemTouchSimpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
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