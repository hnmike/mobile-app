# PSEUDOCODE - NewsRepositoryImproved

## 🏗️ KHỞI TẠO CLASS
```
CLASS NewsRepositoryImproved:
    // Singleton pattern
    STATIC instance = null
    
    // Các thành phần chính
    firestore = Firebase Database
    vnExpressService = API để gọi VnExpress
    executorService = Thread pool để xử lý background tasks
    context = Android Context
    
    // LiveData để theo dõi trạng thái
    categories = danh sách các chuyên mục
    articles = danh sách bài viết hiện tại
    bookmarkedArticles = danh sách bài viết đã lưu
    selectedArticle = bài viết đang được chọn
    isLoading = trạng thái đang tải
    errorMessage = thông báo lỗi
    
    CONSTRUCTOR(context):
        khởi tạo Firebase, API service, thread pool
        gọi loadCategories() để tải danh mục
    
    STATIC METHOD getInstance(context):
        IF instance == null:
            instance = new NewsRepositoryImproved(context)
        RETURN instance
```

## 🛠️ HELPER METHODS (Phương thức hỗ trợ)
```
METHOD handleError(operation, error, userMessage):
    // Xử lý lỗi tập trung
    đặt isLoading = false
    đặt errorMessage = userMessage
    ghi log lỗi

METHOD setLoadingState(loading, error):
    // Quản lý trạng thái loading tập trung
    đặt isLoading = loading
    IF error != null:
        đặt errorMessage = error

METHOD executeWithNetworkCheck(onlineAction, offlineAction):
    // Kiểm tra mạng trước khi thực hiện
    IF có kết nối internet:
        thực hiện onlineAction
    ELSE:
        IF offlineAction != null:
            thực hiện offlineAction
        ELSE:
            báo lỗi "Không có kết nối internet"

METHOD executeAsync(task, onSuccess, errorMessage):
    // Thực hiện task bất đồng bộ với error handling
    chạy task trong background thread:
        TRY:
            result = thực hiện task
            gọi onSuccess(result)
        CATCH Exception:
            xử lý lỗi với errorMessage

METHOD getCurrentUserId():
    // Lấy ID người dùng hiện tại
    IF user đã đăng nhập:
        RETURN user ID
    ELSE:
        RETURN null
```

## 📂 QUẢN LÝ DANH MỤC (CATEGORIES)
```
METHOD loadCategories():
    executeWithNetworkCheck(
        online: loadCategoriesFromFirestore(),
        offline: createDefaultCategories()
    )

METHOD loadCategoriesFromFirestore():
    đặt loading = true
    
    truy vấn Firebase collection "categories":
        ON SUCCESS:
            đặt loading = false
            IF không có data:
                gọi createCategories()
            ELSE:
                chuyển đổi data thành List<Category>
                cập nhật categories LiveData
        
        ON FAILURE:
            xử lý lỗi
            gọi createDefaultCategories() làm fallback

METHOD createCategories():
    chạy async:
        phân tích categories từ VnExpress
        cập nhật categories LiveData
        lưu vào Firebase async

METHOD createDefaultCategories():
    // Fallback khi offline
    tạo danh sách categories mặc định
    cập nhật categories LiveData
```

## 📰 QUẢN LÝ BÀI VIẾT (ARTICLES)
```
METHOD loadArticlesByCategory(categoryId):
    executeWithNetworkCheck(
        online: loadArticlesFromVnExpress(categoryId),
        offline: loadArticlesFromCache(categoryId)
    )

METHOD loadArticlesFromVnExpress(categoryId):
    đặt loading = true
    
    gọi API VnExpress với categoryId:
        ON SUCCESS:
            đặt loading = false
            IF có HTML content:
                gọi parseAndCacheArticles(html, categoryId)
            ELSE:
                fallback: loadArticlesFromCache(categoryId)
        
        ON FAILURE:
            xử lý lỗi
            fallback: loadArticlesFromCache(categoryId)

METHOD parseAndCacheArticles(html, categoryId):
    chạy async:
        phân tích HTML thành List<Article>
        IF có articles:
            lưu vào cache
            kiểm tra bookmark status
            cập nhật articles LiveData
        ELSE:
            fallback: loadArticlesFromCache(categoryId)

METHOD loadArticlesFromCache(categoryId):
    truy vấn Firebase articles với categoryId:
        sắp xếp theo ngày xuất bản
        giới hạn 20 bài
        
        ON SUCCESS:
            IF có articles:
                kiểm tra bookmark status
                cập nhật articles LiveData
            ELSE:
                báo lỗi "không có cache"
        
        ON FAILURE:
            xử lý lỗi

METHOD loadLatestArticles():
    executeWithNetworkCheck(
        online: fetchLatestArticles(),
        offline: null
    )

METHOD fetchLatestArticles():
    đặt loading = true
    
    chạy async:
        fetch latest articles từ VnExpress
        lưu vào cache
        kiểm tra bookmark status
        cập nhật articles LiveData
        đặt loading = false

METHOD loadArticleDetail(articleId):
    đặt loading = true
    
    truy vấn Firebase article với ID:
        ON SUCCESS:
            IF tìm thấy article:
                cập nhật selectedArticle LiveData
                kiểm tra bookmark status
                
                executeWithNetworkCheck(
                    online: fetchFullArticleContent(article),
                    offline: đặt loading = false
                )
            ELSE:
                báo lỗi "bài viết không tồn tại"
        
        ON FAILURE:
            xử lý lỗi

METHOD fetchFullArticleContent(article):
    IF article có sourceUrl:
        gọi API lấy HTML content:
            ON SUCCESS:
                cập nhật nội dung article
                lưu vào cache
                cập nhật selectedArticle LiveData
            
            ON FAILURE:
                ghi log warning (không báo lỗi)
    
    đặt loading = false
```

## 🔖 QUẢN LÝ BOOKMARK
```
METHOD loadBookmarkedArticles():
    userID = getCurrentUserId()
    IF userID == null:
        báo lỗi "cần đăng nhập"
        RETURN
    
    đặt loading = true
    
    truy vấn Firebase user data:
        ON SUCCESS:
            đặt loading = false
            
            IF user có bookmarked articles:
                gọi loadArticlesByIds(bookmarkedArticleIds)
            ELSE:
                cập nhật bookmarkedArticles = empty list
        
        ON FAILURE:
            xử lý lỗi

METHOD toggleBookmark(article):
    userID = getCurrentUserId()
    IF userID == null:
        báo lỗi "cần đăng nhập"
        RETURN
    
    đặt loading = true
    
    lấy user data từ Firebase:
        ON SUCCESS:
            IF article đã được bookmark:
                xóa bookmark
                article.setBookmarked(false)
            ELSE:
                thêm bookmark
                article.setBookmarked(true)
            
            cập nhật user data trong Firebase:
                ON SUCCESS:
                    đặt loading = false
                    cập nhật selectedArticle LiveData
                
                ON FAILURE:
                    xử lý lỗi
        
        ON FAILURE:
            xử lý lỗi

METHOD loadArticlesByIds(articleIds):
    IF articleIds rỗng:
        cập nhật bookmarkedArticles = empty list
        RETURN
    
    // Chia thành batches (Firestore giới hạn 10 items/query)
    chia articleIds thành các batch 10 items
    
    FOR mỗi batch:
        truy vấn Firebase articles với IDs trong batch:
            ON SUCCESS:
                thêm articles vào allArticles list
                đánh dấu bookmarked = true
                
                IF đây là batch cuối cùng:
                    cập nhật bookmarkedArticles LiveData
            
            ON FAILURE:
                ghi log lỗi
                IF đây là batch cuối cùng:
                    cập nhật bookmarkedArticles với articles đã load được
```

## 💾 CACHE OPERATIONS
```
METHOD cacheArticles(articles):
    FOR mỗi article trong articles:
        gọi cacheArticle(article)

METHOD cacheArticle(article):
    lưu article vào Firebase collection "articles":
        ON FAILURE:
            ghi log warning (không báo lỗi)

METHOD checkBookmarkedStatus(articleList):
    userID = getCurrentUserId()
    IF userID == null:
        RETURN
    
    lấy user data từ Firebase:
        ON SUCCESS:
            IF user có bookmarked articles:
                FOR mỗi article trong articleList:
                    IF article.id trong bookmarked list:
                        article.setBookmarked(true)
                    ELSE:
                        article.setBookmarked(false)
                
                cập nhật articles LiveData
        
        ON FAILURE:
            ghi log warning

METHOD checkBookmarkStatusForArticle(article):
    // Tương tự checkBookmarkedStatus nhưng cho 1 article
    // Cập nhật selectedArticle LiveData thay vì articles
```

## 🔍 CONTENT EXTRACTION
```
METHOD extractArticleContent(html, originalArticle):
    IF html rỗng:
        RETURN originalArticle
    
    tạo copy của originalArticle
    
    sử dụng Jsoup để parse HTML:
        tìm content elements với selector "article.fck_detail p.Normal"
        
        IF tìm thấy content:
            ghép tất cả paragraphs thành string
            cập nhật article.content
        
        tìm image element với selector "div.fig-picture img"
        
        IF tìm thấy image:
            lấy image URL từ data-src hoặc src
            IF URL không có protocol:
                thêm "https:" vào đầu
            cập nhật article.imageUrl
    
    RETURN updated article
```

## 📋 FLOW TỔNG QUAN

### Khi mở app:
1. Khởi tạo NewsRepositoryImproved (Singleton)
2. Tự động load categories (online → fallback offline)

### Khi chọn category:
1. Kiểm tra network
2. Online: Load từ VnExpress → parse → cache → hiển thị
3. Offline: Load từ cache → hiển thị

### Khi bookmark:
1. Kiểm tra user đăng nhập
2. Toggle bookmark status
3. Cập nhật Firebase user data
4. Cập nhật UI

### Khi xem chi tiết bài viết:
1. Load basic info từ cache
2. Nếu online: fetch full content từ source
3. Update và cache full content 