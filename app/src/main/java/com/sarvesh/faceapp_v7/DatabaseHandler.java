package com.sarvesh.faceapp_v7;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ListAdapter;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;


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
        db.execSQL("create table " + TABLE_NAME + " ( "+ Col_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Col_2 + " TEXT, " + Col_3 + " BLOB, " + Col_4 + " INTEGER ," + Col_5 + " INTEGER )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1)
    {
        db.execSQL(" DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public boolean addData(Uri photo_path, String name, int status, int synced, Context context)
    {
        long result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        try{

            Log.d("mdatabase","uri path is:"+photo_path);

            InputStream iStream =   context.getContentResolver().openInputStream(photo_path);
            byte[] imgbyte = getBytes(iStream);

            ContentValues contentValues = new ContentValues();
            contentValues.put(Col_3,imgbyte);

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

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("mdatabase","fileIO error");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("mdatabase","io error");
            return false;
        }
    }

    public boolean addServerData(byte[] photo, String name, int status, int synced)
    {
        long result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        Bitmap tempBitmap = BitmapFactory.decodeByteArray(photo,0,photo.length);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        tempBitmap.compress(Bitmap.CompressFormat.PNG,50,buffer);

        contentValues.put(Col_3,buffer.toByteArray());
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

    public boolean updateData(int id, byte[] photoBlob, String name, int status, int synced)
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
