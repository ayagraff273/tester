package com.graff.tester;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.graff.tester.models.ClothingItem;
import com.graff.tester.models.ClothingItemRepository;
import com.graff.tester.models.ClothingType;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private final Context context;
    private final List<ClothingItem> clothingItems;
    public GalleryAdapter(Context context, List<ClothingItem> clothingItems) {
        this.context = context;
        this.clothingItems = clothingItems;
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
        int dp = 16;
        int spacing = dpToPx(dp); // adjust based on margins (4dp each side * 2 or more)
        int size = (screenWidth / columns) - spacing;

        // Set itemView's width and height to make it square
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params != null) {
            params.width = size;
            params.height = size;
            holder.itemView.setLayoutParams(params);
        }

        ClothingItem clothingItem = clothingItems.get(position);

        // Use Glide to load the image URL into the ImageView
        Glide.with(context)
                .load(clothingItem.getImageUrl())
                .into(holder.imageView);  // Assuming your item layout has an ImageView with this ID

        holder.btnDelete.setOnClickListener(v -> {
            ClothingItem item = clothingItems.get(position);

            if (!holder.canRemoveFromList(item)) {
                Toast.makeText(context, "You must have at least 2 " + item.getClothingType() + "s!", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(context)
                    .setTitle("מחיקת פריט לבוש")
                    .setMessage("את/ה בטוח/ה שאת/ה רוצה למחוק את הפריט הזה?")
                    .setPositiveButton("כן", (dialog, which) -> {
                        FirebaseManager firebaseManager = new FirebaseManager();
                        firebaseManager.deleteItem(item, item1 -> {
                            ClothingItemRepository.getInstance().removeItem(item);
                            clothingItems.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton("ביטול", (dialog, which) -> dialog.dismiss())
                    .show();
        });

    }

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return clothingItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public boolean canRemoveFromList(ClothingItem item) {
            List<ClothingItem> shirts=ClothingItemRepository.getInstance().getShirtItems();
            List<ClothingItem> pants=ClothingItemRepository.getInstance().getPantsItems();
            ClothingType clothingType = item.getClothingType();
            if(clothingType==ClothingType.SHIRT && shirts.size()<=2){
                return false;
            }
            else return clothingType != ClothingType.PANTS || pants.size() > 2;
        }

        }
    }


