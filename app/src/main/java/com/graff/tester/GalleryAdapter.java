package com.graff.tester;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private Context context;
    private List<String> imageUrls;  // List of image URLs

    public GalleryAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Calculate card size dynamically
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int columns = 3; // same as your GridLayoutManager
        int spacing = dpToPx(16); // adjust based on margins (4dp each side * 2 or more)
        int size = (screenWidth / columns) - spacing;

        // Set itemView's width and height to make it square
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params != null) {
            params.width = size;
            params.height = size;
            holder.itemView.setLayoutParams(params);
        }

        String imageUrl = imageUrls.get(position);

        // Use Glide to load the image URL into the ImageView
        Glide.with(context)
                .load(imageUrl)
                .into(holder.imageView);  // Assuming your item layout has an ImageView with this ID
    }

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}

