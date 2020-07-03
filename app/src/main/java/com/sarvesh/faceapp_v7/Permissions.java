package com.sarvesh.faceapp_v7;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Permission;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Permissions extends AppCompatActivity implements PermissionViewHolder.OnSyncListener {

    public String HOST = "serveousercontent.com";
    public int Port = 1998;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;

    //representing data
    private PermissionAdapter adapter;
    private RecyclerView recyclerView;
    List<CardData> list = new ArrayList<>();

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
        list = getData();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new PermissionAdapter(list, getApplicationContext(), this);
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
        {   displayLongToast("database is empty!");
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
        //send data to server
        SyncApp syncApp = new SyncApp();
        syncApp.execute(new Integer(position));
    }

    @Override
    public void onPermissionSwitch(int position) {
        Cursor Localdb = mDatabaseHandler.getData();
        Log.d("syncupdate","Old Permission was:"+ String.valueOf(Localdb.getInt(Localdb.getColumnIndex("STATUS"))));

        Localdb.moveToPosition(position);
        if(Localdb.getInt(Localdb.getInt(Localdb.getColumnIndex("STATUS"))) == 1)
            UpdateData(Localdb.getInt(Localdb.getColumnIndex("ID")),
                                false,
                                      Localdb.getBlob(Localdb.getColumnIndex("PHOTO")),
                                      Localdb.getString(Localdb.getColumnIndex("STATUS")));
        else
            UpdateData(Localdb.getInt(Localdb.getColumnIndex("ID")),
                    true,
                    Localdb.getBlob(Localdb.getColumnIndex("PHOTO")),
                    Localdb.getString(Localdb.getColumnIndex("STATUS")));

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
        Socket s8;
        PrintWriter pw8;

        @Override
        protected Void doInBackground(Integer... Params) {
            Integer param1 = Params[0];
            int mCardPosition = param1.intValue();

            SyncApp(mCardPosition);
            return null;
        }

        void SyncApp(int CardPosition)
        {
            while(s8 == null)
            {
                try {
                    s8 = new Socket(HOST, Port);
                    pw8 = new PrintWriter(s8.getOutputStream());
                    BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(s8.getInputStream()));


                    pw8.close();

                    String command = String.valueOf(mBufferIn.readLine());

                    s8.close();

                    Cursor Localdb = mDatabaseHandler.getData();
                    Localdb.moveToPosition(CardPosition);

                    if(Localdb.getInt(Localdb.getColumnIndex("STATUS")) == 1)//it means person is allowed
                        pw8.write("?UPDATE");


                    else if(Localdb.getInt(Localdb.getColumnIndex("STATUS")) == 0)//it means person is not allowed
                        pw8.write("?DELETE");
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

        boolean updateData = mDatabaseHandler.updateData(id, photoBlob, name, statusInt, this);

        if(updateData){
            displayLongToast("Data Successfully Updated Locally.");
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

    class send extends AsyncTask<Void, Void, Void> {
        Socket s23;
        PrintWriter pw23;
        Socket s1;
        PrintWriter pw1;
        Socket s2;
        PrintWriter pw47;
        Socket s4;
        Socket s5;
        Socket s90;
        PrintWriter pw90;

        Socket Sack;

        @Override
        protected Void doInBackground(Void... params) {
            //send name
            try {
                sendName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //send photo
            sendFile();
            receiveACK();
           /* try {
              recieveFile();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            return null;
        }

        void sendName() throws IOException {
            //prepration
            while(s23 == null)
            {
                try {
                    s23 = new Socket(HOST, Port);
                    pw23 = new PrintWriter(s23.getOutputStream());
                } catch (IOException e) {
                    System.out.println("Fail");
                    e.printStackTrace();
                }
                //preparing to send name_length
                byte[] nameBytes = name.getBytes();
                int nameBytesLength = nameBytes.length;//no of charaters in the name
                String nameBytesLengthString = Integer.toString(nameBytesLength);

                if (s23 != null)
                {
                    //send name delimeter
                    pw23.write("?NAME");
                    //send name_length
                    if (nameBytesLength < 100) {
                        if (nameBytesLength <= 9)
                            pw23.write('0' + nameBytesLengthString);
                        else
                            pw23.write(nameBytesLengthString);
                    }
                    pw23.write(name);
                    pw23.flush();
                    pw23.close();
                    s23.close();
                }
                else
                {
                    Log.d("errort","s23 is null!");
                }
            }
        }

       /* byte[] EncodeToUTF8(String string) {
                //encoding delimeter string to utf-8 encoding
                ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(string);
                byte[] buff = new byte[byteBuffer.remaining()];
                byteBuffer.get(buff, 0, buff.length);
                return buff;
        }*/

        void sendFile() {

            while(s1 == null)
            {
                try {
                    s1 = new Socket(HOST, Port);
                    pw1 = new PrintWriter(s1.getOutputStream());
                } catch (IOException e) {
                    System.out.println("Fail");
                    e.printStackTrace();
                }
                try {
                    //preparing bytearray of photo
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    InputStream inn = new ByteArrayInputStream(byteArray);

                    if (pw1 != null) {
                        //send photo delimeter
                        pw1.write("?IMAGE");

                        //sending bytearray_length or image_length
                        pw1.write(String.valueOf(byteArray.length) + '$');
                        Log.d("finally", String.valueOf(byteArray.length));
                        pw1.flush();
                        pw1.close();
                        s1.close();
                    }

                    //making 3rd connection with pyjnius for sending photo
                    try {
                        s2 = new Socket(HOST, Port);
                        pw47 = new PrintWriter(s2.getOutputStream());
                        DataOutputStream dos = new DataOutputStream(s2.getOutputStream());
                        if (s2 != null)
                        {
                            pw47.write("?image");
                            pw47.flush();

                            Log.d("photo", "writing image in stream............");
                            dos.write(byteArray, 0, byteArray.length);
                            Log.d("image", Arrays.toString(byteArray));
                            Log.d("photo", "photo was wrote in dos");

                            dos.flush();
                            pw47.close();
                            stream.close();
                            inn.close();
                            dos.close();
                            s2.close();
                        }
                    } catch (IOException e) {
                        System.out.println("Fail");
                        e.printStackTrace();
                    }
                }catch (IOException ioe) {
                    Log.d("Exception Caught", ioe.getMessage());
                }
            }
        }

        void receiveACK() {
            while (Sack == null) {
                try {
                    Sack = new Socket(HOST, Port);
                    BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(Sack.getInputStream()));
                    Log.d("try","send op code");

                    if (Sack != null)
                    {
                        String ACK = mBufferIn.readLine();
                        if (ACK != null)
                        {
                            Log.d("try","Read Line...");
                            if(ACK.equals("?ACK"))
                            {
                                Log.d("try","ACK recieved....");
                                //*********Display Toast using threads***************************************//
                                Register.MyAndroidThread myTask = new Register.MyAndroidThread(Register.this,String.valueOf(mBufferIn.readLine()));
                                Thread t1 = new Thread(myTask, "Sarvesh");
                                t1.start();

                                try {
                                    Thread.sleep(1);
                                    t1.interrupt();
                                    Thread.sleep(5);
                                }
                                catch (InterruptedException e) {
                                    System.out.println("Caught:" + e);
                                }
                                //****************************************************************************//
                                //enable navigationBar
                                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                                displayLongToast(String.valueOf(mBufferIn.readLine()));
                                break;
                            }
                        }
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void recieveFile() throws IOException {

            while(s4 == null)
            {
                try {
                    s4 = new Socket(HOST, Port);
                    BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(s4.getInputStream()));
                    OutputStream sout = s4.getOutputStream();

                    Boolean mRun = true;
                    if (s4 != null) {
                        while (mRun) {
                            String mServerMessage = mBufferIn.readLine();
                            if (mServerMessage != null) {
                                //receive name
                                if (mServerMessage.equals("?name")) {
                                    String name = String.valueOf(mBufferIn.readLine());
                                    Log.d("recna", "Name is: " + name);
                                    PersonName = name;
                                }
                            }
                            //Check if data is image and receive image
                            String mServerMessage1 = mBufferIn.readLine();
                            if (mServerMessage1.equals("?start")) {
                                // Get length of image byte array
                                int size = Integer.parseInt(mBufferIn.readLine());
                                Log.d("recna", "ImageSize is: " + size);

                            /*while (true)
                            {
                                Log.d("recna", "Start copying bytes to img_buffer...");

                                int bytes_read = dis.read(msg_buff, 0, msg_buff.length);
                                if (bytes_read == -1)
                                {
                                    break;
                                }
                                //copy bytes into img_buff
                                System.arraycopy(msg_buff, 0, img_buff, img_offset, bytes_read);
                                img_offset += bytes_read;
                                if (img_offset >= size)
                                {
                                    break;
                                }
                                Log.d("recna", "End copying bytes to img_buffer...");
                            }*/
                                //create file_storage path
                            /*File myDir = new File(getApplicationContext().getFilesDir(),"FaceApp"+File.separator+"Images");
                            Log.d("recna", "FileDir is: " + getApplicationContext().getFilesDir());*/ //let me commit

                                File myDir = new File(Environment.getExternalStorageDirectory() + "/DCIM");
                                if (!myDir.exists()) {
                                    myDir.mkdirs();
                                    Log.d("recna", "Directory not found!");
                                    Log.d("recna", "Making Directory...");
                                }

                                //save images
                                String fileName = PersonName + ".jpeg";
                                File imageFile = new File(myDir, fileName);
                                Log.d("recna", "Making File at Directory...");

                                byte[] data = new byte[size];
                                String mServerMessage2 = mBufferIn.readLine();
                                if (mServerMessage2.equals("?imageFile")) {
                                    s4.close();
                                    while(s5 == null)
                                    {
                                        try {
                                            s5 = new Socket(HOST, Port);
                                        } catch (IOException e) {
                                            System.out.println("Fail");
                                            e.printStackTrace();
                                        }

                                        InputStream sinn = s5.getInputStream();
                                        DataInputStream diss = new DataInputStream(sinn);

                                        Log.d("recna", "Trying new method");
                                        //String data = mBufferIn.readLine();
                                        diss.readFully(data, 0, data.length);
                                        Log.d("recna", "Read Successfully.");

                                        FileOutputStream out = new FileOutputStream(imageFile);
                                        Bitmap data_bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        data_bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                                        out.flush();
                                        out.close();
                                        s5.close();
                                    }
                                }
                            }
                        }
                        mRun = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}