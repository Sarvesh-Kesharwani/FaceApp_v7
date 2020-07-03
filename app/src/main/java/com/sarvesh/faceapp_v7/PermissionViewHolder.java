package com.sarvesh.faceapp_v7;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class PermissionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    ImageView PersonPhotoImageView;
    TextView PersonNameTextView;
    Switch PersonPermissionStatusSwitch;
    ImageButton PermissionSyncButton;

    OnSyncListener onSyncListener;

    PermissionViewHolder(View itemView, OnSyncListener onSyncListener)
    {
        super(itemView);
        PersonPhotoImageView = itemView.findViewById(R.id.Person_image);
        PersonNameTextView = itemView.findViewById(R.id.Person_name);
        PersonPermissionStatusSwitch = itemView.findViewById(R.id.Permission_Status_switch);
        PermissionSyncButton = itemView.findViewById(R.id.Permission_Data_Synced);
        this.onSyncListener = onSyncListener;

        PermissionSyncButton.setOnClickListener(this);
        PersonPermissionStatusSwitch.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        onSyncListener.onSyncClick(getAdapterPosition());
        onSyncListener.onPermissionSwitch(getAdapterPosition());
    }

    public interface OnSyncListener
    {
        void onSyncClick(int position);
        void onPermissionSwitch(int position);
    }
}