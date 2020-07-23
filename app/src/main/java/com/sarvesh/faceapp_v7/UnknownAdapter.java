package com.sarvesh.faceapp_v7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UnknownAdapter extends RecyclerView.Adapter<UnknownViewHolder> {

    List<Unknown_CardData> list;
    Context context;
    public UnknownRecyclerViewClickInterface mOnFullScreenClickListner;

    public UnknownAdapter(List<Unknown_CardData> list, Context context, UnknownRecyclerViewClickInterface mOnFullScreenClickListner)
    {
        this.list = list;
        this.context = context;
        this.mOnFullScreenClickListner = mOnFullScreenClickListner;
    }

    @Override
    public UnknownViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the layout
        View photoView = inflater.inflate(R.layout.unknown_card,parent, false);//converted xml file into a view buy inflating it.

        UnknownViewHolder viewHolder = new UnknownViewHolder(photoView, mOnFullScreenClickListner);//giving inflated view to viewHolder
        return viewHolder;
    }

    //now my viewHolder will set values to views inside inflated-view which my viewHolder is containing.
    @Override
    public void onBindViewHolder(final UnknownViewHolder viewHolder, final int position)
    {
        byte[] Photo_bytes = list.get(position).Unknown_Person_Photo;
        Bitmap bitmap = BitmapFactory.decodeByteArray(Photo_bytes , 0, Photo_bytes .length);
        viewHolder.CapturedImage_ImageView.setImageBitmap(bitmap);

        viewHolder.Time_TextView.setText(list.get(position).Image_Capture_Time);
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
