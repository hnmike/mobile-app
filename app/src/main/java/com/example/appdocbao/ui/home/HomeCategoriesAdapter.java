package com.example.appdocbao.ui.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdocbao.R;
import com.example.appdocbao.data.News;
import com.example.appdocbao.data.model.Category;
import com.example.appdocbao.ui.categories.CategoriesActivity;
import com.example.appdocbao.ui.newsdetail.NewsDetailActivity;
import com.example.appdocbao.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeCategoriesAdapter extends RecyclerView.Adapter<HomeCategoriesAdapter.CategoryViewHolder> {

    private final Context context;
    private List<Category> categories;
    private Map<Integer, List<News>> categoryNewsMap;
    private final OnNewsClickListener onNewsClickListener;
    private final Map<Integer, ArticleAdapter> adapterMap = new HashMap<>();

    public interface OnNewsClickListener {
        void onNewsClick(News news);
    }

    public HomeCategoriesAdapter(Context context, List<Category> categories, 
                               Map<Integer, List<News>> categoryNewsMap, 
                               OnNewsClickListener onNewsClickListener) {
        this.context = context;
        this.categories = categories;
        this.categoryNewsMap = categoryNewsMap;
        this.onNewsClickListener = onNewsClickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_with_news, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateData(List<Category> newCategories, Map<Integer, List<News>> newCategoryNewsMap) {
        this.categories = newCategories;
        this.categoryNewsMap = newCategoryNewsMap;
        notifyDataSetChanged();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvViewAll;
        RecyclerView rvCategoryNews;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvViewAll = itemView.findViewById(R.id.tvViewAll);
            rvCategoryNews = itemView.findViewById(R.id.rvCategoryNews);
        }

        public void bind(Category category, int position) {
            // Set category name
            tvCategoryName.setText(category.getName());

            // Set up horizontal RecyclerView for news
            rvCategoryNews.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            
            // Get news for this category
            int categoryId = Integer.parseInt(category.getId());
            List<News> newsList = categoryNewsMap.get(categoryId);
            if (newsList == null) {
                newsList = new ArrayList<>();
            }

            // Get or create adapter for this category
            ArticleAdapter newsAdapter = adapterMap.get(categoryId);
            if (newsAdapter == null) {
                newsAdapter = new ArticleAdapter(context, newsList, new ArticleAdapter.OnNewsClickListener() {
                    @Override
                    public void onNewsClick(News news) {
                        if (onNewsClickListener != null) {
                            onNewsClickListener.onNewsClick(news);
                        }
                    }
                });
                adapterMap.put(categoryId, newsAdapter);
            } else {
                newsAdapter.updateNewsList(newsList);
            }
            
            rvCategoryNews.setAdapter(newsAdapter);

            // Set up "View All" click listener
            tvViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(context, CategoriesActivity.class);
                intent.putExtra(Constants.EXTRA_CATEGORY_ID, category.getId());
                intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getName());
                context.startActivity(intent);
            });

            // Hide "View All" button for featured articles category
            if (categoryId == 0) {
                tvViewAll.setVisibility(View.GONE);
            } else {
                tvViewAll.setVisibility(View.VISIBLE);
            }
        }
    }
} 