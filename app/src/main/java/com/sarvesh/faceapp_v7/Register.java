package com.sarvesh.faceapp_v7;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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

    //defines is user admin or else
    public static int UserID;

    ImageView NavigationDrawerPhoto;
    //database handler
    DatabaseHandler mDatabaseHandler;

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int value = extras.getInt("LoginProfileKey");
            if(value == 0)
            {
                UserID = 0;
                Log.d("receve", "Value is: "+String.valueOf(value));
            }
            else
            {
                UserID = 1;
                Log.d("receve", "Value is: "+String.valueOf(value));
            }
        }

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

                            AddData(uri, name,true, 0, getApplicationContext());
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
                checkPermission(Manifest.permission.CAMERA,
                        CAMERA_PERMISSION_CODE);
                /*Intent intent = new Intent(MediaStore.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_PHOTO);*/
                selectImage(Register.this);
            }
        });

        //Navigation Coding Start
        drawerLayout = findViewById(R.id.register_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(Register.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = findViewById(R.id.register_navigation_view);
        //setting navigation_header
        if(UserID == 1)
            {View navView = navigationView.inflateHeaderView(R.layout.navigation_header_admin);}

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        //////
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
                    case R.id.nav_logout:
                        editSharedPref();
                        Intent intent5 = new Intent(Register.this, MainActivity.class);
                        startActivity(intent5);
                        break;
                    }
                    return false;
                }
            });

        //Ask for camera permissions
        checkPermission(Manifest.permission.CAMERA,
                CAMERA_PERMISSION_CODE);
    }

    public void editSharedPref()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPreference", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedInKey", false);
        editor.apply();
    }

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(Register.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(Register.this,
                    new String[] { permission },
                    requestCode);
        }
        else {
            Toast.makeText(Register.this,
                    "Permission already granted",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(Register.this,
                        "Camera Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(Register.this,
                        "Camera Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
        else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(Register.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
                Log.d("receve","Storage Permission Granted");

            }
            else {
                Toast.makeText(Register.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
                Log.d("receve","Storage Permission Denied");

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_CANCELED)
        {
            if(data.getData() != null)
                uri = data.getData();
            else
                Log.d("photo","uri is null");

            switch (requestCode) {
                case 0:
                    Log.d("photo","camera");
                    if (resultCode == RESULT_OK && data != null)
                    {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        photoImage.setImageBitmap(selectedImage);

                        uri = getImageUri(getApplicationContext(), selectedImage);
                    }
                    break;
                case 1:
                    Log.d("photo","gallery");
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
                    break;
            }
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage,null,null);
        return Uri.parse(path);
    }

    private void selectImage(Context context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK);
                    pickPhoto.setType("image/*");
                    startActivityForResult(pickPhoto , 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
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

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data.getData() != null)
        {
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
    }*/

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
