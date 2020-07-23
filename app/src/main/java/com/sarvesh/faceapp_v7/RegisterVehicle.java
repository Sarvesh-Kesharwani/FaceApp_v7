package com.sarvesh.faceapp_v7;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterVehicle extends AppCompatActivity {

    public EditText VehicleNumberTextView;
    public Button AddVehicleNumberButton;
    private String VehicleNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_vehicle);

        VehicleNumberTextView = findViewById(R.id.VehicleNumberEditTextView);
        AddVehicleNumberButton = findViewById(R.id.AddVehicleNumberButton);

        if(!(VehicleNumberTextView.getText().equals("")))
        {
            if(VehicleNumberTextView.getText().length() < 15)
            {
                VehicleNumber = VehicleNumberTextView.getText().toString();
                VehicleNumberTextView.setText("");//reset text
            }
        }
    }
}
