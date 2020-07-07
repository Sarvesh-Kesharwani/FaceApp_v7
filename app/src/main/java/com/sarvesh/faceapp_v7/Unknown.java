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

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Unknown extends AppCompatActivity implements UnknownRecyclerViewClickInterface {

    public String HOST = "192.168.43.205";//serveousercontent.com
    public int Port = 1998;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;

    ImageButton refreshButton;
    ImageButton clearAllButton;
    ProgressBar progressBar;

    boolean connected = false;

    public UnknownAdapter adapter;
    public RecyclerView unknown_recycler_view;
    List<Unknown_CardData> UnknownCardList = new ArrayList<>();
    DatabaseHandler mDatabaseHandler;
    private boolean ProgressComplete = false;


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
        actionBarDrawerToggle = new ActionBarDrawerToggle(Unknown.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = findViewById(R.id.Unknown_navigation_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_register:
                        Intent intent1 = new Intent(Unknown.this, Register.class);
                        startActivity(intent1);
                        break;
                    case R.id.nav_permissions:
                        Intent intent2 = new Intent(Unknown.this, Permissions.class);
                        startActivity(intent2);
                        break;
                    case R.id.nav_unknown_activity:
                        Intent intent3 = new Intent(Unknown.this, Unknown.class);
                        startActivity(intent3);
                        break;
                    case R.id.nav_emergency_toggle:
                        Intent intent4 = new Intent(Unknown.this, Emergency.class);
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
            //UnknownCardList = getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);

        unknown_recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        //adapter = new PermissionAdapter(Unknown_CardData, getApplicationContext(), this);
        //new ItemTouchHelper(itemTouchSimpleCallback).attachToRecyclerView(unknown_recycler_view);
        unknown_recycler_view.setAdapter(adapter);
        unknown_recycler_view.setLayoutManager(new LinearLayoutManager(Unknown.this));

        refreshButton = findViewById(R.id.refreshButton2);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //displayLongToast("Updating to server.");
                //send data to server
                if (connected) {
                    Log.d("receve", "grabUnknownCards called.");
                    /*GrabUnknownCards grabCards = new GrabUnknownCards();
                    grabCards.execute();*/
                } else {
                    displayLongToast("Connect to Internet...");
                }
            }
        });

        clearAllButton = findViewById(R.id.clearAllButton2);
        clearAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //clearAllCards();
            }
        });
    }

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
        Toast.makeText(Unknown.this, ToastMessage, Toast.LENGTH_LONG).show();
    }

    private void displayShortToast(String ToastMessage) {
        Toast.makeText(Unknown.this, ToastMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSyncClick(int position) {
        //displayLongToast("Updating to server.");
        //send data to server
        if (connected) {
            GrabUnknownCards GrabUnownCards = new GrabUnknownCards(position);
            GrabUnownCards.execute(new Integer(position));
        } else {
            displayLongToast("Connect to Internet...");
        }
    }

    private class GrabUnknownCards extends AsyncTask<Integer, Integer, Integer> {
        Socket skt;
        PrintWriter printWriter;
        String ToastMessage;

        boolean Error = false;
        String ErrorMessage;
        int mPosition;

        public GrabUnknownCards(int position) {
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
               // GrabUnknownfun(mCardPosition);
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
        }

     /*   void GrabUnknownfun(int CardPosition) {
            while (skt == null) {
                try {
                    skt = new Socket(HOST, Port);
                    printWriter = new PrintWriter(skt.getOutputStream());

                    if (!skt.isConnected()) {
                        displayLongToast("Can't connect to server! Reopen Permission Tab or Restart The App");
                        if (!isCancelled()) {
                            cancel(true);
                        }
                    }

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

            publishProgress(10);
            //sending delimiter
            Log.d("receve", "sending delimiter.");
            printWriter.write("?UNKNON");
            printWriter.flush();

            publishProgress(20);
            int i = 1;
            int NoOfPeople = 10;
            while (skt == null || i <= NoOfPeople) {
                try {
                    skt = new Socket(HOST, Port);
                    InputStream sin = skt.getInputStream();
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
        }*/

        public void clearAllCards() {
            //update recycler list with new (EMPTY) ArrayList of type CARD
            adapter = new UnknownAdapter(new ArrayList<CardData>(), getApplicationContext(), Unknown.this);
            unknown_recycler_view.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        private List<CardData> getData() {
            List<CardData> list = new ArrayList<>();

            //displayShortToast("Data Retreived Successfully From LocalDB.");
           //list.add(new Unknown_CardData(photo_image, name));
            return list;
        }



        ItemTouchHelper.SimpleCallback itemTouchSimpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                mDatabaseHandler = new DatabaseHandler(Unknown.this);
                Cursor Localdb = mDatabaseHandler.getData();
                Localdb.moveToPosition(viewHolder.getAdapterPosition());
                mDatabaseHandler.deleteData(Localdb.getInt(Localdb.getColumnIndex("ID")));
                UnknownCardList.remove(viewHolder.getAdapterPosition());
                adapter.notifyDataSetChanged();
            }
        };

    }

}
