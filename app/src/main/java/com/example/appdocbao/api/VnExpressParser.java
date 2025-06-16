/**
 * Lớp VnExpressParser chịu trách nhiệm phân tích (parse) nội dung HTML từ trang VnExpress
 * để trích xuất thông tin về các danh mục (categories) và bài báo (articles).
 * Nó sử dụng thư viện Jsoup để thao tác với cây DOM của HTML.
 * Lớp này cũng bao gồm một bộ nhớ cache đơn giản để lưu trữ thông tin bài viết đã phân tích.
 *
 * Tác giả: Tran Quy Dinh
 * Ngày tạo: [Ngày bạn tạo file, ví dụ: 27/05/2024]
 * Người sửa đổi:
 */
package com.example.appdocbao.api;

import com.example.appdocbao.data.model.Article;
import com.example.appdocbao.data.model.Category;
import com.example.appdocbao.data.News;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException; // Thêm import này nếu bạn có ý định sử dụng SimpleDateFormat ở đâu đó mà chưa import
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

    /**
     * Khởi tạo và trả về một Map chứa ánh xạ giữa ID danh mục (dạng slug) và tên danh mục tiếng Việt.
     * @return Map chứa thông tin các danh mục.
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

    /**
     * Phân tích và tạo danh sách các đối tượng Category từ CATEGORY_MAP.
     * Mỗi Category sẽ có ID, tên, mô tả và một emoji đại diện.
     * @return Danh sách các Category.
     */
    public static List<Category> parseCategories() {
        List<Category> categories = new ArrayList<>();

        for (Map.Entry<String, String> entry : CATEGORY_MAP.entrySet()) {
            String categoryId = entry.getKey();
            String categoryName = entry.getValue();

            // Tạo ánh xạ emoji cho các danh mục
            String emoji = "📰"; // Mặc định
            // (Các điều kiện if-else để gán emoji)
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

    /**
     * Phân tích nội dung HTML để trích xuất danh sách các bài báo thuộc một danh mục cụ thể.
     * Sử dụng Jsoup để tìm các phần tử HTML chứa thông tin bài báo.
     * Có thử các CSS selector khác nhau nếu selector chính không tìm thấy bài báo.
     * @param html Nội dung HTML cần phân tích.
     * @param categoryId ID của danh mục (dạng slug) để liên kết với các bài báo.
     * @return Danh sách các Article đã được phân tích.
     */
    public static List<Article> parseArticlesByCategory(String html, String categoryId) {
        List<Article> articles = new ArrayList<>();

        try {
            if (html == null || html.isEmpty()) {
                Log.e("VnExpressParser", "Empty HTML content");
                return articles; // Trả về danh sách rỗng nếu HTML trống
            }

            Document doc = Jsoup.parse(html); // Phân tích HTML thành đối tượng Document
            Log.d("VnExpressParser", "Parsed HTML document with title: " + doc.title());

            // Thử selector chính để tìm các phần tử bài báo
            Elements articleElements = doc.select("article.item-news");
            Log.d("VnExpressParser", "Found " + articleElements.size() + " article elements with 'article.item-news'");

            // Nếu không tìm thấy, thử các selector thay thế
            if (articleElements.isEmpty()) {
                articleElements = doc.select("article.item-news-common");
                Log.d("VnExpressParser", "Second attempt found " + articleElements.size() + " article elements with 'article.item-news-common'");

                if (articleElements.isEmpty()) {
                    articleElements = doc.select("article"); // Thử selector chung hơn
                    Log.d("VnExpressParser", "Last attempt found " + articleElements.size() + " article elements with 'article'");
                }
            }

            // Duyệt qua từng phần tử bài báo tìm được và phân tích thông tin
            for (Element articleElement : articleElements) {
                try {
                    Article article = parseArticleElement(articleElement, categoryId); // Gọi hàm phân tích chi tiết từng bài
                    if (article != null) {
                        articles.add(article);
                        Log.d("VnExpressParser", "Added article: " + article.getTitle());
                    }
                } catch (Exception e) {
                    Log.e("VnExpressParser", "Error parsing individual article: " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            Log.e("VnExpressParser", "Error parsing articles by category: " + e.getMessage(), e);
        }

        Log.d("VnExpressParser", "Returning " + articles.size() + " articles for category " + categoryId);
        return articles;
    }

    /**
     * Phân tích một phần tử HTML (Element) đại diện cho một bài báo để trích xuất thông tin chi tiết.
     * @param articleElement Phần tử HTML chứa thông tin bài báo.
     * @param categoryId ID của danh mục (dạng slug) của bài báo này.
     * @return Đối tượng Article chứa thông tin đã trích xuất, hoặc null nếu có lỗi.
     */
    public static Article parseArticleElement(Element articleElement, String categoryId) {
        try {
            // Trích xuất tiêu đề và URL nguồn của bài báo
            Element titleElement = articleElement.selectFirst("h3.title-news > a");
            if (titleElement == null) {
                Log.w("VnExpressParser", "Title element not found in article element.");
                return null; // Không tìm thấy tiêu đề, không thể tạo bài báo
            }

            String title = titleElement.text();
            String sourceUrl = titleElement.attr("href");
            // Đảm bảo URL nguồn là URL đầy đủ
            if (!sourceUrl.startsWith("http")) {
                sourceUrl = BASE_URL + sourceUrl;
            }

            // Trích xuất URL hình ảnh
            Element imageElement = articleElement.selectFirst("div.thumb-art > a > img");
            String imageUrl = "";
            if (imageElement != null) {
                imageUrl = imageElement.attr("data-src"); // Ưu tiên 'data-src' cho lazy loading
                if (imageUrl.isEmpty()) {
                    imageUrl = imageElement.attr("src"); // Nếu không có 'data-src', thử 'src'
                }

                // Sửa lỗi URL hình ảnh nếu cần (thêm https:)
                if (!imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                    imageUrl = "https:" + imageUrl;
                }
                Log.d("VnExpressParser", "Image URL for article '" + title + "': " + imageUrl);
            } else {
                // Thử selector thay thế cho hình ảnh
                imageElement = articleElement.selectFirst("img.lazy");
                if (imageElement != null) {
                    imageUrl = imageElement.attr("data-src");
                    if (imageUrl.isEmpty()) {
                        imageUrl = imageElement.attr("src");
                    }
                    if (!imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                        imageUrl = "https:" + imageUrl;
                    }
                    Log.d("VnExpressParser", "Found image with alternative selector for '" + title + "': " + imageUrl);
                } else {
                    Log.w("VnExpressParser", "Image element not found for article: " + title);
                }
            }

            // Trích xuất mô tả ngắn/đoạn trích nội dung
            Element descElement = articleElement.selectFirst("p.description");
            String content = descElement != null ? descElement.text() : ""; // Lấy text nếu phần tử tồn tại, ngược lại là chuỗi rỗng

            // Tạo đối tượng Article
            String id = UUID.randomUUID().toString(); // Tạo ID duy nhất cho bài báo
            String categoryName = CATEGORY_MAP.getOrDefault(categoryId, "Tin tức"); // Lấy tên danh mục, mặc định là "Tin tức"

            return new Article(
                    id,
                    title,
                    content, // Đây là mô tả ngắn, không phải nội dung đầy đủ
                    imageUrl,
                    sourceUrl,
                    "VnExpress", // Tên nguồn báo
                    categoryId,
                    categoryName,
                    new Date() // Ngày xuất bản được gán là thời điểm hiện tại khi parse
            );
        } catch (Exception e) {
            Log.e("VnExpressParser", "Error parsing article element: " + e.getMessage(), e);
            return null; // Trả về null nếu có lỗi xảy ra
        }
    }

    /**
     * Phân tích nội dung HTML của trang chi tiết một bài báo để trích xuất thông tin đầy đủ.
     * @param html Nội dung HTML của trang chi tiết bài báo.
     * @param categoryId ID của danh mục (dạng slug) mà bài báo này có thể thuộc về.
     * @return Đối tượng Article chứa thông tin chi tiết, hoặc null nếu có lỗi.
     */
    public static Article parseArticleDetail(String html, String categoryId) {
        try {
            Document doc = Jsoup.parse(html);

            // Trích xuất tiêu đề chi tiết
            Element titleElement = doc.selectFirst("h1.title-detail");
            if (titleElement == null) {
                Log.w("VnExpressParser", "Detail title element not found.");
                return null;
            }
            String title = titleElement.text();

            // Trích xuất hình ảnh chính của bài báo
            Element imageElement = doc.selectFirst("div.fig-picture > picture > img");
            String imageUrl = "";
            if (imageElement != null) {
                imageUrl = imageElement.attr("data-src");
                if (imageUrl.isEmpty()) {
                    imageUrl = imageElement.attr("src");
                }
                if (!imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                    imageUrl = "https:" + imageUrl;
                }
                Log.d("VnExpressParser", "Detail image URL: " + imageUrl);
            } else {
                // Thử selector thay thế cho hình ảnh chi tiết
                imageElement = doc.selectFirst("img.lazy"); // Thường dùng cho lazy loading
                if (imageElement != null) {
                    imageUrl = imageElement.attr("data-src");
                    if (imageUrl.isEmpty()) {
                        imageUrl = imageElement.attr("src");
                    }
                    if (!imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                        imageUrl = "https:" + imageUrl;
                    }
                    Log.d("VnExpressParser", "Found detail image with alternative selector: " + imageUrl);
                } else {
                    Log.w("VnExpressParser", "Detail image element not found for article: " + title);
                }
            }

            // Trích xuất nội dung đầy đủ của bài báo
            StringBuilder contentBuilder = new StringBuilder();
            Elements contentElements = doc.select("article.fck_detail > p.Normal"); // Selector cho các đoạn văn bản
            if (contentElements.isEmpty()){
                // Thử một selector khác nếu không tìm thấy nội dung
                contentElements = doc.select("article.fck_detail p"); // Lấy tất cả các thẻ <p> trong <article class="fck_detail">
                Log.d("VnExpressParser", "Trying alternative content selector, found: " + contentElements.size());
            }

            for (Element p : contentElements) {
                contentBuilder.append(p.text()).append("\n\n"); // Nối văn bản từ mỗi đoạn, thêm dòng mới
            }
            String content = contentBuilder.toString().trim(); // Nội dung đầy đủ

            // Tạo đối tượng Article
            String id = UUID.randomUUID().toString();
            String categoryName = CATEGORY_MAP.getOrDefault(categoryId, "Tin tức");

            return new Article(
                    id,
                    title,
                    content, // Nội dung đầy đủ
                    imageUrl,
                    doc.location(), // URL của trang chi tiết, lấy trực tiếp từ Document
                    "VnExpress",
                    categoryId,
                    categoryName,
                    new Date() // Ngày xuất bản được gán là thời điểm hiện tại
            );
        } catch (Exception e) {
            Log.e("VnExpressParser", "Error parsing article detail: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lấy và phân tích các bài báo mới nhất từ trang chủ VnExpress.
     * Giới hạn số lượng bài báo lấy về (mặc định là 20).
     * @return Danh sách các Article mới nhất.
     */
    public static List<Article> fetchLatestArticles() {
        List<Article> articles = new ArrayList<>();

        try {
            // Kết nối đến trang chủ VnExpress và lấy nội dung HTML
            Document doc = Jsoup.connect(BASE_URL).get();

            Elements articleElements = doc.select("article.item-news"); // Selector cho các bài báo trên trang chủ
            for (Element articleElement : articleElements) {
                // Xác định danh mục của bài báo dựa trên link của danh mục
                String categoryId = "thoi-su"; // Mặc định là "Thời sự"
                Element categoryLink = articleElement.selectFirst("a.cat"); // Selector cho link danh mục
                if (categoryLink != null) {
                    String href = categoryLink.attr("href");
                    for (String catKey : CATEGORY_MAP.keySet()) {
                        if (href.contains("/" + catKey)) { // Kiểm tra xem href có chứa slug của danh mục không
                            categoryId = catKey;
                            break;
                        }
                    }
                }

                Article article = parseArticleElement(articleElement, categoryId); // Phân tích từng bài báo
                if (article != null) {
                    articles.add(article);
                }

                // Giới hạn số lượng bài báo lấy về
                if (articles.size() >= 20) {
                    break;
                }
            }
        } catch (IOException e) {
            Log.e("VnExpressParser", "Error fetching latest articles: " + e.getMessage(), e);
            // e.printStackTrace(); // Có thể hữu ích khi debug, nhưng Log.e thường đủ cho sản phẩm
        }

        return articles;
    }

    /**
     * Phân tích HTML để lấy danh sách các đối tượng `News` (một lớp dữ liệu khác).
     * Chuyển đổi từ `Article` (đã parse) sang `News`.
     * @param html Nội dung HTML để phân tích.
     * @param categoryId ID dạng số nguyên của danh mục.
     * @return Danh sách các đối tượng `News`.
     */
    public List<com.example.appdocbao.data.News> parseNews(String html, int categoryId) {
        List<com.example.appdocbao.data.News> newsList = new ArrayList<>();

        // Chuyển đổi categoryId (int) sang categoryId dạng chuỗi (slug)
        String categoryStringId = getCategoryIdFromInt(categoryId);
        // Nếu categoryId không hợp lệ (và không phải là tin nổi bật/trang chủ) thì trả về danh sách rỗng
        if (categoryStringId == null && categoryId != 0) { // categoryId 0 có thể là trang chủ/tin nổi bật
            Log.e("VnExpressParser", "Invalid category ID for parseNews: " + categoryId);
            return newsList;
        }

        List<Article> articles;
        // Nếu categoryId là 0 (ví dụ: tin nổi bật), phân tích từ một categoryId mặc định hoặc trang chủ
        if (categoryId == 0) {
            // "tin-tuc-24h" có thể là một slug đại diện cho trang chủ hoặc tin tổng hợp
            articles = parseArticlesByCategory(html, "tin-tuc-24h");
        } else {
            articles = parseArticlesByCategory(html, categoryStringId);
        }

        // Chuyển đổi từ danh sách Article sang danh sách News
        int newsIdCounter = categoryId * 1000 + 1; // Tạo ID ban đầu cho News để tránh trùng lặp giữa các danh mục
        for (Article article : articles) {
            // Xác định xem tin có phải là tin nổi bật hay không (ví dụ: 3 tin đầu tiên)
            boolean isFeatured = (newsIdCounter % 1000) <= 3; // Kiểm tra 3 bài đầu tiên dựa trên counter

            // ID này được sử dụng để lưu Article vào cache, cần đảm bảo tính duy nhất và ổn định nếu có thể
            // Hiện tại đang dùng newsIdCounter, có thể cân nhắc dùng article.getId() nếu nó ổn định hơn
            String cacheKeyId = String.valueOf(newsIdCounter);
            articleCache.put(cacheKeyId, article); // Lưu Article vào cache

            com.example.appdocbao.data.News newsItem = new com.example.appdocbao.data.News(
                    newsIdCounter++, // Tăng newsIdCounter cho bài tiếp theo
                    article.getTitle(),
                    article.getContent(), // Lưu ý: content này từ parseArticleElement là mô tả ngắn
                    article.getImageUrl(),
                    formatDate(article.getPublishedTime()), // Định dạng ngày xuất bản
                    categoryId, // ID danh mục dạng số nguyên
                    isFeatured
            );

            newsList.add(newsItem);

            // Giới hạn số lượng tin tức cho mỗi danh mục (ví dụ: 10 tin)
            if (newsList.size() >= 10) {
                break;
            }
        }
        Log.d("VnExpressParser", "Parsed " + newsList.size() + " news items for category int ID: " + categoryId);
        return newsList;
    }

    /**
     * Chuyển đổi ID danh mục dạng số nguyên (int) sang ID danh mục dạng chuỗi (slug).
     * @param categoryId ID danh mục dạng số nguyên.
     * @return ID danh mục dạng chuỗi (slug), hoặc null nếu không tìm thấy.
     */
    private String getCategoryIdFromInt(int categoryId) {
        switch (categoryId) {
            case 1: return "thoi-su";
            case 2: return "the-gioi";
            case 3: return "kinh-doanh";
            case 4: return "giai-tri";
            case 5: return "the-thao";
            case 6: return "phap-luat";
            case 7: return "giao-duc";
            case 8: return "suc-khoe";
            case 9: return "doi-song";
            case 10: return "du-lich";
            case 11: return "khoa-hoc";
            case 12: return "so-hoa"; // "Số hóa"
            case 13: return "xe";
            case 14: return "y-kien";
            case 15: return "tam-su";
            default:
                Log.w("VnExpressParser", "Unknown integer category ID: " + categoryId);
                return null; // Trả về null cho ID không xác định
        }
    }

    /**
     * Định dạng đối tượng Date thành một chuỗi theo mẫu "HH:mm - dd/MM/yyyy".
     * @param date Đối tượng Date cần định dạng.
     * @return Chuỗi ngày đã định dạng, hoặc chuỗi rỗng nếu date là null.
     */
    private String formatDate(Date date) {
        if (date == null) {
            return ""; // Trả về chuỗi rỗng nếu không có ngày
        }
        // Sử dụng java.text.SimpleDateFormat để định dạng ngày
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Lấy một đối tượng Article từ bộ nhớ cache dựa trên ID của nó.
     * @param id ID của bài báo cần lấy (thường là ID đã dùng khi đưa vào cache, ví dụ từ newsIdCounter).
     * @return Đối tượng Article nếu tìm thấy trong cache, ngược lại là null.
     */
    public static Article getArticleFromCache(String id) {
        return articleCache.get(id);
    }

    /**
     * Thêm hoặc cập nhật một đối tượng Article vào bộ nhớ cache.
     * @param id ID để lưu trữ bài báo trong cache.
     * @param article Đối tượng Article cần lưu.
     */
    public static void putArticleInCache(String id, Article article) {
        if (id != null && article != null) {
            articleCache.put(id, article);
        } else {
            Log.w("VnExpressParser", "Attempted to put null id or article in cache.");
        }
    }
}