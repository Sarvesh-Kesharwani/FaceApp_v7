package com.sarvesh.faceapp_v7;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class UnknownViewHolder extends RecyclerView.ViewHolder{

    ImageView CapturedImage_ImageView;
    TextView Time_TextView;

    UnknownViewHolder(View itemView)
    {
        super(itemView);
        CapturedImage_ImageView = itemView.findViewById(R.id.captured_unknown_person);
        Time_TextView = itemView.findViewById(R.id.time_of_capture);
    }
}