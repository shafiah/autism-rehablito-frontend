package com.autismcentre.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.autismcentre.R;

public class ImageSliderAdapter
        extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {

    private final int[] images;

    public ImageSliderAdapter(int[] images) {
        this.images = images;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_image_slider,
                        parent,
                        false
                );

        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ImageViewHolder holder,
            int position) {

        holder.sliderImage.setImageResource(
                images[position]
        );
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    static class ImageViewHolder
            extends RecyclerView.ViewHolder {

        ImageView sliderImage;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            sliderImage = itemView.findViewById(
                    R.id.sliderImage
            );
        }
    }
}