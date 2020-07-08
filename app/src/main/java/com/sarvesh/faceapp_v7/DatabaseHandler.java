package com.sarvesh.faceapp_v7;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.Nullable;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;


public class DatabaseHandler extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "RpiDb.db";
    private static final String TABLE_NAME = "members_data";

    private static final String Col_1 = "ID";
    private static final String Col_2 = "NAME";
    private static final String Col_3 = "PHOTO";
    private static final String Col_4 = "STATUS";
    private static final String Col_5 = "SYNCED";

    public DatabaseHandler(@Nullable Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.d("receve","database created.");
        db.execSQL("create table " + TABLE_NAME + " ( "+ Col_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Col_2 + " TEXT, " + Col_3 + " TEXT, " + Col_4 + " INTEGER ," + Col_5 + " INTEGER )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1)
    {
        Log.d("receve","database not found and created.");
        db.execSQL(" DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(Uri uri, String name, int status, int synced, Context context)
    {
        long result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        try{
            //prepare file to write on.
            ContextWrapper cw = new ContextWrapper(context);//context..getApplicationContext()
            File directory = cw.getDir("imageDatabase", Context.MODE_PRIVATE);
            FileOutputStream fos = new FileOutputStream(new File(directory, name + ".png"));

            //read bytes from photo_path file and save it to bytes array.
            BufferedInputStream buf = new BufferedInputStream(context.getContentResolver().openInputStream(uri));
            byte[] bytes = new byte[1048576];//5mb, it means max 5mb image can be read only
            int ReadBytesSize = 0;
            int temp = 0;
            while(true)
            {
                if((temp = buf.read(bytes)) == -1)
                {
                    break;
                }
                else
                {
                    ReadBytesSize = temp;
                }
            }
            buf.close();
            byte[] PhotoBytes = new byte[ReadBytesSize];
            PhotoBytes = Arrays.copyOfRange(bytes, 0, ReadBytesSize);

            //save read bytes to app'local file
            fos.write(PhotoBytes, 0, PhotoBytes.length);
            fos.close();

            //store localFile's path to database.
            contentValues.put(Col_3, directory.getAbsolutePath() + "/" + name + ".png");
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        }

        contentValues.put(Col_2,name);
        contentValues.put(Col_4,status);
        contentValues.put(Col_5,synced);
        result = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1)
        {
            Log.d("mdatabase","result is -1");
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean addServerData(byte[] photo, String name, int status, int synced,Context context)
    {
        long result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDatabase", Context.MODE_PRIVATE);
        // Create imageDir
        File photoFile = new File(directory,name + ".png");

        if (photoFile.exists()) {
            photoFile.delete();
        }
        try {
            FileOutputStream fos = new FileOutputStream(photoFile);
            fos.write(photo);
            fos.close();
        }
        catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }

        contentValues.put(Col_3,photoFile.getPath());
        //contentValues.put(Col_3,photo);
        contentValues.put(Col_2,name);
        contentValues.put(Col_4,status);
        contentValues.put(Col_5,synced);
        result = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1)
        {
            Log.d("mdatabase","result is -1");
            return false;
        }
        else
        {
            return true;
        }

    }

    public boolean updateData(int id, String photoBlob, String name, int status, int synced)
    {
        long result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Col_1,id);
        contentValues.put(Col_2,name);
        contentValues.put(Col_3,photoBlob);
        contentValues.put(Col_4,status);
        contentValues.put(Col_5,synced);
        result = db.update(TABLE_NAME, contentValues, "ID = ?", new String[] { String.valueOf(id) });

        if(result == -1)
        {
            Log.d("mdatabase","result is -1");
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean deleteData(int id)
    {
        long result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        result = db.delete(TABLE_NAME,"ID = ?", new String[] { String.valueOf(id) });

        if(result == -1)
        { Log.d("mdatabase","result is -1");
            return false;}
        else
        { return true;}
    }

    public Cursor getData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

}
