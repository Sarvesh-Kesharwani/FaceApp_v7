// ViewHolder code for RecyclerView 
package com.sarvesh.faceapp_v7;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.sarvesh.faceapp_v7.R;

public class PermissionViewHolder extends RecyclerView.ViewHolder {

    TextView PersonPhoto;
    TextView PersonName;
    TextView PersonPermissionStatus;

    PermissionViewHolder(View itemView)
    {
        super(itemView);
        PersonPhoto = itemView.findViewById(R.id.Person_image);
        PersonName = itemView.findViewById(R.id.Person_name);
        PersonPermissionStatus = itemView.findViewById(R.id.Permission_Sync_button);
    }
} 