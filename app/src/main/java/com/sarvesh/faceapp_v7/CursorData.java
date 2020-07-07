package com.sarvesh.faceapp_v7;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class CursorData {
    Cursor cursor;
    List<Integer> ListOfPhotoIDs;
    List<byte[]> ListOfPhotoByteArrays;


    public CursorData(Cursor cursor, List<byte[]> ListOfPhotoByteArrays, List<Integer> ListOfPhotoIDs)
    {
         this.cursor = cursor;
        this.ListOfPhotoByteArrays = ListOfPhotoByteArrays;
                this.ListOfPhotoIDs = ListOfPhotoIDs;
    }
}
