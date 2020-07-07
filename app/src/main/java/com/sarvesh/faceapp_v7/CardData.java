package com.sarvesh.faceapp_v7;

public class CardData {

    byte[] PersonPhoto;
    String PersonName;
    boolean PersonPermissionStatus;
    int PermissionDataSynced;

    CardData(byte[] personPhoto, String personName, boolean personPermissionStatus, int PermissionDataSynced)
    {
        this.PersonPhoto = personPhoto;
        this.PersonName = personName;
        this.PersonPermissionStatus = personPermissionStatus;
        this.PermissionDataSynced = PermissionDataSynced;
    }
}