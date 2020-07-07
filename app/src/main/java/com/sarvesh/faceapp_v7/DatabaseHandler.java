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
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


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

    SQLiteConnection sqLiteConnection=null;
    SQLiteStatement sqLiteStatement=null;


    public DatabaseHandler(@Nullable Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.d("receve","database created.");
        db.execSQL("create table " + TABLE_NAME + " ( "+ Col_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Col_2 + " TEXT, " + Col_3 + " BLOB, " + Col_4 + " INTEGER ," + Col_5 + " INTEGER )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1)
    {
        Log.d("receve","database not found and created.");
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

        /*Bitmap tempBitmap = BitmapFactory.decodeByteArray(photo,0,photo.length);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        tempBitmap.compress(Bitmap.CompressFormat.PNG,50,buffer);
        contentValues.put(Col_3,buffer.toByteArray());

        try{buffer.close();} catch (IOException e) {
            e.printStackTrace();
        }*/
        contentValues.put(Col_3,photo);
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

    public CursorData getNewData()
    {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT "+ Col_1 + ", length(PHOTO) FROM " + TABLE_NAME + " WHERE  length(PHOTO) > 1000000 ";
        Cursor TargetedRows = db.rawQuery(query, null);
        int TargetRowId;
        int TargetBlobSize;

        List<byte[]> ListOfPhotoByteArrays = new ArrayList<>();
        List<Integer> ListOfPhotoIDs = new ArrayList<>();

        while(TargetedRows.moveToNext())
        {
            TargetRowId = TargetedRows.getInt(TargetedRows.getColumnIndex("ID"));
            TargetBlobSize = TargetedRows.getInt(1);
            int ReadBlobSize = 0;
            byte[] OnePhotoBytes = new byte[TargetBlobSize];

            while(ReadBlobSize < TargetBlobSize)
            {
                String bringBlob = "SELECT substr(" + Col_3 + ", 1, " + Math.min(1000000, TargetBlobSize-ReadBlobSize) + ") FROM " + TABLE_NAME + " WHERE "+  Col_1+ " = " + TargetRowId;
                Cursor temp_Cursor = db.rawQuery(bringBlob,null);
                String BlobPartString = temp_Cursor.getString(temp_Cursor.getColumnIndexOrThrow("substr(PHOTO, 1, 1000000)")+1);
                temp_Cursor.close();
                ReadBlobSize += BlobPartString.length();
                byte[] tempBytes = new byte[BlobPartString.length()];
                tempBytes = BlobPartString.getBytes();
                System.arraycopy(tempBytes, 0, OnePhotoBytes, 0, tempBytes.length);
            }
            ListOfPhotoByteArrays.add(OnePhotoBytes);
            ListOfPhotoIDs.add(new Integer(TargetRowId));
        }


        String query1 = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query1, null);
        //return data;
        return new CursorData(data, ListOfPhotoByteArrays, ListOfPhotoIDs);
    }

    public Cursor getData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public byte[] get4Data(int id, Context context) {
        try
        {
            File databaseFile = context.getDatabasePath("RpiDb.db");
            sqLiteConnection=new SQLiteConnection(databaseFile);
            sqLiteConnection.open();
            sqLiteStatement=sqLiteConnection.prepare("SELECT PHOTO FROM members_data WHERE id="+id);
            //sqLiteStatement.bind(1, id);
            sqLiteStatement.step();

            return sqLiteStatement.columnBlob(0);

        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally
        {
            if(sqLiteStatement!=null)
                sqLiteStatement.dispose();
            if(sqLiteConnection!=null)
                sqLiteConnection.dispose();
        }

        return null;
    }


    public Cursor getCursorExceptBlob()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT ID , NAME , STATUS , SYNCED  FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }
}
