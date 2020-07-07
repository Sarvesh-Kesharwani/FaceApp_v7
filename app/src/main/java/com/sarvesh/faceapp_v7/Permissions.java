package com.sarvesh.faceapp_v7;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.almworks.sqlite4java.SQLiteConnection;
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
import java.util.List;


public class Permissions extends AppCompatActivity implements RecyclerViewClickInterface {

    public String HOST = "serveousercontent.com";//serveousercontent.com
    public int Port = 1998;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;

    //Refresh to grab data from server
    ImageButton refreshButton;
    ImageButton clearAllButton;
    //progress bar
    ProgressBar progressBar;
    public boolean ProgressComplete = false;

    //intenet
    boolean connected = false;

    //representing data
    public PermissionAdapter adapter;
    public RecyclerView recyclerView;
    List<CardData> CardList = new ArrayList<>();
    List<CardData> ServerCardList = new ArrayList<>();
    //Refresh with swip down
    //SwipeRefreshLayout swipeRefreshLayout;

    //Database
    DatabaseHandler mDatabaseHandler;
    //DownlodCould
    boolean CloudSyncComplete = false;

    SQLiteConnection sqLiteConnection=null;
    SQLiteStatement sqLiteStatement=null;

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

        mDatabaseHandler = new DatabaseHandler(this);
        //RecyclerView Code
        try {
            CardList = getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new PermissionAdapter(CardList, getApplicationContext(), this);
        new ItemTouchHelper(itemTouchSimpleCallback).attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(Permissions.this));

        refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //displayLongToast("Updating to server.");
                //send data to server
                if (connected) {
                    Log.d("receve", "grabcards called.");
                    GrabCards grabCards = new GrabCards();
                    grabCards.execute();
                } else {
                    displayLongToast("Connect to Internet...");
                }
            }
        });

        clearAllButton = findViewById(R.id.clearAllButton);
        clearAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllCards();
            }
        });

    }

    public void clearAllCards()
    {
        mDatabaseHandler = new DatabaseHandler(Permissions.this);
        Cursor Localdb = mDatabaseHandler.getData();
        while (Localdb.moveToNext()) {
            try {
                //remove cards from DB one-by-one
                mDatabaseHandler.deleteData(Localdb.getInt(Localdb.getColumnIndex("ID")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //update recycler list with new (EMPTY) ArrayList of type CARD
        adapter = new PermissionAdapter(new ArrayList<CardData>(), getApplicationContext(), Permissions.this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }




    @Override
    public void onSyncClick(int position) {
        //displayLongToast("Updating to server.");
        //send data to server
        if (connected) {
            SyncApp syncApp = new SyncApp(position);
            syncApp.execute(new Integer(position));
        } else {
            displayLongToast("Connect to Internet...");
        }
    }

    @Override
    public void onPermissionSwitch(int position) {
        Log.d("syncupdate", "switching permission.");
        //displayShortToast("Updating to LocalDB.");

        Cursor Localdb = mDatabaseHandler.getData();
        Localdb.moveToPosition(position);
        if (Localdb.getInt(Localdb.getColumnIndex("STATUS")) == 1) {
            Log.d("status", "Old Status is:" + String.valueOf(Localdb.getInt(Localdb.getInt(Localdb.getColumnIndex("STATUS")))));
            UpdateData(Localdb.getInt(Localdb.getColumnIndex("ID")),
                    false,
                    Localdb.getString(Localdb.getColumnIndex("PHOTO")),
                    Localdb.getString(Localdb.getColumnIndex("NAME")),
                    0);
        } else {
            UpdateData(Localdb.getInt(Localdb.getColumnIndex("ID")),
                    true,
                    Localdb.getString(Localdb.getColumnIndex("PHOTO")),
                    Localdb.getString(Localdb.getColumnIndex("NAME")),
                    0);
        }
    }




    private class GrabCards extends AsyncTask<Integer, Integer, Integer> {
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
            adapter = new PermissionAdapter(ServerCardList, getApplicationContext(), Permissions.this);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            publishProgress(100);
            //bad method of Refreshing RecyclerList
            //CardList.addAll(ServerCardList);
            //adapter.notifyDataSetChanged();
        }

        int NoOfPeople;
        List<String> PersonNames;
        List<Integer> PersonPhotoSizes;

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
                    printWriter.write("?RETREV");
                    printWriter.flush();

                    publishProgress(20);
                    //recieve no of people
                    NoOfPeople = Integer.parseInt(mBufferIn.readLine());
                    Log.d("receve", "no of people are:" + NoOfPeople);

                    publishProgress(30);
                    //prepare storage
                    int i = 1;
                    String PersonName = null;
                    PersonNames = new ArrayList<>();

                    int PersonPhotoSize = 0;
                    PersonPhotoSizes = new ArrayList<>();

                    publishProgress(40);
                    //recieving person names.
                    while (i <= NoOfPeople) {
                        PersonName = String.valueOf(mBufferIn.readLine());
                        PersonNames.add(PersonName);
                        i++;
                    }
                    Log.d("receve", "Names are:" + PersonNames);

                    publishProgress(50);
                    //receving photo sizes.
                    i = 1;
                    while (i <= NoOfPeople) {
                        PersonPhotoSize = Integer.parseInt(mBufferIn.readLine());
                        PersonPhotoSizes.add(new Integer(PersonPhotoSize));
                        i++;
                    }
                    Log.d("receve", "Person photoSizes are:" + String.valueOf(PersonPhotoSizes));

                    printWriter.close();
                    mBufferIn.close();
                    skt.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            int i = 1;
            while (skt1 == null || i <= NoOfPeople) {
                try {
                    skt1 = new Socket(HOST, Port);
                    InputStream sin = skt1.getInputStream();
                    DataInputStream dis = new DataInputStream(sin);

                    byte[] data = new byte[PersonPhotoSizes.get(i - 1).intValue()];
                    try {
                        dis.readFully(data, 0, data.length);
                    } catch (IOException e) {
                        Log.d("receve", "Failed to retrieve image.");
                        e.printStackTrace();
                    }

                    Log.d("receve", "Reading of Photo " + i + " is Completed.");
                    ServerCardList.add(new CardData(data, PersonNames.get(i - 1), true, 1));
                    Log.d("receve", "Saved Photo " + i + " to DB.");
                    i++;
                    dis.close();
                    skt1.close();
                } catch (IOException e) {
                    System.out.println("Fail");
                    e.printStackTrace();
                }
            }
            publishProgress(70);
            AddServerData(ServerCardList, NoOfPeople);
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

            //update databse to hide sync_button
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
                                                false,
                                                Localdb.getString(Localdb.getColumnIndex("PHOTO")),
                                                Localdb.getString(Localdb.getColumnIndex("NAME")),
                                                1);
                                    }

                                    adapter.notifyItemChanged(mPosition);
                                }
                            });
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
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
                    String PersonName = Localdb.getString(Localdb.getColumnIndex("NAME"));
                    Log.d("status", "PersonName is:" + PersonName);
                    //retreving photoByteArray from LocalDB
                    byte[] PhotobyteArray = Localdb.getBlob(Localdb.getColumnIndex("PHOTO"));
                    publishProgress(20);

                    //if person is allowed
                    if (Localdb.getInt(Localdb.getColumnIndex("STATUS")) == 1)//////////////////////**************
                    {
                        Log.d("status", "person is allowed sending name&photo.");
                        DataOutputStream dos = new DataOutputStream(skt.getOutputStream());

                        //sending name_length and name
                        //retreving bytes from personName
                        byte[] nameBytes = PersonName.getBytes();
                        int nameBytesLength = nameBytes.length;//no of charaters in the name
                        String nameBytesLengthString = Integer.toString(nameBytesLength);
                        publishProgress(30);

                        //sending name length
                        if (nameBytesLength < 100) {
                            //sending update delimiter
                            printWriter.write("?UPDATE");
                            printWriter.flush();

                            if (nameBytesLength <= 9) {
                                printWriter.write('0' + nameBytesLengthString);
                                printWriter.flush();
                            } else {
                                printWriter.write(nameBytesLengthString);
                                printWriter.flush();
                            }

                            //sending name
                            printWriter.write(PersonName);
                            printWriter.flush();
                        } else {
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

                    //if person is not allowed
                    else if (Localdb.getInt(Localdb.getColumnIndex("STATUS")) == 0)///////////////////**************
                    {
                        Log.d("status", "person not allowed sending name only");

                        //sending name_length and name
                        //retreving bytes from personName
                        byte[] nameBytes = PersonName.getBytes();
                        int nameBytesLength = nameBytes.length;//no of charaters in the name
                        String nameBytesLengthString = Integer.toString(nameBytesLength);
                        publishProgress(30);

                        //sending name length
                        if (nameBytesLength < 100) {
                            printWriter.write("?DELETE");
                            printWriter.flush();

                            if (nameBytesLength <= 9) {
                                printWriter.write('0' + nameBytesLengthString);
                                printWriter.flush();
                            } else {
                                printWriter.write(nameBytesLengthString);
                                printWriter.flush();
                            }
                            //sending name
                            printWriter.write(PersonName);
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
                } catch (IOException e) {
                    System.out.println("Fail");
                    e.printStackTrace();
                }
            }
        }
    }



    public void AddServerData(List<CardData> ServerCardlist, int NoOfPeople) {
        int i = 1;

        while (i <= NoOfPeople) {
            int statusInt;
            if (ServerCardlist.get(i - 1).PersonPermissionStatus)
                statusInt = 1;
            else
                statusInt = 0;

            boolean insertData = mDatabaseHandler.addServerData(ServerCardlist.get(i - 1).PersonPhoto, ServerCardList.get(i - 1).PersonName, statusInt, ServerCardList.get(i - 1).PermissionDataSynced, this);
            if (insertData) {
                Log.d("receve", "Server Data Successfully Inserted Locally.");
            } else {
                Log.d("receve", "Something went wrong with Server-Local-DB!");
            }
            i++;
            Log.d("receve", "Server Iteration: " + i);
        }

    }

    public boolean UpdateData(int id, boolean status, String photoBlob, String name, int synced) {
        int statusInt;
        if (status)
            statusInt = 1;
        else
            statusInt = 0;


        boolean updateData = mDatabaseHandler.updateData(id, photoBlob, name, statusInt, synced);

        if (updateData) {
            displayShortToast("Data Successfully Updated Locally.");
            Log.d("status", "Data Successfully Updated Locally.");
        } else {
            displayShortToast("Something went wrong with updating local DB!");
        }
        return updateData;
    }

    public void DeleteData(int id) {
        boolean updateData = mDatabaseHandler.deleteData(id);

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
                Log.d("status", "INvalid status input!");
            int Synced = data.getInt(data.getColumnIndex("SYNCED"));

            //displayShortToast("Data Retreived Successfully From LocalDB.");
            File imagefile = new File(photo_path);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(imagefile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Bitmap bm = BitmapFactory.decodeStream(fis);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 0 , baos);
            byte[] b = baos.toByteArray();
            list.add(new CardData(b, name, status, Synced));
        }
        return list;
    }




//////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    class MyAndroidThread implements Runnable {
        AppCompatActivity activity;
        String command;

        public MyAndroidThread(AppCompatActivity activity, String Command) {
            this.activity = activity;
            command = Command;
        }

        @Override
        public void run() {

            //perform heavy task here and finally update the UI with result this way -
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), command, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void displayLongToast(String ToastMessage) {
        Toast.makeText(Permissions.this, ToastMessage, Toast.LENGTH_LONG).show();
    }

    private void displayShortToast(String ToastMessage) {
        Toast.makeText(Permissions.this, ToastMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ItemTouchHelper.SimpleCallback itemTouchSimpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            mDatabaseHandler = new DatabaseHandler(Permissions.this);
            Cursor Localdb = mDatabaseHandler.getData();
            Localdb.moveToPosition(viewHolder.getAdapterPosition());
            mDatabaseHandler.deleteData(Localdb.getInt(Localdb.getColumnIndex("ID")));
            CardList.remove(viewHolder.getAdapterPosition());
            adapter.notifyDataSetChanged();
        }
    };
}