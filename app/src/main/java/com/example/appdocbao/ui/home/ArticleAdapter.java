package com.example.appdocbao.ui.home;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdocbao.R;
import com.example.appdocbao.data.News;
import com.example.appdocbao.ui.newsdetail.NewsDetailActivity;
import com.example.appdocbao.utils.Constants;
import com.example.appdocbao.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.NewsViewHolder> {

    private final Context context;
    private final List<News> newsList;
    private final OnNewsClickListener listener;

    public interface OnNewsClickListener {
        void onNewsClick(News news);
    }

    public ArticleAdapter(Context context, List<News> newsList, OnNewsClickListener listener) {
        this.context = context;
        this.newsList = newsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news_horizontal, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.bind(news);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public void updateNewsList(List<News> newNewsList) {
        newsList.clear();
        newsList.addAll(newNewsList);
        notifyDataSetChanged();
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvDate, tvSource;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvSource = itemView.findViewById(R.id.tvSource);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNewsClick(newsList.get(position));
                }
            });
        }

        public void bind(News news) {
            // Set title
            tvTitle.setText(news.getTitle());

            // Set date using DateUtils for better formatting
            String displayTime = "Vừa đăng"; // Default fallback
            
            if (news.getPublishedDate() != null) {
                // Use Date object if available
                String relativeTime = DateUtils.getRelativeTimeSpan(news.getPublishedDate());
                displayTime = relativeTime;
                Log.d("ArticleAdapter", "Using publishedDate: " + news.getPublishedDate() + " -> " + relativeTime);
            } else if (news.getPublishDate() != null && !news.getPublishDate().isEmpty()) {
                // Use String date if Date is null
                displayTime = news.getPublishDate();
                Log.d("ArticleAdapter", "Using publishDate (String): " + news.getPublishDate());
            } else {
                Log.w("ArticleAdapter", "Both publishedDate and publishDate are null/empty for news: " + news.getTitle());
            }
            
            tvDate.setText(displayTime);

            // Set source
            if (tvSource != null) {
                tvSource.setText("VnExpress");
            }

            // Load thumbnail image
            if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                String imageUrl = news.getImageUrl();
                if (!imageUrl.startsWith("http")) {
                    imageUrl = "https:" + imageUrl;
                }

                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(ivThumbnail);
            } else {
                ivThumbnail.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}
