package com.example.appdocbao.ui.categories;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocbao.R;
import com.example.appdocbao.data.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    
    private List<Category> categories;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        this.listener = listener;
    }

    public void updateCategories(List<Category> newCategories) {
        if (newCategories == null) return;
        
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CategoryDiffCallback(this.categories, newCategories));
        this.categories.clear();
        this.categories.addAll(newCategories);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categories.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    private static class CategoryDiffCallback extends DiffUtil.Callback {
        private final List<Category> oldList;
        private final List<Category> newList;

        CategoryDiffCallback(List<Category> oldList, List<Category> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEmoji;
        private final TextView tvCategoryName;
        private final CardView cardViewCategory;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            cardViewCategory = itemView.findViewById(R.id.cardViewCategory);
        }

        void bind(final Category category, final OnCategoryClickListener listener) {
            tvEmoji.setText(category.getEmoji() != null ? category.getEmoji() : "📰");
            tvCategoryName.setText(category.getName());
            cardViewCategory.setOnClickListener(v -> listener.onCategoryClick(category));
        }
    }
} 