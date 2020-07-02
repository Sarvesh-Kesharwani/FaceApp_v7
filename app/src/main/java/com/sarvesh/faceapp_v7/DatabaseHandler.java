package com.sarvesh.faceapp_v7;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class DatabaseHandler extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "RpiDb.db";
    private static final String TABLE_NAME = "members_data";

    private static final String Col_1 = "ID";
    private static final String Col_2 = "NAME";
    private static final String Col_3 = "SURNAME";
    private static final String Col_4 = "PHOTO";
    private static final String Col_5 = "STATUS";

    public DatabaseHandler(@Nullable Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table " + TABLE_NAME + " ( "+ Col_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Col_2 + " TEXT, " + Col_3 + " TEXT, " + Col_4 + " TEXT, " + Col_5 + " TEXT) " );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1)
    {
        db.execSQL(" DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String name, String surname, boolean status, String photo_path)
    {
        long result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            FileInputStream fs = new FileInputStream(photo_path);
            byte [] imgbyte = new byte[fs.available()];
            fs.read(imgbyte);

            ContentValues contentValues = new ContentValues();
            contentValues.put(Col_4,imgbyte);

            contentValues.put(Col_2,name);
            contentValues.put(Col_3,surname);
            contentValues.put(Col_5,status);
            result = db.insert(TABLE_NAME, null, contentValues);
            fs.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if(result == -1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
