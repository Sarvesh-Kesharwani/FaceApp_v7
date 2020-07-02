package com.sarvesh.faceapp_v7;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Register extends AppCompatActivity {
    //////////////////////////////////////////////////
    public String HOST = "serveousercontent.com";//RPI3 eth0 ip 192.168.0.100  //2.tcp.ngrok.io
    public int Port = 1998;
    public int SELECT_PHOTO = 1;
    public Uri uri;
    public ImageView photoImage;

    // data received by input
    public static Bitmap photoBitmap;
    public static String name;

    public String PersonName;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;
    //////////////////////////////////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ///////////////////////////////////////////////
        setContentView(R.layout.activity_register);
        toolbar = findViewById(R.id.register_toolBar);
        navigationView = findViewById(R.id.register_navigation_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText nameText = findViewById(R.id.nameText);
        final Button sendButton = findViewById(R.id.sendButton);
        photoImage = findViewById(R.id.poi);
        sendButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if(photoBitmap != null)
                {
                    //disable navigationBar
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    send sendcode = new send();
                    name = nameText.getText().toString();
                    sendcode.execute();
                }
                else
                    displayToast("Select Photo!");

            }
        });

        final Button uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        uploadPhotoButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent (Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,SELECT_PHOTO);
            }
        });

        ///////////////////////////////////////////////

        //Navigation Coding Start
        drawerLayout = findViewById(R.id.register_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(Register.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = findViewById(R.id.register_navigation_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId())
                {
                    case R.id.nav_register:
                        Intent intent1 = new Intent(Register.this, Register.class);
                        startActivity(intent1);
                        ReadyAppend readyAppend = new ReadyAppend();
                        readyAppend.execute();
                        break;
                    case R.id.nav_permissions:
                        Intent intent2= new Intent(Register.this, Permissions.class);
                        startActivity(intent2);
                        break;
                    case R.id.nav_unknown_activity:
                        Intent intent3 = new Intent(Register.this, Unknown.class);
                        startActivity(intent3);
                        break;
                    case R.id.nav_emergency_toggle:
                        Intent intent4 = new Intent(Register.this, Emergency.class);
                        startActivity(intent4);
                        break;
                    }
                    return false;
                }
            });

    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }


    public static Bitmap getCorrectlyOrientedImage(Context context, Uri photoUri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }
        int MAX_IMAGE_WIDTH = 720;
        int MAX_IMAGE_HEIGHT = 1280;
        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > MAX_IMAGE_WIDTH || rotatedHeight > MAX_IMAGE_HEIGHT) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_WIDTH);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_HEIGHT);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        return srcBitmap;
    }

    class MyAndroidThread implements Runnable
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

    class ReadyAppend extends AsyncTask<Void, Void, Void>
    {
        Socket s8;
        PrintWriter pw8;

        @Override
        protected Void doInBackground(Void... voids) {
            ReadyAppend();
            return null;
        }


        void ReadyAppend()
        {
            while(s8 == null)
            {
                try {
                    s8 = new Socket(HOST, Port);
                    pw8 = new PrintWriter(s8.getOutputStream());
                    BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(s8.getInputStream()));
                    pw8.write("?OPE");
                    pw8.write("1");
                    pw8.flush();

                    String command = String.valueOf(mBufferIn.readLine());
                    Log.d("uh","Commandi: "+command);

                    //*********Display Toast using threads***************************************//
                    MyAndroidThread myTask = new MyAndroidThread(Register.this,command);
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

                    pw8.close();
                    s8.close();
                } catch (IOException e) {
                    System.out.println("Fail");
                    e.printStackTrace();
                }
            }
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


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data.getData() != null){
            uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                if(bitmap != null)
                {
                    Bitmap RotatedBitmap = getCorrectlyOrientedImage(Register.this,uri);
                    photoImage.setImageBitmap(RotatedBitmap);
                    Log.d("recna", "Original PhotoBitmap is:" + bitmap);

                    photoBitmap = RotatedBitmap;
                }
                else
                {
                    //int id = getResources().getIdentifier("com.example.faceapp_java_24/"+StringGenerated,null,null);
                    displayToast("Select Photo!");
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayToast(String ToastMessage)
    {
        Toast.makeText(Register.this,ToastMessage,Toast.LENGTH_SHORT).show();
    }
    private void displayLongToast(String ToastMessage)
    {
        Toast.makeText(Register.this,ToastMessage,Toast.LENGTH_LONG).show();
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
                                MyAndroidThread myTask = new MyAndroidThread(Register.this,String.valueOf(mBufferIn.readLine()));
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
