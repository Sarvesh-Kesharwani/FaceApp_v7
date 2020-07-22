package com.sarvesh.faceapp_v7;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FullScreenView extends AppCompatActivity {

    Button Close;
    Button Save;
    ImageView FullscreenPhoto;
    Bitmap photo_bitmap;
    String ImageName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen);

        FullscreenPhoto = findViewById(R.id.fullscreen_imageView3);
        Close = findViewById(R.id.close_fullscreen);
        Save = findViewById(R.id.save_image_button);

        Intent data = getIntent();
        Log.d("full","starting ......");
        byte[] selectedImage = data.getExtras().getByteArray("FullImage");
        ImageName = data.getExtras().getString("FullImageName");
        photo_bitmap = BitmapFactory.decodeByteArray(selectedImage,0,selectedImage.length);
        FullscreenPhoto.setImageBitmap(photo_bitmap);
        Log.d("full","Completed.");

        Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaStore.Images.Media.insertImage(getContentResolver(), photo_bitmap, ImageName , null);
                Toast.makeText(FullScreenView.this, "Photo Saved to Gallery Successfully.", Toast.LENGTH_SHORT).show();
                Log.d("full","Image Saved to Gallary.");
            }
        });


    }

}
