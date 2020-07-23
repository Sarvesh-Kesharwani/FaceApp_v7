package com.sarvesh.faceapp_v7;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RegisteredVehicles extends AppCompatActivity implements VehicleRecyclerViewClickinterface{

    public String HOST = "serveousercontent.com";//serveousercontent.com
    public int Port = 1998;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;

    public static ImageButton freeServerButton;
    ImageButton refreshButton;
    ImageButton clearAllButton;
    ProgressBar progressBar;
    public boolean ProgressComplete = false;


    boolean connected = false;

    public VehicleAdapter adapter;
    public RecyclerView vehicle_recycler_view;
    List<Vehicle_CardData> VehicleCardList = new ArrayList<>();
    List<Vehicle_CardData> ServerCardList = new ArrayList<>();

    VehicleDatabaseHandler mDatabaseHandler;
    boolean CloudSyncComplete = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vehicles);
        toolbar = findViewById(R.id.registeredVehicles_toolBar);
        toolbar.setTitle("Vehicles");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout = this.findViewById(R.id.registeredVehicles_drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(RegisteredVehicles.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = findViewById(R.id.registeredVehicles_navigation_view);
        //setting navigation_header
        if(Register.UserID == 1)
        {View navView = navigationView.inflateHeaderView(R.layout.navigation_header_admin);}

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        //////
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_register:
                        Intent intent1 = new Intent(RegisteredVehicles.this, Register.class);
                        startActivity(intent1);
                        break;
                    case R.id.nav_permissions:
                        Intent intent2 = new Intent(RegisteredVehicles.this, Permissions.class);
                        startActivity(intent2);
                        break;
                    case R.id.nav_unknown_activity:
                        Intent intent3 = new Intent(RegisteredVehicles.this, Unknown.class);
                        startActivity(intent3);
                        break;
                    case R.id.nav_emergency_toggle:
                        Intent intent4 = new Intent(RegisteredVehicles.this, Emergency.class);
                        startActivity(intent4);
                        break;
                    case R.id.nav_logout:
                        editSharedPref();
                        Intent intent5 = new Intent(RegisteredVehicles.this, MainActivity.class);
                        startActivity(intent5);
                        break;
                    case R.id.nav_vehicles:
                        Intent intent6 = new Intent(RegisteredVehicles.this, RegisteredVehicles.class);
                        startActivity(intent6);
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


        progressBar = (ProgressBar) findViewById(R.id.progressBar2);

        vehicle_recycler_view = (RecyclerView) findViewById(R.id.registeredVehicles_recycler_view);
        adapter = new VehicleAdapter(VehicleCardList, getApplicationContext(), this);
        new ItemTouchHelper(itemTouchSimpleCallback).attachToRecyclerView(vehicle_recycler_view);
        vehicle_recycler_view.setAdapter(adapter);
        vehicle_recycler_view.setLayoutManager(new LinearLayoutManager(RegisteredVehicles.this));

        refreshButton = findViewById(R.id.registeredVehicles_Data_Synced);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //displayLongToast("Updating to server.");
                //send data to server
                if (connected) {
                    Log.d("receve", "grabVehicleCards called.");
                    GrabVehicleCards grabCards = new GrabVehicleCards();
                    grabCards.execute();
                } else {
                    displayLongToast("Connect to Internet...");
                }
            }
        });

        clearAllButton = findViewById(R.id.registeredVehicle_clearAllButton);
        clearAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllCards();
            }
        });

        mDatabaseHandler = new VehicleDatabaseHandler(this);
    }

    public void editSharedPref()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPreference", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedInKey", false);
        editor.apply();
    }

    private void clearAllCards()
    {
        adapter = new VehicleAdapter(new ArrayList<Vehicle_CardData>(), getApplicationContext(), this);
        vehicle_recycler_view.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onVehicleSyncClick(int position) {
        //displayLongToast("Updating to server.");
        //send data to server
        if (connected) {
            SyncApp syncApp = new SyncApp(position);
            syncApp.execute(new Integer(position));
        } else {
            displayLongToast("Please Connect to Internet...");
        }
    }

    @Override
    public void onVehiclePermissionSwitch(int position) {
        Log.d("syncupdate", "switching permission.");
        //displayShortToast("Updating to LocalDB.");

        Cursor Localdb = mDatabaseHandler.getData();
        Localdb.moveToPosition(position);
        if (Localdb.getInt(Localdb.getColumnIndexOrThrow("STATUS")) == 1) {
            Log.d("status", "Old Status is:" + String.valueOf(Localdb.getInt(Localdb.getInt(Localdb.getColumnIndex("STATUS")))));
            UpdateData(Localdb.getInt(Localdb.getColumnIndex("ID")),
                    Localdb.getString(Localdb.getColumnIndex("NUMBER")),
                    Localdb.getString(Localdb.getColumnIndex("NAME")),
                    0,
                    0);
        } else {
            UpdateData(Localdb.getInt(Localdb.getColumnIndex("ID")),
                    Localdb.getString(Localdb.getColumnIndex("NUMBER")),
                    Localdb.getString(Localdb.getColumnIndex("NAME")),
                    1,
                    0);
        }

        /*adapter = new PermissionAdapter(CardList, getApplicationContext(), this);
        recyclerView.setAdapter(adapter);
        adapter.notifyItemChanged(position);*/

        startActivity(new Intent(this, Permissions.class));
        finish();
    }


    private class GrabVehicleCards extends AsyncTask<Integer, Integer, Integer> {
        Socket skt, skt1;
        PrintWriter printWriter;
        String ToastMessage;

        boolean Error = false;
        String ErrorMessage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setMax(100);
            progressBar.setProgress(0);
            CloudSyncComplete = false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
            if (Error)
                displayShortToast(ErrorMessage);
        }

        @Override
        protected Integer doInBackground(Integer... Params) {
            try {
                GrabCardsfun();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            //CardList.clear();
            //Correct method of Refreshing RecyclerList
            publishProgress(90);
            //CardList.addAll(ServerCardList);
            adapter = new VehicleAdapter(ServerCardList, getApplicationContext(), RegisteredVehicles.this);
            vehicle_recycler_view.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            publishProgress(100);
            //bad method of Refreshing RecyclerList
            //CardList.addAll(ServerCardList);
            //adapter.notifyDataSetChanged();
        }

        int NoOfVehicles;
        List<String> VehNumbers;
        List<String> VehNames;

        void GrabCardsfun() {
            while (skt == null) {
                try {
                    skt = new Socket(HOST, Port);
                    printWriter = new PrintWriter(skt.getOutputStream());
                    InputStream sin = skt.getInputStream();
                    BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(skt.getInputStream()));

                    if (!skt.isConnected()) {
                        displayLongToast("Can't connect to server! Reopen Permission Tab or Restart The App");
                        if (!isCancelled()) {
                            cancel(true);
                        }
                    }

                    publishProgress(10);
                    //sending delimiter
                    Log.d("receve", "sending delimiter.");
                    printWriter.write("?RECVDB");
                    printWriter.flush();

                    publishProgress(20);
                    //recieve no of people
                    NoOfVehicles = Integer.parseInt(mBufferIn.readLine());
                    Log.d("receve", "no of people are:" + NoOfVehicles);

                    publishProgress(30);
                    //prepare storage
                    int i = 1;
                    String VehicleNumber = null;
                    VehNumbers = new ArrayList<>();

                    String VehicleName;
                    VehNames = new ArrayList<>();

                    publishProgress(40);
                    //recieving person names.
                    while (i <= NoOfVehicles) {
                        VehicleNumber = String.valueOf(mBufferIn.readLine());
                        VehNumbers.add(VehicleNumber);
                        i++;
                    }
                    Log.d("receve", "VehNumbers are:" + VehNames);

                    publishProgress(50);
                    //receving photo sizes.
                    i = 1;
                    while (i <= NoOfVehicles) {
                        VehicleName = String.valueOf(mBufferIn.readLine());
                        VehNames.add(VehicleName);

                        Log.d("receve", "Reading of Photo " + i + " is Completed.");
                        ServerCardList.add(new Vehicle_CardData(VehNumbers.get(i - 1), VehNames.get(i - 1), true, 1));
                        Log.d("receve", "Saved Photo " + i + " to DB.");
                        i++;
                    }
                    Log.d("receve", "VehNames are:" + String.valueOf(VehNames));

                    displayShortToast(String.valueOf(mBufferIn.readLine()));
                    printWriter.close();
                    mBufferIn.close();
                    skt.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            publishProgress(70);
            AddServerData(ServerCardList, NoOfVehicles);
            publishProgress(80);

        }
    }

    private class SyncApp extends AsyncTask<Integer, Integer, Integer> {
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
            Log.d("status", "Progress is:" + String.valueOf(values[0]));
            progressBar.setProgress(values[0]);
            if (Error)
                displayShortToast(ErrorMessage);
        }

        @Override
        protected Integer doInBackground(Integer... Params) {
            Integer param1 = Params[0];
            int mCardPosition = param1.intValue();

            try {
                SyncAppfun(mCardPosition);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            displayShortToast(ToastMessage);
            if (progressBar.getProgress() == 100) {
                Log.d("status", "postexecute Progress is compelete.");
                ProgressComplete = true;
            }

            //update database to hide sync_button
            Log.d("status", "setting sync button to 0");
            if (ProgressComplete == true) {
                //RefreshRecyclerView
                new Thread() {
                    public void run() {
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Cursor Localdb = mDatabaseHandler.getData();
                                    Localdb.moveToPosition(mPosition);

                                    if (Localdb.getInt(Localdb.getColumnIndex("SYNCED")) == 0) {
                                        Log.d("status", "SYNCED was 0");
                                        UpdateData(Localdb.getInt(Localdb.getColumnIndex("ID")),
                                                Localdb.getString(Localdb.getColumnIndex("NUMBER")),
                                                Localdb.getString(Localdb.getColumnIndex("NAME")),
                                                Localdb.getInt(Localdb.getColumnIndex("STATUS")),
                                                1);
                                    }

                                    adapter.notifyItemChanged(mPosition);
                                    startActivity(new Intent(getApplicationContext(), Permissions.class));
                                }
                            });
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
            finish();
        }

        void SyncAppfun(int CardPosition) {
            while (skt == null) {
                try {
                    skt = new Socket(HOST, Port);
                    printWriter = new PrintWriter(skt.getOutputStream());
                    BufferedReader mBufferIn;

                    if (!skt.isConnected()) {
                        displayLongToast("Can't connect to server! Reopen Permission Tab or Restart The App");
                        if (!isCancelled()) {
                            cancel(true);
                        }
                    }
                    Cursor Localdb = mDatabaseHandler.getData();
                    Localdb.moveToPosition(CardPosition);
                    publishProgress(10);

                    //retreving name from LocalDB
                    String VehicleName = Localdb.getString(Localdb.getColumnIndex("NAME"));
                    Log.d("status", "VehicleName is:" + VehicleName);

                    String VehicleNumber = Localdb.getString(Localdb.getColumnIndex("NUMBER"));
                    Log.d("status", "VehicleNumber is:" + VehicleNumber);

                    publishProgress(20);

                    //if vehicle is allowed
                    if (Localdb.getInt(Localdb.getColumnIndex("STATUS")) == 1)//////////////////////**************
                    {
                        Log.d("status", "person is allowed sending name&photo.");
                        DataOutputStream dos = new DataOutputStream(skt.getOutputStream());

                        //sending name_length and name
                        //retreving bytes from personName
                        byte[] numberBytes = VehicleNumber.getBytes();
                        int numberBytesLength = numberBytes.length;//no of charaters in the name
                        String numberBytesLengthString = Integer.toString(numberBytesLength);
                        publishProgress(30);

                        //sending name length
                        if (numberBytesLength < 100) {
                            //sending update delimiter
                            printWriter.write("?VCLUPD");
                            printWriter.flush();

                            if (numberBytesLength <= 9) {
                                printWriter.write('0' + numberBytesLengthString);
                                printWriter.flush();
                            } else {
                                printWriter.write(numberBytesLengthString);
                                printWriter.flush();
                            }

                            //sending name
                            printWriter.write(VehicleNumber);
                            printWriter.flush();

                            //sending name_length and name
                            //retreving bytes from personName
                            byte[] nameBytes = VehicleName.getBytes();
                            int nameBytesLength = nameBytes.length;//no of charaters in the name
                            String nameBytesLengthString = Integer.toString(nameBytesLength);
                            publishProgress(30);

                            //sending name length
                            if (nameBytesLength < 100) {
                                //sending update delimiter
                                printWriter.write("?VCLUPD");
                                printWriter.flush();

                                if (nameBytesLength <= 9) {
                                    printWriter.write('0' + nameBytesLengthString);
                                    printWriter.flush();
                                } else {
                                    printWriter.write(nameBytesLengthString);
                                    printWriter.flush();
                                }

                                //sending name
                                printWriter.write(VehicleName);
                                printWriter.flush();

                            } else {
                                ToastMessage = "Name is too long!";
                                continue;
                            }
                            publishProgress(60);
                            publishProgress(70);

                            //receving name & number received ACK.
                            mBufferIn = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                            Log.d("status", "shutting down output");
                            skt.shutdownOutput();
                            Log.d("status", "recieving ACK");
                            String ACK;
                            String ResultMessage;
                            publishProgress(80);
                            if (skt.isOutputShutdown()) {
                                ACK = mBufferIn.readLine();
                                if (ACK.equals("?SYNC_DONE")) {
                                    publishProgress(100);//indiacates that process is complete and hide the syncButton otherwise not
                                    ResultMessage = mBufferIn.readLine();
                                    ToastMessage = ResultMessage;
                                }
                            } else
                                Log.d("status", "Output isn't down!");
                            skt.close();
                        }
                    }
                    //if person is not allowed
                    else if (Localdb.getInt(Localdb.getColumnIndex("STATUS")) == 0)///////////////////**************
                    {
                        Log.d("status", "person not allowed sending name only");

                        //sending name_length and name
                        //retreving bytes from personName
                        byte[] numberBytes = VehicleNumber.getBytes();
                        int numberBytesLength = numberBytes.length;//no of charaters in the name
                        String numberBytesLengthString = Integer.toString(numberBytesLength);
                        publishProgress(30);

                        //sending name length
                        if (numberBytesLength < 100) {
                            printWriter.write("?VCLDEL");
                            printWriter.flush();

                            if (numberBytesLength <= 9) {
                                printWriter.write('0' + numberBytesLengthString);
                                printWriter.flush();
                            } else {
                                printWriter.write(numberBytesLengthString);
                                printWriter.flush();
                            }
                            //sending name
                            printWriter.write(VehicleNumber);
                            printWriter.flush();
                            publishProgress(50);
                        } else {
                            Log.d("status", "name is too long");
                            displayShortToast("Name is too long!");
                        }
                        publishProgress(60);

                        //shuting down outputStream
                        Log.d("status", "buffer input stream");
                        mBufferIn = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                        Log.d("status", "shutting down output");
                        skt.shutdownOutput();
                        publishProgress(80);

                        //receving name received ACK.
                        Log.d("status", "recieving ACK");
                        String ACK;
                        String ResultMessage;
                        if (skt.isOutputShutdown()) {
                            ACK = mBufferIn.readLine();
                            if (ACK.equals("?SYNC_DONE")) {
                                publishProgress(100);//indiacates that process is complete and hide the syncButton otherwise not
                                ResultMessage = mBufferIn.readLine();
                                ToastMessage = ResultMessage;
                            }
                        } else {
                            Log.d("status", "Output isn't down!");
                        }
                        skt.close();
                    } else
                        Log.d("status", "Invalid status input!");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void AddServerData(List<Vehicle_CardData> ServerCardlist, int NoOfVehicles) {
        int i = 1;

        while (i <= NoOfVehicles) {
            int statusInt;
            if (ServerCardlist.get(i - 1).vehicleStatus)
                statusInt = 1;
            else
                statusInt = 0;

            boolean insertData = mDatabaseHandler.addServerData(ServerCardlist.get(i - 1).number, ServerCardList.get(i - 1).name, statusInt, ServerCardList.get(i - 1).vehicleSynced);
            if (insertData) {
                Log.d("receve", "Server Data Successfully Inserted Locally.");
            } else {
                Log.d("receve", "Something went wrong with Server-Local-DB!");
            }
            i++;
            Log.d("receve", "Server Iteration: " + i);
        }

    }

    public boolean UpdateData(int id, String number, String name,  int status, int synced) {

        /*int statusInt;
        if (status)
            statusInt = 1;
        else
            statusInt = 0;*/


        boolean updateData = mDatabaseHandler.updateData(id, number, name, status, synced);

        if (updateData) {
            displayShortToast("Data Successfully Updated Locally.");
            Log.d("status", "Data Successfully Updated Locally.");
        } else {
            displayShortToast("Something went wrong with updating local DB!");
        }
        return updateData;
    }

    public void DeleteDataAndPhoto(int id) {
        boolean updateData = mDatabaseHandler.deleteDataLocally(this);

        if (updateData) {
            displayLongToast("Data Locally Deleted Successfully.");
        } else {
            displayLongToast("Something went wrong with updating local DB!");
        }
    }

    private List<CardData> getData() {
        List<CardData> list = new ArrayList<>();
        //get data from localDB
        Cursor data = mDatabaseHandler.getData();

        if (data == null) {
            displayShortToast("database ref is empty!");
            return null;
        }
        if (data.getCount() == 0) {
            displayShortToast("No Members Found!");
            return null;
        }

        //converting .db file into list, which will be passed to recycler view in OnCreate().
        while (data.moveToNext()) {
            String name = data.getString(data.getColumnIndex("NAME"));
            String photo_path = data.getString(data.getColumnIndex("PHOTO"));

            boolean status = false;
            if (data.getInt(data.getColumnIndex("STATUS")) == 1)
                status = true;
            else if (data.getInt(data.getColumnIndex("STATUS")) == 0)
                status = false;
            else
                Log.d("status", "Invalid status input!");
            int Synced = data.getInt(data.getColumnIndex("SYNCED"));

            Bitmap myBitmap = BitmapFactory.decodeFile(photo_path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            myBitmap.compress(Bitmap.CompressFormat.PNG, 100 , baos);
            byte[] b = baos.toByteArray();
            list.add(new CardData(b, name, status, Synced));
        }
        return list;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayLongToast(String ToastMessage) {
        Toast.makeText(RegisteredVehicles.this, ToastMessage, Toast.LENGTH_LONG).show();
    }

    private void displayShortToast(String ToastMessage) {
        Toast.makeText(RegisteredVehicles.this, ToastMessage, Toast.LENGTH_SHORT).show();
    }

    ItemTouchHelper.SimpleCallback itemTouchSimpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            VehicleCardList.remove(viewHolder.getAdapterPosition());
            adapter.notifyDataSetChanged();
        }
    };
}
