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

public class VehicleAdapter extends RecyclerView.Adapter<VehicleViewHolder> {

    List<Vehicle_CardData> list;
    Context context;
    public VehicleRecyclerViewClickinterface mOnSyncListener;

    public VehicleAdapter(List<Vehicle_CardData> list, Context context, VehicleRecyclerViewClickinterface mOnSyncListener)
    {
        this.list = list;
        this.context = context;
        this.mOnSyncListener = mOnSyncListener;
    }

    @Override
    public VehicleViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the layout
        View photoView = inflater.inflate(R.layout.vehicle_card,parent, false);//converted xml file into a view by inflating it.

        VehicleViewHolder viewHolder = new VehicleViewHolder(photoView, mOnSyncListener);//giving inflated view to viewHolder
        return viewHolder;
    }

    //now my viewHolder will set values to views inside inflated-view which my viewHolder is containing.
    @Override
    public void onBindViewHolder(final VehicleViewHolder viewHolder, final int position)
    {
        viewHolder.VehicleNumberTextView.setText(list.get(position).number);
        viewHolder.VehicleNameTextView.setText(list.get(position).name);
        viewHolder.VehiclePermissionStatusSwitch.setChecked(list.get(position).vehicleStatus);

        if(list.get(position).vehicleSynced == 1)
            viewHolder.VehicleSyncButton.setVisibility(View.GONE);
        else
            viewHolder.VehicleSyncButton.setVisibility(View.VISIBLE);
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
