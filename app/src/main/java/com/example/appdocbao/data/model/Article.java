package com.example.appdocbao.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Article {
    private String id;
    private String title;
    private String summary;
    private String content;
    private String imageUrl;
    private String url;
    private String categoryId;
    private String categoryText;
    private String authorName;
    private String authorAvatarUrl;
    private Date publishedTime;
    private int viewCount;
    private boolean isBookmarked;
    private String sourceName;
    
    /****
     * Default no-argument constructor for serialization and Firebase compatibility.
     */
    public Article() {
        // Required empty constructor for Firebase
    }
    
    /**
     * Constructs an Article with all primary attributes specified.
     *
     * @param id unique identifier for the article
     * @param title title of the article
     * @param summary brief summary of the article
     * @param content full content of the article
     * @param imageUrl URL of the article's image
     * @param url source URL of the article
     * @param categoryId identifier for the article's category
     * @param categoryText name of the article's category
     * @param authorName name of the article's author
     * @param authorAvatarUrl URL of the author's avatar image
     * @param publishedTime publication date and time
     * @param viewCount number of times the article has been viewed
     * @param isBookmarked whether the article is bookmarked
     */
    public Article(String id, String title, String summary, String content, String imageUrl, 
                  String url, String categoryId, String categoryText, String authorName, 
                  String authorAvatarUrl, Date publishedTime, int viewCount, boolean isBookmarked) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.imageUrl = imageUrl;
        this.url = url;
        this.categoryId = categoryId;
        this.categoryText = categoryText;
        this.authorName = authorName;
        this.authorAvatarUrl = authorAvatarUrl;
        this.publishedTime = publishedTime;
        this.viewCount = viewCount;
        this.isBookmarked = isBookmarked;
    }
    
    /**
     * Constructs an Article with specified details, initializing view count to 0 and bookmark status to false.
     *
     * This constructor is intended for use with parsers such as VnExpressParser, allowing creation of an Article with essential metadata and default values for view count and bookmark status.
     *
     * @param id the unique identifier of the article
     * @param title the title of the article
     * @param content the full content of the article
     * @param imageUrl the URL of the article's image
     * @param sourceUrl the URL of the article's source
     * @param sourceName the name of the article's source
     * @param categoryId the identifier of the article's category
     * @param categoryName the name of the article's category
     * @param publishedTime the publication date of the article
     */
    public Article(String id, String title, String content, String imageUrl, String sourceUrl, 
                  String sourceName, String categoryId, String categoryName, Date publishedTime) {
        this.id = id;
        this.title = title;
        this.summary = content;
        this.content = content;
        this.imageUrl = imageUrl;
        this.url = sourceUrl;
        this.categoryId = categoryId;
        this.categoryText = categoryName;
        this.publishedTime = publishedTime;
        this.sourceName = sourceName;
        this.viewCount = 0;
        this.isBookmarked = false;
    }

    /****
     * Returns the unique identifier of the article.
     *
     * @return the article's ID
     */
    public String getId() {
        return id;
    }

    /****
     * Sets the unique identifier for the article.
     *
     * @param id the article's unique identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the title of the article.
     *
     * @return the article's title
     */
    public String getTitle() {
        return title;
    }

    /****
     * Sets the title of the article.
     *
     * @param title the new title to assign to the article
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the summary of the article.
     *
     * @return the article summary, or null if not set
     */
    public String getSummary() {
        return summary;
    }

    /****
     * Sets the summary of the article.
     *
     * @param summary the brief summary to assign to the article
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Returns the full content of the article.
     *
     * @return the article's content text
     */
    public String getContent() {
        return content;
    }

    /****
     * Sets the full content of the article.
     *
     * @param content the article's content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /****
     * Returns the URL of the article's image.
     *
     * @return the image URL, or null if not set
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /****
     * Sets the URL of the article's image.
     *
     * @param imageUrl the URL to set for the article's image
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Returns the URL of the article.
     *
     * @return the article's source URL
     */
    public String getUrl() {
        return url;
    }

    /****
     * Sets the URL of the article.
     *
     * @param url the URL to assign to this article
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /****
     * Returns the identifier of the article's category.
     *
     * @return the category ID
     */
    public String getCategoryId() {
        return categoryId;
    }

    /****
     * Sets the identifier for the article's category.
     *
     * @param categoryId the category identifier to assign
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    /****
     * Returns the textual name of the article's category.
     *
     * @return the category name as a string
     */
    public String getCategoryText() {
        return categoryText;
    }

    /****
     * Sets the textual name of the article's category.
     *
     * @param categoryText the category name to assign
     */
    public void setCategoryText(String categoryText) {
        this.categoryText = categoryText;
    }

    /****
     * Returns the name of the article's author.
     *
     * @return the author's name, or null if not set
     */
    public String getAuthorName() {
        return authorName;
    }

    /****
     * Sets the name of the article's author.
     *
     * @param authorName the author's name
     */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /****
     * Returns the URL of the author's avatar image.
     *
     * @return the author's avatar image URL, or null if not set
     */
    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    /****
     * Sets the URL of the author's avatar image for the article.
     *
     * @param authorAvatarUrl the URL of the author's avatar image
     */
    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    /****
     * Returns the publication date and time of the article.
     *
     * @return the publication date as a {@link Date} object, or {@code null} if not set
     */
    public Date getPublishedTime() {
        return publishedTime;
    }

    /****
     * Sets the publication date and time of the article.
     *
     * @param publishedTime the date and time when the article was published
     */
    public void setPublishedTime(Date publishedTime) {
        this.publishedTime = publishedTime;
    }

    /****
     * Returns the number of times the article has been viewed.
     *
     * @return the view count of the article
     */
    public int getViewCount() {
        return viewCount;
    }

    /****
     * Sets the view count for the article.
     *
     * @param viewCount the number of times the article has been viewed
     */
    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    /**
     * Returns whether the article is bookmarked.
     *
     * @return true if the article is bookmarked; false otherwise
     */
    public boolean isBookmarked() {
        return isBookmarked;
    }

    /****
     * Sets the bookmark status of the article.
     *
     * @param bookmarked true to mark the article as bookmarked, false otherwise
     */
    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }
    
    /****
     * Returns the URL of the article's source.
     *
     * @return the article's source URL
     */
    public String getSourceUrl() {
        return url;
    }
    
    /****
     * Returns the source name of the article, or "VnExpress" if the source name is not set.
     *
     * @return the source name, or the default "VnExpress" if none is specified
     */
    public String getSourceName() {
        return sourceName != null ? sourceName : "VnExpress";
    }
    
    /****
     * Sets the name of the article's source.
     *
     * @param sourceName the name of the source to assign to this article
     */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
    /****
     * Returns the name of the article's category.
     *
     * @return the category name, or null if not set
     */
    public String getCategoryName() {
        return categoryText;
    }
    
    /****
     * Returns the publication date of the article.
     *
     * @return the publication date, or null if not set
     */
    public Date getPublishedAt() {
        return publishedTime;
    }
    
    /****
     * Returns the publication date formatted as "dd/MM/yyyy HH:mm" using the default locale.
     *
     * @return the formatted publication date string, or an empty string if the publication date is not set
     */
    public String getPublishedTimeFormatted() {
        if (publishedTime == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(publishedTime);
    }
} 