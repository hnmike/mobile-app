package com.example.appdocbao.data.model;

public class Category {
    private String id;
    private String name;
    private String description;
    private String iconEmoji;
    private int articleCount;

    /****
     * Constructs a new Category with default values.
     * <p>
     * Required for Firestore deserialization.
     */
    public Category() {
    }

    /**
     * Constructs a Category with the specified id, name, description, and emoji icon.
     * The article count is initialized to zero.
     *
     * @param id the unique identifier for the category
     * @param name the name of the category
     * @param description a description of the category
     * @param iconEmoji the emoji icon representing the category
     */
    public Category(String id, String name, String description, String iconEmoji) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.articleCount = 0;
    }

    /**
     * Returns the unique identifier of the category.
     *
     * @return the category's ID
     */
    public String getId() {
        return id;
    }

    /****
     * Sets the identifier for this category.
     *
     * @param id the unique identifier to assign to the category
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the category.
     *
     * @return the category name
     */
    public String getName() {
        return name;
    }

    /****
     * Sets the name of the category.
     *
     * @param name the new name for the category
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of the category.
     *
     * @return the category description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the category.
     *
     * @param description the new description for the category
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the emoji icon representing the category.
     *
     * @return the emoji icon associated with this category
     */
    public String getIconEmoji() {
        return iconEmoji;
    }

    /****
     * Sets the emoji icon representing the category.
     *
     * @param iconEmoji the emoji to associate with this category
     */
    public void setIconEmoji(String iconEmoji) {
        this.iconEmoji = iconEmoji;
    }

    /**
     * Returns the emoji icon representing the category.
     *
     * @return the emoji icon for this category
     */
    public String getEmoji() {
        return iconEmoji;
    }

    /****
     * Returns the number of articles associated with this category.
     *
     * @return the article count for the category
     */
    public int getArticleCount() {
        return articleCount;
    }

    /**
     * Sets the number of articles associated with this category.
     *
     * @param articleCount the new article count
     */
    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }
} 