package com.sarvesh.faceapp_v7;

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
    private String VehicleNumber;
    private Button CloseVehicleActivityButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_vehicle);

        VehicleNumberTextView = findViewById(R.id.VehicleNumberEditTextView);
        AddVehicleNumberButton = findViewById(R.id.AddVehicleNumberButton);

        AddVehicleNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(VehicleNumberTextView.getText().equals("")))
                {
                    if(VehicleNumberTextView.getText().length() < 15)
                    {
                        VehicleNumber = VehicleNumberTextView.getText().toString();
                        VehicleNumberTextView.setText("");//reset text
                        Toast.makeText(RegisterVehicle.this,"Vehicle Was Added Locally.",Toast.LENGTH_SHORT).show();
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
}
