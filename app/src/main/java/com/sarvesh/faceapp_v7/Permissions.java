package com.sarvesh.faceapp_v7;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    public boolean ProgressComplete = false;

    //intenet
    boolean connected=false;

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

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            {connected = true;}
        }
        else
        {connected = false;}



        mDatabaseHandler = new DatabaseHandler(this);
        //RecyclerView Code
        try{
            CardList = getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

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

        if(data == null)
        {   displayShortToast("database ref is empty!");
            return null;}
        if(data.getCount() == 0)
        {   displayShortToast("No Members Found!");
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
            int Synced = data.getInt(data.getColumnIndex("SYNCED"));

            //displayShortToast("Data Retreived Successfully From LocalDB.");
            list.add(new CardData(photo_image, name, status, Synced));
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
        //displayLongToast("Updating to server.");
        //send data to server
        if(connected)
        {
            SyncApp syncApp = new SyncApp(position);
            syncApp.execute(new Integer(position));
        }
        else
        {displayLongToast("Connect to Internet...");}
    }

    @Override
    public void onPermissionSwitch(int position) {
        Log.d("syncupdate","switching permission.");
        //displayShortToast("Updating to LocalDB.");

        Cursor Localdb = mDatabaseHandler.getData();
        Localdb.moveToPosition(position);
        if(Localdb.getInt(Localdb.getColumnIndex("STATUS")) == 1)
        {
            Log.d("status","Old Status is:"+String.valueOf(Localdb.getInt(Localdb.getInt(Localdb.getColumnIndex("STATUS")))));
            UpdateData(Localdb.getInt(Localdb.getColumnIndex("ID")),
                                false,
                                      Localdb.getBlob(Localdb.getColumnIndex("PHOTO")),
                                      Localdb.getString(Localdb.getColumnIndex("NAME")),
                                      Localdb.getInt(Localdb.getColumnIndex("SYNCED")));
        }
        else
        {
            UpdateData(Localdb.getInt(Localdb.getColumnIndex("ID")),
                    true,
                    Localdb.getBlob(Localdb.getColumnIndex("PHOTO")),
                    Localdb.getString(Localdb.getColumnIndex("NAME")),
                    Localdb.getInt(Localdb.getColumnIndex("SYNCED")));
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
        int mPosition;
        public SyncApp(int position) {
            mPosition = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setMax(100);
            progressBar.setProgress(0);
            ProgressComplete = false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.d("status","Progress is:"+String.valueOf(values[0]));
            progressBar.setProgress(values[0]);
            if(Error)
                displayShortToast(ErrorMessage);
        }

        @Override
        protected Integer doInBackground(Integer... Params) {
            Integer param1 = Params[0];
            int mCardPosition = param1.intValue();

            try{
                SyncAppfun(mCardPosition);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer)
        {
            super.onPostExecute(integer);
            displayShortToast(ToastMessage);
            if(progressBar.getProgress() == 100)
            {
                Log.d("status","postexecute Progress is compelete.");
                ProgressComplete = true;
            }

            //update databse to hide sync_button
            Log.d("status","setting sync button to 0");
            if(ProgressComplete == true)
            {
                Cursor Localdb = mDatabaseHandler.getData();
                Localdb.moveToPosition(mPosition);

                Log.d("status","SYNCED value in db is:"+String.valueOf(Localdb.getInt(Localdb.getColumnIndex("SYNCED"))));
                if(Localdb.getInt(Localdb.getColumnIndex("SYNCED")) == 0)
                {
                    Log.d("status","SYNCED was 0");
                    UpdateData(Localdb.getInt(Localdb.getColumnIndex("ID")),
                            false,
                            Localdb.getBlob(Localdb.getColumnIndex("PHOTO")),
                            Localdb.getString(Localdb.getColumnIndex("NAME")),
                            1);
                }
            }
        }

        void SyncAppfun(int CardPosition)
        {
            while(skt == null)
            {
                try {
                    skt = new Socket(HOST, Port);
                    printWriter = new PrintWriter(skt.getOutputStream());
                    BufferedReader mBufferIn;

                    Cursor Localdb = mDatabaseHandler.getData();
                    Localdb.moveToPosition(CardPosition);
                    publishProgress(10);

                    //retreving name from LocalDB
                    String PersonName = Localdb.getString(Localdb.getColumnIndex("NAME"));
                    Log.d("status","PersonName is:"+PersonName);
                    //retreving photoByteArray from LocalDB
                    byte[] PhotobyteArray = Localdb.getBlob(Localdb.getColumnIndex("PHOTO"));
                    publishProgress(20);

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
                            publishProgress(30);

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
                            publishProgress(60);

                            //sending photo_length and photo_file
                            //sending photo size
                            printWriter.write(String.valueOf(PhotobyteArray.length) + '$');
                            printWriter.flush();
                            //sending photo file
                            dos.write(PhotobyteArray, 0, PhotobyteArray.length);
                            dos.flush();
                            publishProgress(70);
                            //receving name& photo received ACK.
                            mBufferIn = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                            Log.d("status","shutting down output");
                            skt.shutdownOutput();
                            Log.d("status","recieving ACK");
                            String ACK;
                            String ResultMessage;
                            publishProgress(80);
                            if(skt.isOutputShutdown())
                            {
                                ACK  = mBufferIn.readLine();
                                if(ACK.equals("?SYNC_DONE"))
                                {
                                    publishProgress(100);//indiacates that process is complete and hide the syncButton otherwise not
                                    ResultMessage = mBufferIn.readLine();
                                    ToastMessage = ResultMessage;
                                }
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
                        publishProgress(30);

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
                            publishProgress(50);
                        }
                        else
                        {
                            Log.d("status","name is too long");
                            displayShortToast("Name is too long!");
                        }
                        publishProgress(60);

                        //shuting down outputStream
                        Log.d("status","buffer input stream");
                        mBufferIn = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                        Log.d("status","shutting down output");
                        skt.shutdownOutput();
                        publishProgress(80);
                        //receving name received ACK.
                        Log.d("status","recieving ACK");
                        String ACK;
                        String ResultMessage;
                        if(skt.isOutputShutdown())
                        {
                            ACK  = mBufferIn.readLine();
                            if(ACK.equals("?SYNC_DONE"))
                            {
                                publishProgress(100);//indiacates that process is complete and hide the syncButton otherwise not
                                ResultMessage = mBufferIn.readLine();
                                ToastMessage = ResultMessage;
                            }
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

    public void UpdateData(int id, boolean status, byte[] photoBlob, String name, int synced)
    {
        int statusInt;
        if(status)
            statusInt = 1;
        else
            statusInt = 0;


        boolean updateData = mDatabaseHandler.updateData(id, photoBlob, name, statusInt, synced);

        if(updateData){
            //displayShortToast("Data Successfully Updated Locally.");
            Log.d("status","Data Successfully Updated Locally.");
        }
        else{
            displayShortToast("Something went wrong with updating local DB!");
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