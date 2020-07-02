package com.sarvesh.faceapp_v7;

import android.graphics.Bitmap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;

public class CardData {

    Bitmap PersonPhoto;
    String PersonName;
    boolean PersonPermissionStatus;
    boolean PermissionDataSynced;

    CardData(Bitmap personPhoto, String personName, boolean personPermissionStatus, boolean PermissionDataSynced)
    {
        this.PersonPhoto = personPhoto;
        this.PersonName = personName;
        this.PersonPermissionStatus = personPermissionStatus;
        this.PermissionDataSynced = PermissionDataSynced;
    }
}