package com.example.appdocbao.ui.newslist;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdocbao.R;
import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.utils.DateUtils;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<Article> articles;
    private final OnArticleClickListener listener;

    public interface OnArticleClickListener {
        /****
 * Called when a news article item is clicked.
 *
 * @param article the Article object that was selected
 */
void onArticleClick(Article article);
    }

    /**
     * Constructs a NewsAdapter with the specified list of articles and click listener.
     *
     * @param articles the list of articles to display in the adapter
     * @param listener the listener to handle article click events
     */
    public NewsAdapter(List<Article> articles, OnArticleClickListener listener) {
        this.articles = articles;
        this.listener = listener;
    }

    /**
     * Replaces the current list of articles with a new list and refreshes the RecyclerView display.
     *
     * @param newArticles the new list of articles to display
     */
    public void updateArticles(List<Article> newArticles) {
        this.articles = newArticles;
        notifyDataSetChanged();
    }

    /**
     * Creates a new NewsViewHolder by inflating the news item layout.
     *
     * @param parent the parent ViewGroup into which the new view will be added
     * @param viewType the view type of the new view
     * @return a new instance of NewsViewHolder for a news article item
     */
    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    /**
     * Binds the article at the specified position to the provided ViewHolder.
     *
     * @param holder the ViewHolder to bind data to
     * @param position the position of the article in the list
     */
    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.bind(article, listener);
    }

    /**
     * Returns the number of articles in the adapter.
     *
     * @return the total count of articles, or 0 if the list is null
     */
    @Override
    public int getItemCount() {
        return articles != null ? articles.size() : 0;
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgThumbnail;
        private final TextView tvTitle;
        private final TextView tvSource;
        private final TextView tvPublishedTime;
        private final CardView cardViewNews;

        /**
         * Initializes the NewsViewHolder by caching references to the news item UI components.
         *
         * @param itemView the view representing a single news item in the RecyclerView
         */
        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSource = itemView.findViewById(R.id.tvSource);
            tvPublishedTime = itemView.findViewById(R.id.tvPublishedTime);
            cardViewNews = itemView.findViewById(R.id.cardViewNews);
        }

        /**
         * Binds an Article's data to the view holder's UI components and sets up the click listener.
         *
         * Populates the title, source, published time, and thumbnail image for the article.
         * If the article has a valid image URL, loads it using Glide; otherwise, displays a placeholder image.
         * Invokes the provided listener when the article card is clicked.
         *
         * @param article the Article to display in this view holder
         * @param listener callback to handle article click events
         */
        void bind(final Article article, final OnArticleClickListener listener) {
            tvTitle.setText(article.getTitle());
            tvSource.setText(article.getSourceName());
            
            // Format time as "X hours ago"
            String timeAgo = DateUtils.getTimeAgo(article.getPublishedAt());
            tvPublishedTime.setText(timeAgo);
            
            // Load image with Glide
            String imageUrl = article.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Fix image URL if needed
                if (!imageUrl.startsWith("http")) {
                    imageUrl = "https:" + imageUrl;
                }
                
                Log.d("NewsAdapter", "Loading image: " + imageUrl);
                
                Glide.with(imgThumbnail.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(imgThumbnail);
            } else {
                imgThumbnail.setImageResource(R.drawable.placeholder_image);
                Log.d("NewsAdapter", "No image URL for article: " + article.getTitle());
            }
            
            cardViewNews.setOnClickListener(v -> {
                if (listener != null) {
                    Log.d("NewsAdapter", "Article clicked: " + article.getId() + " - " + article.getTitle());
                    listener.onArticleClick(article);
                }
            });
        }
    }
} 