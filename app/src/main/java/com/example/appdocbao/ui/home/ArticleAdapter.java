package com.example.appdocbao.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdocbao.R;
import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.utils.DateUtils;

import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private List<Article> articles;
    private OnArticleClickListener listener;

    public ArticleAdapter(List<Article> articles) {
        this.articles = articles;
    }

    public void setOnArticleClickListener(OnArticleClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void updateArticles(List<Article> newArticles) {
        this.articles = newArticles;
        notifyDataSetChanged();
    }

    public interface OnArticleClickListener {
        void onArticleClick(Article article);
    }

    class ArticleViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivArticleThumbnail;
        private TextView tvCategory;
        private TextView tvTitle;
        private TextView tvTime;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArticleThumbnail = itemView.findViewById(R.id.iv_article_thumbnail);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onArticleClick(articles.get(position));
                }
            });
        }

        public void bind(Article article) {
            // Load article thumbnail
            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(article.getImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.circle_bg)
                        .into(ivArticleThumbnail);
            } else {
                ivArticleThumbnail.setImageResource(R.drawable.circle_bg);
            }

            // Set category text
            tvCategory.setText(article.getCategoryText());

            // Set title
            tvTitle.setText(article.getTitle());

            // Set time
            tvTime.setText(DateUtils.getTimeAgo(article.getPublishedTime()));
        }
    }
} 