package com.sarvesh.faceapp_v7;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.DiscretePathEffect;
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
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Register extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;
    public ImageView photoImage;
    public int SELECT_PHOTO = 1;

    //received user data.
    public String name;
    public Uri uri;

    //database handler
    DatabaseHandler mDatabaseHandler;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Local Database
        mDatabaseHandler = new DatabaseHandler(this);

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
                if(photoImage.getDrawable() != null)
                {
                    if(!(nameText.getText().equals("")))
                    {
                        if(nameText.getText().length() < 100)
                        {
                            //disable navigationBar
                            //drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                            name = nameText.getText().toString();
                            nameText.setText("");//reset text
                            photoImage.setImageResource(R.drawable.avatar);//reset imageview

                            //save person data to localDB
                            if(uri == null)
                                Log.d("uri","uri is null!!!!!!");
                            AddData(uri,name,true, 0,getApplicationContext());
                        }
                        else
                        {displayToast("Name is too long!");}
                    }
                    else
                    {displayToast("Enter Name!");}
                }
                else
                {displayToast("Select Photo!");}

            }
        });

        final Button uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        uploadPhotoButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_PHOTO);
            }
        });

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

            //this try catch toasts "Select Photo!" if user taps upload photo but selects nothing.
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                if(bitmap != null)
                {
                    Bitmap RotatedBitmap = getCorrectlyOrientedImage(Register.this,uri);
                    photoImage.setImageBitmap(RotatedBitmap);
                }
                else
                {
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

    public void AddData(Uri photo_uri, String name, boolean status, int synced, Context context)
    {
        int statusInt;
        if(status)
            statusInt = 1;
        else
            statusInt = 0;

        boolean insertData = mDatabaseHandler.addData(photo_uri, name, statusInt, synced,context);

        if(insertData){
            displayLongToast("Data Successfully Inserted Locally.");
        }
        else{
            displayLongToast("Something went wrong with Local DB!");
        }
    }
}
