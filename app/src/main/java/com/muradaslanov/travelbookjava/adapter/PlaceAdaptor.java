package com.muradaslanov.travelbookjava.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.renderscript.ScriptGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Placeholder;
import androidx.recyclerview.widget.RecyclerView;

import com.muradaslanov.travelbookjava.databinding.RecycleRowBinding;
import com.muradaslanov.travelbookjava.model.Place;
import com.muradaslanov.travelbookjava.view.MapsActivity;

import java.util.List;

public class PlaceAdaptor extends RecyclerView.Adapter<PlaceAdaptor.PlaceHolder> {

    List<Place> placeList;

    public PlaceAdaptor(List<Place> placeList){
        this.placeList = placeList;
    }


    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         RecycleRowBinding rowBinding = RecycleRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new PlaceHolder(rowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.recycleRowBinding.recyclerViewTextView.setText(placeList.get(position).name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), MapsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("place",placeList.get(position));
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public class PlaceHolder extends  RecyclerView.ViewHolder{
        RecycleRowBinding recycleRowBinding;

        public PlaceHolder(RecycleRowBinding recycleRowBinding) {
            super(recycleRowBinding.getRoot());
            this.recycleRowBinding = recycleRowBinding;
        }
    }

}
