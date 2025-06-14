package com.example.appdocbao.api;

import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.data.model.Category;
import com.example.appdocbao.data.News;

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
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

public class VnExpressParser {

    public static final String BASE_URL = "https://vnexpress.net";
    private static final Map<String, String> CATEGORY_MAP = initCategoryMap();
    private static final Map<String, Article> articleCache = new ConcurrentHashMap<>();

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

    public static List<Category> parseCategories() {
        List<Category> categories = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : CATEGORY_MAP.entrySet()) {
            String categoryId = entry.getKey();
            String categoryName = entry.getValue();
            
            String emoji = "📰";       
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

    // Method để tương thích với HomeActivity
    public List<News> parseNews(String html, int categoryId) {
        List<News> newsList = new ArrayList<>();

        try {
            if (html == null || html.isEmpty()) {
                Log.e("VnExpressParser", "Empty HTML content for parseNews");
                return newsList;
            }

            Document doc = Jsoup.parse(html);
            Log.d("VnExpressParser", "parseNews - Parsed HTML document with title: " + doc.title());

            Elements articleElements = doc.select("article.item-news");
            Log.d("VnExpressParser", "parseNews - Found " + articleElements.size() + " article elements");

            if (articleElements.isEmpty()) {
                // Try alternative CSS selectors
                articleElements = doc.select("article.item-news-common");
                Log.d("VnExpressParser", "parseNews - Second attempt found " + articleElements.size() + " article elements");

                if (articleElements.isEmpty()) {
                    // Try more general selector
                    articleElements = doc.select("article");
                    Log.d("VnExpressParser", "parseNews - Last attempt found " + articleElements.size() + " article elements");
                }
            }

            for (Element articleElement : articleElements) {
                try {
                    News news = parseNewsElement(articleElement, categoryId);
                    if (news != null) {
                        newsList.add(news);
                        Log.d("VnExpressParser", "parseNews - Added news: " + news.getTitle());
                    }
                } catch (Exception e) {
                    Log.e("VnExpressParser", "Error parsing individual news: " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            Log.e("VnExpressParser", "Error in parseNews: " + e.getMessage(), e);
        }

        Log.d("VnExpressParser", "parseNews - Returning " + newsList.size() + " news items");
        return newsList;
    }

    private News parseNewsElement(Element articleElement, int categoryId) {
        try {
            // Extract title and URL
            Element titleElement = articleElement.selectFirst("h3.title-news > a");
            if (titleElement == null) {
                titleElement = articleElement.selectFirst("h2.title-news > a");
            }
            if (titleElement == null) {
                titleElement = articleElement.selectFirst("h3 > a");
            }
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

                Log.d("VnExpressParser", "parseNewsElement - Image URL for news '" + title + "': " + imageUrl);
            } else {
                // Try alternative selectors for images
                imageElement = articleElement.selectFirst("img.lazy");
                if (imageElement == null) {
                    imageElement = articleElement.selectFirst("img");
                }
                if (imageElement != null) {
                    imageUrl = imageElement.attr("data-src");
                    if (imageUrl.isEmpty()) {
                        imageUrl = imageElement.attr("src");
                    }

                    if (!imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                        imageUrl = "https:" + imageUrl;
                    }

                    Log.d("VnExpressParser", "parseNewsElement - Found image with alternative selector: " + imageUrl);
                }
            }

            // Extract description/content snippet
            Element descElement = articleElement.selectFirst("p.description");
            String description = descElement != null ? descElement.text() : "";

            // Create News object
            String id = UUID.randomUUID().toString();
            String categoryName = getCategoryNameById(categoryId);

            News news = new News();
            news.setId(id);
            news.setTitle(title);
            news.setDescription(description);
            news.setImageUrl(imageUrl);
            news.setUrl(sourceUrl);
            news.setCategoryId(categoryId);
            news.setCategoryName(categoryName);
            news.setPublishedDate(new Date());

            return news;
        } catch (Exception e) {
            Log.e("VnExpressParser", "Error parsing news element: " + e.getMessage(), e);
            return null;
        }
    }

    private String getCategoryNameById(int categoryId) {
        switch (categoryId) {
            case 0: return "Bài viết nổi bật";
            case 1: return "Thời sự";
            case 2: return "Thế giới";
            case 3: return "Kinh doanh";
            case 4: return "Giải trí";
            case 5: return "Thể thao";
            case 6: return "Pháp luật";
            case 7: return "Giáo dục";
            case 8: return "Sức khỏe";
            default: return "Tin tức";
        }
    }

    public static Article getArticleFromCache(String articleId) {
        return articleCache.get(articleId);
    }

    public static void putArticleInCache(String articleId, Article article) {
        articleCache.put(articleId, article);
    }

    public static void clearCache() {
        articleCache.clear();
    }
}