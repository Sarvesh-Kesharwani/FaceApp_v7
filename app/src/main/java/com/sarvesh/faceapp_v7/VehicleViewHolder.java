package com.sarvesh.faceapp_v7;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class VehicleViewHolder extends RecyclerView.ViewHolder{

    TextView VehicleNumberTextView;
    TextView VehicleNameTextView;
    Switch VehiclePermissionStatusSwitch;
    ImageButton VehicleSyncButton;

    VehicleViewHolder(View itemView, final VehicleRecyclerViewClickinterface recyclerViewClickInterface)
    {
        super(itemView);
        VehicleNumberTextView = itemView.findViewById(R.id.VehicleImageView);
        VehicleNameTextView = itemView.findViewById(R.id.VehicleNameTextView);
        VehiclePermissionStatusSwitch = itemView.findViewById(R.id.Vehicle_Status_switch);
        VehicleSyncButton = itemView.findViewById(R.id.Vehicle_Data_Synced);

        VehicleSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewClickInterface.onVehicleSyncClick(getAdapterPosition());
            }
        });

        VehiclePermissionStatusSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewClickInterface.onVehiclePermissionSwitch(getAdapterPosition());
            }
        });
    }

}