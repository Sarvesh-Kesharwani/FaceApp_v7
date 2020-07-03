package com.sarvesh.faceapp_v7;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class PermissionViewHolder extends RecyclerView.ViewHolder{

    ImageView PersonPhotoImageView;
    TextView PersonNameTextView;
    Switch PersonPermissionStatusSwitch;
    ImageButton PermissionSyncButton;

    PermissionViewHolder(View itemView, final RecyclerViewClickInterface recyclerViewClickInterface)
    {
        super(itemView);
        PersonPhotoImageView = itemView.findViewById(R.id.Person_image);
        PersonNameTextView = itemView.findViewById(R.id.Person_name);
        PersonPermissionStatusSwitch = itemView.findViewById(R.id.Permission_Status_switch);
        PermissionSyncButton = itemView.findViewById(R.id.Permission_Data_Synced);

        PermissionSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewClickInterface.onSyncClick(getAdapterPosition());
            }
        });

        PersonPermissionStatusSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewClickInterface.onPermissionSwitch(getAdapterPosition());
            }
        });
    }


}