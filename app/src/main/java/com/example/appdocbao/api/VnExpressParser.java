package com.example.appdocbao.api;

import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.data.model.Category;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.util.Log;

public class VnExpressParser {

    public static final String BASE_URL = "https://vnexpress.net";
    private static final Map<String, String> CATEGORY_MAP = initCategoryMap();

    /**
     * Initializes and returns a map of VnExpress category URL slugs to their Vietnamese names.
     *
     * @return a map where keys are category IDs (slugs) and values are their corresponding Vietnamese names
     */
    private static Map<String, String> initCategoryMap() {
        Map<String, String> map = new HashMap<>();
        map.put("thoi-su", "Thời sự");
        map.put("the-gioi", "Thế giới");
        map.put("kinh-doanh", "Kinh doanh");
        map.put("giai-tri", "Giải trí");
        map.put("the-thao", "Thể thao");
        map.put("phap-luat", "Pháp luật");
        map.put("giao-duc", "Giáo dục");
        map.put("suc-khoe", "Sức khỏe");
        map.put("doi-song", "Đời sống");
        map.put("du-lich", "Du lịch");
        map.put("khoa-hoc", "Khoa học");
        map.put("so-hoa", "Số hóa");
        map.put("xe", "Xe");
        map.put("y-kien", "Ý kiến");
        map.put("tam-su", "Tâm sự");
        return map;
    }

    /****
     * Returns a list of news categories available on VnExpress, each with an associated emoji and description.
     *
     * @return a list of Category objects representing VnExpress news categories
     */
    public static List<Category> parseCategories() {
        List<Category> categories = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : CATEGORY_MAP.entrySet()) {
            String categoryId = entry.getKey();
            String categoryName = entry.getValue();
            
            // Create emoji mapping for categories
            String emoji = "📰"; // Default
            
            if (categoryName.equals("Thể thao")) emoji = "🏈";
            else if (categoryName.equals("Giải trí")) emoji = "🎬";
            else if (categoryName.equals("Kinh doanh")) emoji = "💼";
            else if (categoryName.equals("Du lịch")) emoji = "🌴";
            else if (categoryName.equals("Công nghệ") || categoryName.equals("Số hóa")) emoji = "🎮";
            else if (categoryName.equals("Đời sống")) emoji = "🌞";
            
            Category category = new Category(
                    categoryId,
                    categoryName,
                    "Tin tức " + categoryName.toLowerCase() + " mới nhất",
                    emoji
            );
            
            categories.add(category);
        }
        
        return categories;
    }

    /****
     * Parses a list of articles from the provided HTML content for a specific category.
     *
     * Attempts to extract article elements using multiple CSS selectors to maximize compatibility with different page layouts.
     * Each article element is parsed into an Article object; invalid or incomplete articles are skipped.
     * Returns an empty list if the HTML is null, empty, or no articles are found.
     *
     * @param html the HTML content containing articles
     * @param categoryId the identifier for the news category
     * @return a list of parsed Article objects, or an empty list if none are found
     */
    public static List<Article> parseArticlesByCategory(String html, String categoryId) {
        List<Article> articles = new ArrayList<>();
        
        try {
            if (html == null || html.isEmpty()) {
                Log.e("VnExpressParser", "Empty HTML content");
                return articles;
            }
            
            Document doc = Jsoup.parse(html);
            Log.d("VnExpressParser", "Parsed HTML document with title: " + doc.title());
            
            Elements articleElements = doc.select("article.item-news");
            Log.d("VnExpressParser", "Found " + articleElements.size() + " article elements");
            
            if (articleElements.isEmpty()) {
                // Try alternative CSS selectors
                articleElements = doc.select("article.item-news-common");
                Log.d("VnExpressParser", "Second attempt found " + articleElements.size() + " article elements");
                
                if (articleElements.isEmpty()) {
                    // Try more general selector
                    articleElements = doc.select("article");
                    Log.d("VnExpressParser", "Last attempt found " + articleElements.size() + " article elements");
                }
            }
            
            for (Element articleElement : articleElements) {
                try {
                    Article article = parseArticleElement(articleElement, categoryId);
                    if (article != null) {
                        articles.add(article);
                        Log.d("VnExpressParser", "Added article: " + article.getTitle());
                    }
                } catch (Exception e) {
                    Log.e("VnExpressParser", "Error parsing individual article: " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            Log.e("VnExpressParser", "Error parsing articles: " + e.getMessage(), e);
        }
        
        Log.d("VnExpressParser", "Returning " + articles.size() + " articles");
        return articles;
    }
    
    /**
     * Parses an article HTML element into an {@link Article} object for a given category.
     *
     * Extracts the article's title, source URL, image URL (with fallback selectors and normalization), and description snippet.
     * Returns null if required elements are missing or an error occurs during parsing.
     *
     * @param articleElement the Jsoup HTML element representing the article
     * @param categoryId the category ID associated with the article
     * @return an {@link Article} object with extracted data, or null if parsing fails
     */
    public static Article parseArticleElement(Element articleElement, String categoryId) {
        try {
            // Extract title and URL
            Element titleElement = articleElement.selectFirst("h3.title-news > a");
            if (titleElement == null) return null;
            
            String title = titleElement.text();
            String sourceUrl = titleElement.attr("href");
            if (!sourceUrl.startsWith("http")) {
                sourceUrl = BASE_URL + sourceUrl;
            }
            
            // Extract image URL
            Element imageElement = articleElement.selectFirst("div.thumb-art > a > img");
            String imageUrl = "";
            if (imageElement != null) {
                imageUrl = imageElement.attr("data-src");
                if (imageUrl.isEmpty()) {
                    imageUrl = imageElement.attr("src");
                }
                
                // Fix image URL if needed
                if (!imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                    imageUrl = "https:" + imageUrl;
                }
                
                Log.d("VnExpressParser", "Image URL for article '" + title + "': " + imageUrl);
            } else {
                // Try alternative selectors for images
                imageElement = articleElement.selectFirst("img.lazy");
                if (imageElement != null) {
                    imageUrl = imageElement.attr("data-src");
                    if (imageUrl.isEmpty()) {
                        imageUrl = imageElement.attr("src");
                    }
                    
                    if (!imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                        imageUrl = "https:" + imageUrl;
                    }
                    
                    Log.d("VnExpressParser", "Found image with alternative selector: " + imageUrl);
                }
            }
            
            // Extract description/content snippet
            Element descElement = articleElement.selectFirst("p.description");
            String content = descElement != null ? descElement.text() : "";
            
            // Create article
            String id = UUID.randomUUID().toString();
            String categoryName = CATEGORY_MAP.getOrDefault(categoryId, "Tin tức");
            
            return new Article(
                    id,
                    title,
                    content,
                    imageUrl,
                    sourceUrl,
                    "VnExpress",
                    categoryId,
                    categoryName,
                    new Date()
            );
        } catch (Exception e) {
            Log.e("VnExpressParser", "Error parsing article element: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Parses detailed information for a single article from the provided HTML string.
     *
     * Extracts the article's title, main image URL (with fallback selectors), and full content paragraphs.
     * Returns an {@link Article} object containing the parsed details, or null if required elements are missing or parsing fails.
     *
     * @param html the HTML content of the article detail page
     * @param categoryId the category ID associated with the article
     * @return an {@link Article} object with detailed content, or null if parsing fails
     */
    public static Article parseArticleDetail(String html, String categoryId) {
        try {
            Document doc = Jsoup.parse(html);
            
            // Extract title
            Element titleElement = doc.selectFirst("h1.title-detail");
            if (titleElement == null) return null;
            String title = titleElement.text();
            
            // Extract main image
            Element imageElement = doc.selectFirst("div.fig-picture > picture > img");
            String imageUrl = "";
            if (imageElement != null) {
                imageUrl = imageElement.attr("data-src");
                if (imageUrl.isEmpty()) {
                    imageUrl = imageElement.attr("src");
                }
                
                // Fix image URL if needed
                if (!imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                    imageUrl = "https:" + imageUrl;
                }
                
                Log.d("VnExpressParser", "Detail image URL: " + imageUrl);
            } else {
                // Try alternative selectors for images
                imageElement = doc.selectFirst("img.lazy");
                if (imageElement != null) {
                    imageUrl = imageElement.attr("data-src");
                    if (imageUrl.isEmpty()) {
                        imageUrl = imageElement.attr("src");
                    }
                    
                    if (!imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                        imageUrl = "https:" + imageUrl;
                    }
                    
                    Log.d("VnExpressParser", "Found detail image with alternative selector: " + imageUrl);
                }
            }
            
            // Extract content
            StringBuilder contentBuilder = new StringBuilder();
            Elements contentElements = doc.select("article.fck_detail > p.Normal");
            for (Element p : contentElements) {
                contentBuilder.append(p.text()).append("\n\n");
            }
            String content = contentBuilder.toString().trim();
            
            // Create article
            String id = UUID.randomUUID().toString();
            String categoryName = CATEGORY_MAP.getOrDefault(categoryId, "Tin tức");
            
            return new Article(
                    id,
                    title,
                    content,
                    imageUrl,
                    doc.location(),
                    "VnExpress",
                    categoryId,
                    categoryName,
                    new Date()
            );
        } catch (Exception e) {
            Log.e("VnExpressParser", "Error parsing article detail: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Fetches and parses the latest articles from the VnExpress homepage.
     *
     * Connects to the homepage, extracts up to 20 recent articles, and attempts to determine each article's category.
     *
     * @return a list of the latest parsed Article objects, or an empty list if fetching or parsing fails
     */
    public static List<Article> fetchLatestArticles() {
        List<Article> articles = new ArrayList<>();
        
        try {
            Document doc = Jsoup.connect(BASE_URL).get();
            
            Elements articleElements = doc.select("article.item-news");
            for (Element articleElement : articleElements) {
                // Determine category
                String categoryId = "thoi-su"; // Default
                
                Element categoryLink = articleElement.selectFirst("a.cat");
                if (categoryLink != null) {
                    String href = categoryLink.attr("href");
                    for (String cat : CATEGORY_MAP.keySet()) {
                        if (href.contains("/" + cat)) {
                            categoryId = cat;
                            break;
                        }
                    }
                }
                
                Article article = parseArticleElement(articleElement, categoryId);
                if (article != null) {
                    articles.add(article);
                }
                
                // Limit to 20 articles
                if (articles.size() >= 20) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return articles;
    }
} 