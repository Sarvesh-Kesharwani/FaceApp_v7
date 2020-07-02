package com.sarvesh.faceapp_v7;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PermissionAdapter extends RecyclerView.Adapter<PermissionViewHolder> {

    List<CardData> list;
    Context context;

    public PermissionAdapter(List<CardData> list, Context context)
    {
        this.list = list;
        this.context = context;
    }

    @Override
    public PermissionViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the layout
        View photoView = inflater.inflate(R.layout.permission_card,parent, false);

        PermissionViewHolder viewHolder = new PermissionViewHolder(photoView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final PermissionViewHolder viewHolder, final int position)
    {
        viewHolder.PersonPhoto.set(list.get(position).name);
        viewHolder.PersonName.setText(list.get(position).date);
        viewHolder.PersonPermissionStatus.setText(list.get(position).message);
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

    // Sample data for RecyclerView
    private List<CardData> getData()
    {
        List<CardData> list = new ArrayList<>();
        list.add(new CardData("First Exam",
                "May 23, 2015",
                "Best Of Luck"));
        list.add(new CardData("Second Exam",
                "June 09, 2015",
                "b of l"));
        list.add(new CardData("My Test Exam",
                "April 27, 2017",
                "This is testing exam .."));

        return list;
    }
}
