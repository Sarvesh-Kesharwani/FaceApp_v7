package com.sarvesh.faceapp_v7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PermissionAdapter extends RecyclerView.Adapter<PermissionViewHolder> {

    List<CardData> list;
    Context context;

    private PermissionViewHolder.OnSyncListener mOnSyncListener;

    public PermissionAdapter(List<CardData> list, Context context, PermissionViewHolder.OnSyncListener onSyncListener)
    {
        this.list = list;
        this.context = context;
        this.mOnSyncListener = onSyncListener;
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

        byte [] imageByteArray = list.get(position).PersonPhoto;
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageByteArray,0,imageByteArray.length);
        viewHolder.PersonPhotoImageView.setImageBitmap(imageBitmap);

        viewHolder.PersonNameTextView.setText(list.get(position).PersonName);
        viewHolder.PersonPermissionStatusSwitch.setChecked(list.get(position).PersonPermissionStatus);

        if(list.get(position).PermissionDataSynced)
            viewHolder.PermissionSyncButton.setVisibility(View.GONE);
        else
            viewHolder.PermissionSyncButton.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    //this is sample data to know how to pass data items & not to use.
   /* // Sample data for RecyclerView
    private List<CardData> getData()
    {
        List<CardData> list = new ArrayList<>();
        list.add(new CardData(Register.photoBitmap, Register.name,true, false));
        list.add(new CardData("Second Exam",
                "June 09, 2015",
                "b of l"));
        list.add(new CardData("My Test Exam",
                "April 27, 2017",
                "This is testing exam .."));

        return list;
    }
    */
}
