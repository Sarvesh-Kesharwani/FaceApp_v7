package com.sarvesh.faceapp_v7;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.sarvesh.faceapp_v7.Register.UserID;

public class Unknown extends AppCompatActivity implements UnknownRecyclerViewClickInterface {

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

    boolean connected = false;

    public UnknownAdapter adapter;
    public RecyclerView unknown_recycler_view;
    List UnknownCardList = new ArrayList<Unknown_CardData>();
    DatabaseHandler mDatabaseHandler;
    private boolean ProgressComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Navigation Coding Start
        setContentView(R.layout.activity_unknown);
        toolbar = findViewById(R.id.Unknown_toolbar);
        toolbar.setTitle("Unknonwn Activites");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout = this.findViewById(R.id.unknonw_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(Unknown.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = findViewById(R.id.Unknown_navigation_view);
        //setting navigation_header
        if(UserID == 1)
        {View navView = navigationView.inflateHeaderView(R.layout.navigation_header_admin);}

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        //////
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
                    case R.id.nav_logout:
                        editSharedPref();
                        Intent intent5 = new Intent(Unknown.this, MainActivity.class);
                        startActivity(intent5);
                        break;
                    case R.id.nav_vehicles:
                        Intent intent6 = new Intent(Unknown.this, RegisteredVehicles.class);
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

        freeServerButton = findViewById(R.id.Free_Server);
        mDatabaseHandler = new DatabaseHandler(this);

        //RecyclerView Code
        try {
            //grabing cards from server.
            if (connected) {
                GrabUnknownCards GrabUnownCards = new GrabUnknownCards();
                GrabUnownCards.execute();
            } else {
                displayLongToast("Connect to Internet...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);

        unknown_recycler_view = (RecyclerView) findViewById(R.id.unknown_recycler_view);
        adapter = new UnknownAdapter(UnknownCardList, getApplicationContext(), this);
        new ItemTouchHelper(itemTouchSimpleCallback).attachToRecyclerView(unknown_recycler_view);
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
                    GrabUnknownCards grabCards = new GrabUnknownCards();
                    grabCards.execute();
                } else {
                    displayLongToast("Connect to Internet...");
                }
            }
        });

        clearAllButton = findViewById(R.id.clearAllButton2);
        clearAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllCards();
            }
        });

        freeServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog();
            }
        });

        if(UserID == 0)
        {freeServerButton.setVisibility(View.GONE);}
        else
        {freeServerButton.setVisibility(View.VISIBLE);}
    }

   /* @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putParcelableArrayList("tempList", UnknownCardList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        UnknownCardList = savedInstanceState.getParcelableArrayList("tempLIst");

        adapter = new UnknownAdapter(new ArrayList<Unknown_CardData>(), getApplicationContext(), this);
        unknown_recycler_view.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }*/

    public void editSharedPref()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPreference", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedInKey", false);
        editor.apply();
    }

    private void clearAllCards()
    {
        adapter = new UnknownAdapter(new ArrayList<Unknown_CardData>(), getApplicationContext(), this);
        unknown_recycler_view.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    private void alertDialog() {
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setMessage("All unknown-activity photos will be deleted permanently!");
        dialog.setTitle("Are you sure?");
        dialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        Toast.makeText(getApplicationContext(),"Operation is being performed...",Toast.LENGTH_LONG).show();
                        FreeServer freeServer = new FreeServer();
                        freeServer.execute();
                    }
                });
        dialog.setNegativeButton("cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"Operation Canceled.",Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog alertDialog=dialog.create();
        alertDialog.show();
    }

    @Override
    public void onFullScreenClick(int position) {
        Intent intent = new Intent(this, FullScreenView.class);

        Unknown_CardData temp = (Unknown_CardData) UnknownCardList.get(position);
        intent.putExtra("FullImage", temp.Unknown_Person_Photo);
        intent.putExtra("FullImageName", temp.Image_Capture_Time);
        Log.d("full","ImageSavedToInstance.");
        startActivity(intent);
    }


    private class GrabUnknownCards extends AsyncTask<Integer, Integer, Void> {
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
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.d("status", "Progress is:" + values[0]);
            progressBar.setProgress(values[0]);
            if (Error)
                displayShortToast(ErrorMessage);
        }

        @Override
        protected Void doInBackground(Integer... Params) {
            try {
                GrabUnknownfun();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            displayShortToast("Data Retrieved Successfully.");
            adapter = new UnknownAdapter(UnknownCardList, getApplicationContext(), Unknown.this);
            publishProgress(90);
            unknown_recycler_view.setAdapter(adapter);
            publishProgress(100);
            adapter.notifyDataSetChanged();
        }

            int NoOfPhotos;
            List<String> PersonNames;
            List<Integer> PersonPhotoSizes;
            private void GrabUnknownfun()
            {
                while (skt == null) {
                    try {
                        publishProgress(20);
                        skt = new Socket(HOST, Port);
                        printWriter = new PrintWriter(skt.getOutputStream());
                        BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                        publishProgress(30);

                        //sending delimiter
                        Log.d("receve", "sending delimiter.");
                        printWriter.write("?UNKNON");
                        printWriter.flush();
                        Log.d("receve", "UNKNON Sent.");


                        //recieve no of people
                        NoOfPhotos = Integer.parseInt(mBufferIn.readLine());
                        Log.d("receve", "no of people are:" + NoOfPhotos);

                        publishProgress(40);
                        //recieving file names.
                        int i = 1;
                        String PersonName = null;
                        PersonNames = new ArrayList<>();
                        while (i <= NoOfPhotos) {
                            PersonName = String.valueOf(mBufferIn.readLine());
                            try{
                                PersonName = PersonName.replace(".jpg","");
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("receve","replacing .jpg with empty char failed in app.");
                            }
                            PersonNames.add(PersonName);
                            i++;
                        }
                        Log.d("receve", "Names are:" + PersonNames);
                        Log.d("receve","No of Photos in PersonNameList is: "+ PersonName.length());
                        publishProgress(50);


                        //receving photo sizes.
                        int PersonPhotoSize = 0;
                        PersonPhotoSizes = new ArrayList<>();
                        i = 1;
                        while (i <= NoOfPhotos) {
                            PersonPhotoSize = Integer.parseInt(mBufferIn.readLine());
                            PersonPhotoSizes.add(PersonPhotoSize);
                            i++;
                        }
                        Log.d("receve", "Person photoSizes are:" + PersonPhotoSizes);
                        publishProgress(60);

                        printWriter.close();
                        mBufferIn.close();
                        skt.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                publishProgress(70);
                int i = 1;
                while (skt1 == null || i <= NoOfPhotos) {
                    try {
                        skt1 = new Socket(HOST, Port);
                        InputStream sin = skt1.getInputStream();
                        DataInputStream dis = new DataInputStream(sin);

                        byte[] data = new byte[PersonPhotoSizes.get(i - 1)];
                        try {
                            dis.readFully(data, 0, data.length);
                        } catch (IOException e) {
                            Log.d("receve", "Failed to retrieve image.");
                            e.printStackTrace();
                        }
                        publishProgress(80);
                        Log.d("receve", "Reading of Photo " + i + " is Completed.");
                        UnknownCardList.add(new Unknown_CardData(data, PersonNames.get(i - 1)));
                        Log.d("receve", "Saved Photo " + i + " to DB.");
                        i++;
                        dis.close();
                        skt1.close();
                    } catch (IOException e) {
                        System.out.println("Fail");
                        e.printStackTrace();
                    }
                }

        }
}

    private class FreeServer extends AsyncTask<Integer, Integer, Void> {
        Socket skt;
        PrintWriter printWriter;
        String ToastMessage;

        boolean Error = false;
        String ErrorMessage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setMax(100);
            progressBar.setProgress(0);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.d("status", "Progress is:" + values[0]);
            progressBar.setProgress(values[0]);
            if (Error)
                displayShortToast(ErrorMessage);
        }

        @Override
        protected Void doInBackground(Integer... Params) {
            try {
                FreeServerfun();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            displayShortToast(ToastMessage);
        }

        private void FreeServerfun() {
            while (skt == null) {
                try {
                    publishProgress(20);
                    skt = new Socket(HOST, Port);
                    printWriter = new PrintWriter(skt.getOutputStream());
                    BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(skt.getInputStream()));

                    //sending delimiter
                    Log.d("receve", "sending delimiter.");
                    printWriter.write("?FREESV");
                    printWriter.flush();
                    Log.d("receve", "FREESV Sent.");

                    publishProgress(80);

                    //recieving ACK
                    ToastMessage = mBufferIn.readLine();
                    Log.d("receve",ToastMessage);
                    publishProgress(100);

                    printWriter.close();
                    mBufferIn.close();
                    skt.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
            Toast.makeText(Unknown.this, ToastMessage, Toast.LENGTH_LONG).show();
        }

        private void displayShortToast(String ToastMessage) {
            Toast.makeText(Unknown.this, ToastMessage, Toast.LENGTH_SHORT).show();
        }

        ItemTouchHelper.SimpleCallback itemTouchSimpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                UnknownCardList.remove(viewHolder.getAdapterPosition());
                adapter.notifyDataSetChanged();
                }
            };
}