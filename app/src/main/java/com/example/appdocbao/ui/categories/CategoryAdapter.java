package com.example.appdocbao.ui.categories;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocbao.R;
import com.example.appdocbao.data.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        /****
 * Called when a category item is clicked.
 *
 * @param category the category that was clicked
 */
void onCategoryClick(Category category);
    }

    /**
     * Constructs a CategoryAdapter with the specified list of categories and a click listener.
     *
     * @param categories the initial list of categories to display
     * @param listener the listener to handle category item click events
     */
    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    /****
     * Replaces the current list of categories with a new list and refreshes the RecyclerView display.
     *
     * @param newCategories the updated list of categories to display
     */
    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    /**
     * Creates a new CategoryViewHolder by inflating the category item layout.
     *
     * @param parent the parent ViewGroup into which the new view will be added
     * @param viewType the view type of the new view (unused)
     * @return a new instance of CategoryViewHolder for a category item
     */
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    /**
     * Binds the category data at the specified position to the provided view holder.
     *
     * @param holder the view holder to bind data to
     * @param position the position of the category in the list
     */
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, listener);
    }

    /**
     * Returns the number of categories in the adapter.
     *
     * @return the size of the category list, or 0 if the list is null
     */
    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEmoji;
        private final TextView tvCategoryName;
        private final CardView cardViewCategory;

        /**
         * Initializes the view holder by binding UI components for a category item.
         *
         * @param itemView the view representing a single category item in the RecyclerView
         */
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            cardViewCategory = itemView.findViewById(R.id.cardViewCategory);
        }

        /****
         * Binds the provided category data to the view holder and sets up a click listener.
         *
         * When the card view is clicked, invokes the listener's onCategoryClick method with the current category.
         *
         * @param category the category to display in this view holder
         * @param listener callback to handle category click events
         */
        void bind(final Category category, final OnCategoryClickListener listener) {
            tvEmoji.setText(category.getEmoji());
            tvCategoryName.setText(category.getName());
            
            cardViewCategory.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }
    }
} 