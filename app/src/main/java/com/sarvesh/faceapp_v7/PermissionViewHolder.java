// ViewHolder code for RecyclerView 
package com.sarvesh.faceapp_v7;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.sarvesh.faceapp_v7.R;

public class PermissionViewHolder extends RecyclerView.ViewHolder {
    TextView examName;
    TextView examMessage;
    TextView examDate;

    PermissionViewHolder(View itemView)
    {
        super(itemView);
        examName
                = (TextView)itemView
                .findViewById(R.id.examName);
        examDate
                = (TextView)itemView
                .findViewById(R.id.examDate);
        examMessage
                = (TextView)itemView
                .findViewById(R.id.examMessage);
    }
} 