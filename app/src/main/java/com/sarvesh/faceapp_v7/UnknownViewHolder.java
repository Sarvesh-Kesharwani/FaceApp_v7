package com.sarvesh.faceapp_v7;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class UnknownViewHolder extends RecyclerView.ViewHolder{

    ImageView PersonPhotoImageView;
    TextView PersonNameTextView;

    UnknownViewHolder(View itemView, final UnknownRecyclerViewClickInterface recyclerViewClickInterface)
    {
        super(itemView);
        PersonPhotoImageView = itemView.findViewById(R.id.Person_image);
        PersonNameTextView = itemView.findViewById(R.id.Person_name);
    }
}