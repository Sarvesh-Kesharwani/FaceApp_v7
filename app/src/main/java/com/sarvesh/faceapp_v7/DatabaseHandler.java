package com.sarvesh.faceapp_v7;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class DatabaseHandler extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "RpiDb.db";
    private static final String TABLE_NAME = "members_data";

    private static final String Col_1 = "ID";
    private static final String Col_2 = "NAME";
    private static final String Col_3 = "PHOTO";
    private static final String Col_4 = "STATUS";

    public DatabaseHandler(@Nullable Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table " + TABLE_NAME + " ( "+ Col_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Col_2 + " TEXT, " + Col_3 + " BLOB, " + Col_4 + " INTEGER )");
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

    public boolean addData(Uri photo_path, String name, int status,Context context)
    {
        long result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        try{

            Log.d("mdatabase","uri path is:"+photo_path);

            /*FileInputStream fs = new FileInputStream(photo_path);
            byte [] imgbyte = new byte[fs.available()];
            fs.read(imgbyte);*/

            InputStream iStream =   context.getContentResolver().openInputStream(photo_path);
            byte[] imgbyte = getBytes(iStream);

            ContentValues contentValues = new ContentValues();
            contentValues.put(Col_3,imgbyte);

            contentValues.put(Col_2,name);
            contentValues.put(Col_4,status);
            result = db.insert(TABLE_NAME, null, contentValues);
            //fs.close();

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

    public Cursor getData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

}
