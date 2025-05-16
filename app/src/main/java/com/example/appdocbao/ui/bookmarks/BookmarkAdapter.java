package com.example.appdocbao.ui.bookmarks;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdocbao.R;
import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

    private static final String TAG = "BookmarkAdapter";
    private List<Article> bookmarks;
    private final OnBookmarkClickListener listener;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(Article article);
        void onDeleteClick(Article article, int position);
    }

    public BookmarkAdapter(OnBookmarkClickListener listener) {
        this.bookmarks = new ArrayList<>();
        this.listener = listener;
    }

    public void updateBookmarks(List<Article> newBookmarks) {
        this.bookmarks = newBookmarks;
        notifyDataSetChanged();
        Log.d(TAG, "Updated bookmarks: " + newBookmarks.size() + " items");
    }

    public void removeBookmark(int position) {
        if (position >= 0 && position < bookmarks.size()) {
            Log.d(TAG, "Removing bookmark at position " + position);
            bookmarks.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bookmark, parent, false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        Article article = bookmarks.get(position);
        holder.bind(article, listener, position);
    }

    @Override
    public int getItemCount() {
        return bookmarks != null ? bookmarks.size() : 0;
    }

    static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgThumbnail;
        private final TextView tvTitle;
        private final TextView tvSource;
        private final TextView tvPublishedTime;
        private final CardView cardViewBookmark;
        private final ImageButton btnDelete;

        BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSource = itemView.findViewById(R.id.tvSource);
            tvPublishedTime = itemView.findViewById(R.id.tvPublishedTime);
            cardViewBookmark = itemView.findViewById(R.id.cardViewBookmark);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(final Article article, final OnBookmarkClickListener listener, final int position) {
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
                
                Log.d(TAG, "Loading image: " + imageUrl);
                
                Glide.with(imgThumbnail.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(imgThumbnail);
            } else {
                imgThumbnail.setImageResource(R.drawable.placeholder_image);
                Log.d(TAG, "No image URL for article: " + article.getTitle());
            }
            
            // Set click listeners
            cardViewBookmark.setOnClickListener(v -> {
                if (listener != null) {
                    Log.d(TAG, "Article clicked: " + article.getId() + " - " + article.getTitle());
                    listener.onBookmarkClick(article);
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    Log.d(TAG, "Delete clicked for: " + article.getId() + " - " + article.getTitle());
                    listener.onDeleteClick(article, position);
                }
            });
        }
    }
} 