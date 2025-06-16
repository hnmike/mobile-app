# NewsRepository - Sơ đồ Logic Flow

## 1. Sơ đồ tổng quan luồng hoạt động

```
┌─────────────────────────────────────────────────────────────────┐
│                    NewsRepository Initialization                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│  │ Firestore   │    │ VnExpress   │    │ Executor    │         │
│  │ Service     │    │ Service     │    │ Service     │         │
│  └─────────────┘    └─────────────┘    └─────────────┘         │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Load Categories                          │
│                                                                 │
│  Network Available? ──No──► Load Default Categories            │
│         │                                                       │
│        Yes                                                      │
│         │                                                       │
│         ▼                                                       │
│  Firestore Empty? ──Yes──► Create & Save Categories            │
│         │                                                       │
│        No                                                       │
│         │                                                       │
│         ▼                                                       │
│    Load from Firestore                                         │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Articles Management                       │
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │Load by      │  │Load Latest  │  │Load Detail  │            │
│  │Category     │  │Articles     │  │Article      │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
│         │                 │                 │                  │
│         ▼                 ▼                 ▼                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │VnExpress API│  │VnExpress API│  │Firestore    │            │
│  │+ Cache      │  │+ Cache      │  │+ VnExpress  │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Bookmark Management                          │
│                                                                 │
│  User Logged In? ──No──► Show Login Required Error             │
│         │                                                       │
│        Yes                                                      │
│         │                                                       │
│         ▼                                                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │Add Bookmark │  │Remove       │  │Load         │            │
│  │             │  │Bookmark     │  │Bookmarked   │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
│         │                 │                 │                  │
│         ▼                 ▼                 ▼                  │
│    Update User Document in Firestore                           │
└─────────────────────────────────────────────────────────────────┘
```

## 2. Chi tiết luồng Load Articles by Category

```
┌─────────────────────────────────────────────────────────────────┐
│                 loadArticlesByCategory(categoryId)              │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
                    ┌─────────────────────┐
                    │ Check Network       │
                    └─────────────────────┘
                                │
                  ┌─────────────┴─────────────┐
                  │                           │
                 No                          Yes
                  │                           │
                  ▼                           ▼
    ┌─────────────────────┐        ┌─────────────────────┐
    │ loadArticlesFromCache│        │ Call VnExpress API  │
    └─────────────────────┘        └─────────────────────┘
                                                │
                                                ▼
                                   ┌─────────────────────┐
                                   │ Parse HTML Content  │
                                   └─────────────────────┘
                                                │
                                   ┌─────────────┴─────────────┐
                                   │                           │
                               Success                       Error
                                   │                           │
                                   ▼                           ▼
                      ┌─────────────────────┐    ┌─────────────────────┐
                      │ Cache Articles      │    │ loadArticlesFromCache│
                      │ Check Bookmarks     │    └─────────────────────┘
                      │ Update UI           │
                      └─────────────────────┘
```

## 3. Các vấn đề hiện tại trong code

### 3.1 Quá nhiều try-catch và error handling
- Mỗi method đều có riêng error handling
- Code trùng lặp nhiều
- Khó maintain

### 3.2 Logic phức tạp trong từng method
- Method `loadArticlesByCategory` quá dài (80+ lines)
- Method `fetchFullArticleContent` phức tạp
- Nhiều nested callbacks

### 3.3 Duplicate code
- Cache logic lặp lại
- Bookmark checking logic tương tự
- Error message setting pattern giống nhau

## 4. Đề xuất cải thiện

### 4.1 Tạo Error Handler tập trung
```java
private void handleError(String operation, Throwable error, String userMessage) {
    isLoading.postValue(false);
    errorMessage.postValue(userMessage);
    Log.e(TAG, operation + ": ", error);
}
```

### 4.2 Tạo Network Helper
```java
private void executeWithNetworkCheck(Runnable onlineAction, Runnable offlineAction) {
    if (NetworkUtils.isNetworkAvailable(context)) {
        onlineAction.run();
    } else {
        offlineAction.run();
    }
}
```

### 4.3 Simplify Cache Operations
```java
private void cacheAndUpdateUI(List<Article> articles) {
    cacheArticles(articles);
    checkBookmarkedStatus(articles);
    this.articles.postValue(articles);
}
```

### 4.4 Extract Common Patterns
```java
private <T> void executeAsync(Supplier<T> task, Consumer<T> onSuccess) {
    executorService.execute(() -> {
        try {
            T result = task.get();
            onSuccess.accept(result);
        } catch (Exception e) {
            handleError("async_operation", e, "Lỗi thực hiện thao tác");
        }
    });
}
```

## 5. Cấu trúc code được đề xuất

### 5.1 Chia nhỏ responsibilities
- `NetworkManager`: Xử lý network calls
- `CacheManager`: Xử lý cache operations  
- `BookmarkManager`: Xử lý bookmark operations
- `ErrorHandler`: Xử lý errors tập trung

### 5.2 Sử dụng Builder pattern cho complex operations
```java
new ArticleLoader()
    .categoryId(categoryId)
    .withCache(true)
    .withBookmarkCheck(true)
    .onSuccess(articles -> this.articles.postValue(articles))
    .onError(error -> handleError("load_articles", error, "Không thể tải bài viết"))
    .execute();
```

### 5.3 State management đơn giản hơn
```java
private void setLoadingState(boolean loading, String error) {
    isLoading.postValue(loading);
    if (error != null) {
        errorMessage.postValue(error);
    }
}
``` 