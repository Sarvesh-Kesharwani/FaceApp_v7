package com.sarvesh.faceapp_v7;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterVehicle extends AppCompatActivity {

    public EditText VehicleNumberTextView;
    public Button AddVehicleNumberButton;

    private EditText VehicleNameEditTextView;
    private Button CloseVehicleActivityButton;

    private String VehicleNumber;
    private String VehicleName;

    private VehicleDatabaseHandler mDatabaseHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_vehicle);

        mDatabaseHandler = new VehicleDatabaseHandler(this);
        VehicleNameEditTextView = findViewById(R.id.VehicleNameEditTextView);
        VehicleNumberTextView = findViewById(R.id.VehicleNumberEditTextView);
        AddVehicleNumberButton = findViewById(R.id.AddVehicleNumberButton);

        AddVehicleNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(VehicleNumberTextView.getText().toString().equals("")))
                {
                    if(!(VehicleNameEditTextView.getText().toString().equals("")))
                    {
                        if(VehicleNumberTextView.getText().length() < 15) {
                            if (VehicleNameEditTextView.getText().length() < 100) {

                                VehicleNumber = VehicleNumberTextView.getText().toString();
                                VehicleName = VehicleNameEditTextView.getText().toString();
                                AddData(VehicleNumber, VehicleName, true, 0, RegisterVehicle.this);

                                VehicleNumberTextView.setText("");//reset text
                                VehicleNameEditTextView.setText("");//reset text

                                Toast.makeText(RegisterVehicle.this, "Vehicle Was Added Locally.", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(RegisterVehicle.this,"Vehicle Name is too large...",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(RegisterVehicle.this,"Vehicle Number is too large...",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(RegisterVehicle.this,"Please Enter Vehicle Name.",Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(RegisterVehicle.this,"Please Enter Vehicle Number.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        CloseVehicleActivityButton = findViewById(R.id.Close_AddVehicleButton);
        CloseVehicleActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void displayToast(String ToastMessage)
    {
        Toast.makeText(RegisterVehicle.this,ToastMessage,Toast.LENGTH_SHORT).show();
    }
    private void displayLongToast(String ToastMessage)
    {
        Toast.makeText(RegisterVehicle.this,ToastMessage,Toast.LENGTH_LONG).show();
    }

    public void AddData(String number, String name, boolean status, int synced, Context context)
    {
        int statusInt;
        if(status)
            statusInt = 1;
        else
            statusInt = 0;

        boolean insertData = mDatabaseHandler.addData(number, name, statusInt, synced,context);

        if(insertData){
            displayLongToast("Data Successfully Inserted Locally.");
        }
        else{
            displayLongToast("Something went wrong with Local DB!");
        }
    }
}
