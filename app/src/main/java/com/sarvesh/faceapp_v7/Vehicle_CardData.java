package com.sarvesh.faceapp_v7;

public class Vehicle_CardData {

    String number;
    String name;
    boolean vehicleStatus;
    int vehicleSynced;

    Vehicle_CardData(String number, String name,boolean vehicleStatus, int vehicleSynced)
    {
        this.name = name;
        this.number = number;
        this.vehicleStatus = vehicleStatus;
        this.vehicleSynced = vehicleSynced;
    }
}
