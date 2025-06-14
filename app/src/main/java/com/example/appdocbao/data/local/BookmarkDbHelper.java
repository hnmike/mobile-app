package com.example.appdocbao.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.appdocbao.data.model.Article;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookmarkDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "BookmarkDbHelper";
    private static final String DATABASE_NAME = "bookmarks.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    public static final String TABLE_BOOKMARKS = "bookmarks";
    
    // Column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_SOURCE_URL = "source_url";
    public static final String COLUMN_SOURCE_NAME = "source_name";
    public static final String COLUMN_CATEGORY_ID = "category_id";
    public static final String COLUMN_CATEGORY_NAME = "category_name";
    public static final String COLUMN_PUBLISHED_DATE = "published_date";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // Create table SQL statement
    private static final String SQL_CREATE_BOOKMARKS_TABLE =
            "CREATE TABLE " + TABLE_BOOKMARKS + " (" +
                    COLUMN_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_IMAGE_URL + " TEXT, " +
                    COLUMN_SOURCE_URL + " TEXT, " +
                    COLUMN_SOURCE_NAME + " TEXT, " +
                    COLUMN_CATEGORY_ID + " TEXT, " +
                    COLUMN_CATEGORY_NAME + " TEXT, " +
                    COLUMN_PUBLISHED_DATE + " TEXT, " +
                    COLUMN_TIMESTAMP + " INTEGER" +
                    ")";

    // Singleton instance
    private static BookmarkDbHelper instance;

    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private BookmarkDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized BookmarkDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BookmarkDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BOOKMARKS_TABLE);
        Log.d(TAG, "Created bookmarks database table");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now, simply drop the table and start over
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
        onCreate(db);
    }

    /**
     * Add or update a bookmarked article in the database
     */
    public boolean saveBookmark(Article article) {
        if (article == null) {
            Log.e(TAG, "Cannot save null article");
            return false;
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ID, article.getId());
        values.put(COLUMN_TITLE, article.getTitle());
        values.put(COLUMN_CONTENT, article.getContent());
        values.put(COLUMN_IMAGE_URL, article.getImageUrl());
        values.put(COLUMN_SOURCE_URL, article.getSourceUrl());
        values.put(COLUMN_SOURCE_NAME, article.getSourceName());
        values.put(COLUMN_CATEGORY_ID, article.getCategoryId());
        values.put(COLUMN_CATEGORY_NAME, article.getCategoryName());
        
        if (article.getPublishedAt() != null) {
            values.put(COLUMN_PUBLISHED_DATE, dateFormat.format(article.getPublishedAt()));
        }
        
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());

        try {
            long result = db.insertWithOnConflict(
                    TABLE_BOOKMARKS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE);
            
            Log.d(TAG, "Saved bookmark: " + article.getTitle() + ", result: " + result);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error saving bookmark: " + e.getMessage(), e);
            return false;
        } finally {
            db.close();
        }
    }

    /**
     * Delete a bookmarked article from the database
     */
    public boolean removeBookmark(String articleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int result = db.delete(
                    TABLE_BOOKMARKS,
                    COLUMN_ID + " = ?",
                    new String[]{articleId});
            
            Log.d(TAG, "Removed bookmark with ID: " + articleId + ", result: " + result);
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error removing bookmark: " + e.getMessage(), e);
            return false;
        } finally {
            db.close();
        }
    }

    /**
     * Check if an article is bookmarked
     */
    public boolean isBookmarked(String articleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_BOOKMARKS,
                    new String[]{COLUMN_ID},
                    COLUMN_ID + " = ?",
                    new String[]{articleId},
                    null, null, null);

            boolean isBookmarked = cursor != null && cursor.getCount() > 0;
            Log.d(TAG, "Article " + articleId + " is bookmarked: " + isBookmarked);
            return isBookmarked;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if article is bookmarked: " + e.getMessage(), e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    /**
     * Get all bookmarked articles
     */
    public List<Article> getAllBookmarks() {
        List<Article> bookmarks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Order by timestamp (newest first)
            cursor = db.query(TABLE_BOOKMARKS,
                    null,
                    null,
                    null,
                    null,
                    null,
                    COLUMN_TIMESTAMP + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Article article = cursorToArticle(cursor);
                    if (article != null) {
                        article.setBookmarked(true);
                        bookmarks.add(article);
                    }
                } while (cursor.moveToNext());
            }
            
            Log.d(TAG, "Loaded " + bookmarks.size() + " bookmarked articles");
            return bookmarks;
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving all bookmarks: " + e.getMessage(), e);
            return bookmarks;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    /**
     * Get a specific bookmarked article
     */
    public Article getBookmark(String articleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_BOOKMARKS,
                    null,
                    COLUMN_ID + " = ?",
                    new String[]{articleId},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                Article article = cursorToArticle(cursor);
                if (article != null) {
                    article.setBookmarked(true);
                }
                return article;
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving bookmark: " + e.getMessage(), e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    /**
     * Convert cursor to Article object
     */
    private Article cursorToArticle(Cursor cursor) {
        try {
            String id = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
            String content = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT));
            String imageUrl = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL));
            String sourceUrl = cursor.getString(cursor.getColumnIndex(COLUMN_SOURCE_URL));
            String sourceName = cursor.getString(cursor.getColumnIndex(COLUMN_SOURCE_NAME));
            String categoryId = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY_ID));
            String categoryName = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY_NAME));
            String publishedDateStr = cursor.getString(cursor.getColumnIndex(COLUMN_PUBLISHED_DATE));
            
            Date publishedDate = null;
            if (publishedDateStr != null && !publishedDateStr.isEmpty()) {
                try {
                    publishedDate = dateFormat.parse(publishedDateStr);
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date: " + e.getMessage(), e);
                }
            }
            
            Article article = new Article(
                    id,
                    title,
                    content,
                    imageUrl,
                    sourceUrl,
                    sourceName,
                    categoryId,
                    categoryName,
                    publishedDate
            );
            article.setBookmarked(true);
            return article;
        } catch (Exception e) {
            Log.e(TAG, "Error converting cursor to article: " + e.getMessage(), e);
            return null;
        }
    }
} 