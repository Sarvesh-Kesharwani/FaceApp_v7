package com.sarvesh.faceapp_v7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class PermissionAdapter extends RecyclerView.Adapter<PermissionViewHolder> {

    List<CardData> list;
    Context context;
    public RecyclerViewClickInterface mOnSyncListener;

    public PermissionAdapter(List<CardData> list, Context context, RecyclerViewClickInterface mOnSyncListener)
    {
        this.list = list;
        this.context = context;
        this.mOnSyncListener = mOnSyncListener;
    }

    @Override
    public PermissionViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the layout
        View photoView = inflater.inflate(R.layout.permission_card,parent, false);//converted xml file into a view buy inflating it.

        PermissionViewHolder viewHolder = new PermissionViewHolder(photoView, mOnSyncListener);//giving inflated view to viewHolder
        return viewHolder;
    }

    //now my viewHolder will set values to views inside inflated-view which my viewHolder is containing.
    @Override
    public void onBindViewHolder(final PermissionViewHolder viewHolder, final int position)
    {
        byte[] image_bytes = list.get(position).PersonPhoto;

        /*File imgFile = new File(image_path);

        if(imgFile.exists())
        {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            viewHolder.PersonPhotoImageView.setImageBitmap(myBitmap);
        }*/
        Bitmap bitmap = BitmapFactory.decodeByteArray(image_bytes, 0, image_bytes.length);
        viewHolder.PersonPhotoImageView.setImageBitmap(bitmap);
        viewHolder.PersonNameTextView.setText(list.get(position).PersonName);
        viewHolder.PersonPermissionStatusSwitch.setChecked(list.get(position).PersonPermissionStatus);

        if(list.get(position).PermissionDataSynced == 1)
            viewHolder.PermissionSyncButton.setVisibility(View.GONE);
        else
            viewHolder.PermissionSyncButton.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount()
    {
        return list == null ? 0 : list.size();
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
