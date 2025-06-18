package com.example.appdocbao.ui.home;

import android.content.Context;
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
import com.example.appdocbao.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TrendingArticleAdapter extends RecyclerView.Adapter<TrendingArticleAdapter.TrendingViewHolder> {

    private final Context context;
    private final List<News> trendingNewsList;
    private final OnTrendingNewsClickListener listener;

    public interface OnTrendingNewsClickListener {
        void onTrendingNewsClick(News news);
    }

    public TrendingArticleAdapter(Context context, List<News> trendingNewsList, OnTrendingNewsClickListener listener) {
        this.context = context;
        this.trendingNewsList = trendingNewsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TrendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trending_news, parent, false);
        return new TrendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendingViewHolder holder, int position) {
        News news = trendingNewsList.get(position);
        holder.bind(news, position + 1);
    }

    @Override
    public int getItemCount() {
        return trendingNewsList.size();
    }

    public void updateTrendingNewsList(List<News> newTrendingNewsList) {
        trendingNewsList.clear();
        trendingNewsList.addAll(newTrendingNewsList);
        notifyDataSetChanged();
    }

    class TrendingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTrendingImage;
        TextView tvTrendingTitle, tvTrendingDate, tvTrendingRank, tvTrendingCategory;

        public TrendingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTrendingImage = itemView.findViewById(R.id.ivTrendingImage);
            tvTrendingTitle = itemView.findViewById(R.id.tvTrendingTitle);
            tvTrendingDate = itemView.findViewById(R.id.tvTrendingDate);
            tvTrendingRank = itemView.findViewById(R.id.tvTrendingRank);
            tvTrendingCategory = itemView.findViewById(R.id.tvTrendingCategory);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTrendingNewsClick(trendingNewsList.get(position));
                }
            });
        }

        public void bind(News news, int rank) {
            // Set rank
            tvTrendingRank.setText(String.valueOf(rank));

            // Set title
            tvTrendingTitle.setText(news.getTitle());

            // Set date using DateUtils for better formatting
            String displayTime = "Vừa đăng"; // Default fallback
            
            if (news.getPublishedDate() != null) {
                // Use Date object if available
                String relativeTime = DateUtils.getRelativeTimeSpan(news.getPublishedDate());
                displayTime = relativeTime;
                Log.d("TrendingAdapter", "Using publishedDate: " + news.getPublishedDate() + " -> " + relativeTime);
            } else if (news.getPublishDate() != null && !news.getPublishDate().isEmpty()) {
                // Use String date if Date is null
                displayTime = news.getPublishDate();
                Log.d("TrendingAdapter", "Using publishDate (String): " + news.getPublishDate());
            } else {
                Log.w("TrendingAdapter", "Both publishedDate and publishDate are null/empty for news: " + news.getTitle());
            }
            
            tvTrendingDate.setText(displayTime);

            // Set category (if available)
            if (tvTrendingCategory != null) {
                // You can set category based on news properties or leave it empty
                tvTrendingCategory.setText("Tin tức");
            }

            // Load trending image
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
                        .into(ivTrendingImage);
            } else {
                ivTrendingImage.setImageResource(R.drawable.placeholder_image);
            }

            // Set different styling for top 3 trending articles
            if (rank <= 3) {
                tvTrendingRank.setBackgroundResource(R.drawable.bg_trending_top);
                tvTrendingRank.setTextColor(context.getResources().getColor(android.R.color.white));
            } else {
                tvTrendingRank.setBackgroundResource(R.drawable.bg_trending_normal);
                tvTrendingRank.setTextColor(context.getResources().getColor(android.R.color.black));
            }
        }
    }
}
