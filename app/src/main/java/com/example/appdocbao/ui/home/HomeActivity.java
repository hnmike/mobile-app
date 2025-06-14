package com.example.appdocbao.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.appdocbao.ui.newsdetail.NewsDetailActivity;
import com.example.appdocbao.R;
import com.example.appdocbao.data.News;
import com.example.appdocbao.data.model.Category;
import com.example.appdocbao.ui.newslist.NewsAdapter;
import com.example.appdocbao.ui.newslist.NewsListActivity;
import com.example.appdocbao.ui.bookmarks.BookmarksActivity;
import com.example.appdocbao.ui.categories.CategoriesActivity;
import com.example.appdocbao.ui.profile.ProfileActivity;
import com.example.appdocbao.api.RetrofitClient;
import com.example.appdocbao.api.VnExpressService;
import com.example.appdocbao.api.VnExpressParser;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private RecyclerView rvCategories;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNavigationView;
    
    private HomeCategoriesAdapter homeCategoriesAdapter;
    
    private List<News> allNewsList = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private Executor executor = Executors.newSingleThreadExecutor();
    private VnExpressService vnExpressService;
    private VnExpressParser vnExpressParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Không hiển thị nút back ở trang chủ
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("Trang chủ");
        }
        
        // Khởi tạo các view
        rvCategories = findViewById(R.id.rvCategories);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        
        // Khởi tạo API service
        vnExpressService = RetrofitClient.getClient().create(VnExpressService.class);
        vnExpressParser = new VnExpressParser();
        
        // Thiết lập SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadDataFromApi);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        
        // Thiết lập RecyclerView cho danh mục và bài viết nổi bật của mỗi danh mục
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        homeCategoriesAdapter = new HomeCategoriesAdapter(this, new ArrayList<>(), new HashMap<>());
        rvCategories.setAdapter(homeCategoriesAdapter);
        
        // Thiết lập bottom navigation
        setupBottomNavigation();
        
        // Tải dữ liệu
        loadDataFromApi();
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) {
            Log.w(TAG, "Bottom navigation view is null, possibly not in the layout");
            return;
        }
        
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            try {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    // Đã ở Home rồi
                    return true;
                } else if (itemId == R.id.nav_categories) {
                    // Chuyển đến CategoriesActivity
                    Intent intent = new Intent(HomeActivity.this, CategoriesActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_bookmarks) {
                    // Chuyển đến BookmarksActivity
                    Intent intent = new Intent(HomeActivity.this, BookmarksActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    // Kiểm tra login và chuyển đến trang phù hợp
                    if (isUserLoggedIn()) {
                        // Nếu đã đăng nhập, mở ProfileActivity
                        Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
                        startActivity(profileIntent);
                    } else {
                        // Nếu chưa đăng nhập, mở SignInActivity
                        Intent loginIntent = new Intent();
                        loginIntent.setClassName(getPackageName(), "com.example.appdocbao.ui.auth.SignInActivity");
                        startActivity(loginIntent);
                    }
                    return true;
                }
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Error in navigation: " + e.getMessage(), e);
                return false;
            }
        });
    }

    /**
     * Kiểm tra trạng thái đăng nhập của người dùng
     * @return true nếu đã đăng nhập, false nếu chưa
     */
    private boolean isUserLoggedIn() {
        try {
            return com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status: " + e.getMessage(), e);
            return false;
        }
    }

    private void loadDataFromApi() {
        // Hiển thị loading indicator
        swipeRefreshLayout.setRefreshing(true);
        
        // Tạo danh mục
        categories = createCategories();
        
        // Thêm "danh mục" đặc biệt cho bài viết nổi bật ở đầu danh sách
        Category featuredCategory = new Category("0", "Bài viết nổi bật", "Các bài viết nổi bật trên hệ thống", "🔥");
        List<Category> allCategories = new ArrayList<>();
        allCategories.add(featuredCategory);
        allCategories.addAll(categories);
        
        // Tạo dữ liệu fake để test
        Map<Integer, List<News>> categoryNewsMap = createFakeNewsData();
        
        // Cập nhật UI với dữ liệu fake
        homeCategoriesAdapter.updateData(allCategories, categoryNewsMap);
        
        // Ẩn loading indicator
        swipeRefreshLayout.setRefreshing(false);
        
        Toast.makeText(this, "Đã tải " + allCategories.size() + " danh mục", Toast.LENGTH_SHORT).show();
    }
    
    private Map<Integer, List<News>> createFakeNewsData() {
        Map<Integer, List<News>> categoryNewsMap = new HashMap<>();
        
        for (int i = 0; i <= 8; i++) {
            List<News> newsList = new ArrayList<>();
            
            for (int j = 1; j <= 5; j++) {
                News news = new News();
                news.setId("news_" + i + "_" + j);
                news.setTitle("Tin tức " + getCategoryNameById(i) + " số " + j);
                news.setDescription("Mô tả ngắn cho bài viết " + j + " trong danh mục " + getCategoryNameById(i));
                news.setImageUrl("https://via.placeholder.com/300x200.png?text=News+" + i + "-" + j);
                news.setUrl("https://example.com/news/" + i + "/" + j);
                news.setCategoryId(i);
                news.setCategoryName(getCategoryNameById(i));
                news.setPublishedDate(new Date());
                newsList.add(news);
            }
            
            categoryNewsMap.put(i, newsList);
        }
        
        return categoryNewsMap;
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
    
    private void loadCategoriesNewsFromApi(List<Category> allCategories, Map<Integer, List<News>> categoryNewsMap, 
                                          final int[] processedCategories, final int totalCategories) {
        // Tải tin tức cho mỗi danh mục
        for (Category category : allCategories) {
            final int categoryId;
            try {
                categoryId = Integer.parseInt(category.getId());
            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to parse category ID: " + category.getId(), e);
                continue;
            }
            
            // Lấy URL tương ứng cho danh mục
            final String url = getCategoryUrl(categoryId);
            
            if (url != null) {
                // Sử dụng repository để tải bài viết từ API
                loadNewsForCategory(url, categoryId, category, categoryNewsMap, processedCategories, totalCategories);
            } else {
                // Không có URL cho danh mục này
                processedCategories[0]++;
                categoryNewsMap.put(categoryId, new ArrayList<>());
                
                checkAndUpdateUIIfComplete(categoryNewsMap, processedCategories[0], totalCategories);
            }
        }
    }
    
    private void loadNewsForCategory(String url, int categoryId, Category category, 
                                    Map<Integer, List<News>> categoryNewsMap, 
                                    final int[] processedCategories, final int totalCategories) {
        // Tạo bản sao final của categoryId để sử dụng trong lambda
        final int finalCategoryId = categoryId;
        
        vnExpressService.getHtmlContent(url).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                executor.execute(() -> {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            // Phân tích nội dung HTML để lấy danh sách bài viết
                            List<News> newsForCategory = vnExpressParser.parseNews(response.body(), finalCategoryId);
                            
                            // Cập nhật danh sách tin tức cho danh mục
                            runOnUiThread(() -> {
                                categoryNewsMap.put(finalCategoryId, newsForCategory);
                                processedCategories[0]++;
                                
                                // Kiểm tra và cập nhật UI nếu đã hoàn thành
                                checkAndUpdateUIIfComplete(categoryNewsMap, processedCategories[0], totalCategories);
                            });
                        } else {
                            runOnUiThread(() -> {
                                processedCategories[0]++;
                                categoryNewsMap.put(finalCategoryId, new ArrayList<>());
                                
                                // Kiểm tra và cập nhật UI nếu đã hoàn thành
                                checkAndUpdateUIIfComplete(categoryNewsMap, processedCategories[0], totalCategories);
                                
                                // Hiển thị thông báo lỗi nếu cần
                                if (processedCategories[0] >= totalCategories) {
                                    Toast.makeText(HomeActivity.this, 
                                            "Không thể tải dữ liệu cho danh mục " + category.getName(), 
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing news for category " + finalCategoryId, e);
                        runOnUiThread(() -> {
                            processedCategories[0]++;
                            categoryNewsMap.put(finalCategoryId, new ArrayList<>());
                            
                            // Kiểm tra và cập nhật UI nếu đã hoàn thành
                            checkAndUpdateUIIfComplete(categoryNewsMap, processedCategories[0], totalCategories);
                        });
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed for category " + finalCategoryId, t);
                runOnUiThread(() -> {
                    processedCategories[0]++;
                    categoryNewsMap.put(finalCategoryId, new ArrayList<>());
                    
                    // Kiểm tra và cập nhật UI nếu đã hoàn thành
                    checkAndUpdateUIIfComplete(categoryNewsMap, processedCategories[0], totalCategories);
                });
            }
        });
    }
    
    private void checkAndUpdateUIIfComplete(Map<Integer, List<News>> categoryNewsMap, 
                                          int processedCount, int totalCount) {
        if (processedCount >= totalCount) {
            // Lấy danh sách danh mục đã được cập nhật
            List<Category> updatedCategories = new ArrayList<>();
            
            // Thêm danh mục đặc biệt cho bài viết nổi bật
            Category featuredCategory = new Category("0", "Bài viết nổi bật", "Các bài viết nổi bật trên hệ thống", "🔥");
            updatedCategories.add(featuredCategory);
            
            // Thêm các danh mục thông thường
            updatedCategories.addAll(categories);
            
            // Cập nhật adapter
            homeCategoriesAdapter.updateData(updatedCategories, categoryNewsMap);
            
            // Ẩn loading indicator
            swipeRefreshLayout.setRefreshing(false);
            
            // Thêm log để gỡ lỗi
            logCategoryNewsStats(updatedCategories, categoryNewsMap);
        }
    }
    
    private void logCategoryNewsStats(List<Category> categories, Map<Integer, List<News>> categoryNewsMap) {
        int totalNews = 0;
        for (Category category : categories) {
            try {
                int categoryId = Integer.parseInt(category.getId());
                List<News> news = categoryNewsMap.get(categoryId);
                int newsCount = news != null ? news.size() : 0;
                totalNews += newsCount;
                
                Log.d(TAG, "Category: " + category.getName() + 
                        " (ID: " + category.getId() + ") - News count: " + newsCount);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid category ID: " + category.getId());
            }
        }
        Log.d(TAG, "Total categories: " + categories.size() + ", Total news: " + totalNews);
    }

    private String getCategoryUrl(int categoryId) {
        switch (categoryId) {
            case 0: 
                return "https://vnexpress.net/tin-tuc-24h"; // Bài viết nổi bật
            case 1:
                return "https://vnexpress.net/thoi-su"; // Thời sự
            case 2:
                return "https://vnexpress.net/the-gioi"; // Thế giới
            case 3:
                return "https://vnexpress.net/kinh-doanh"; // Kinh doanh
            case 4:
                return "https://vnexpress.net/giai-tri"; // Giải trí
            case 5:
                return "https://vnexpress.net/the-thao"; // Thể thao
            case 6:
                return "https://vnexpress.net/phap-luat"; // Pháp luật
            case 7:
                return "https://vnexpress.net/giao-duc"; // Giáo dục
            case 8:
                return "https://vnexpress.net/suc-khoe"; // Sức khỏe
            default:
                return null;
        }
    }

    private List<Category> createCategories() {
        List<Category> categoriesList = new ArrayList<>();
        // Sử dụng constructor hiện có (String id, String name, String description, String iconEmoji)
        categoriesList.add(new Category("1", "Thời sự", "Tin tức thời sự trong nước", "📰"));
        categoriesList.add(new Category("2", "Thế giới", "Tin tức quốc tế", "🌎"));
        categoriesList.add(new Category("3", "Kinh doanh", "Tin tức kinh tế, tài chính", "💼"));
        categoriesList.add(new Category("4", "Giải trí", "Tin tức giải trí, showbiz", "🎭"));
        categoriesList.add(new Category("5", "Thể thao", "Tin tức thể thao", "⚽"));
        categoriesList.add(new Category("6", "Pháp luật", "Tin tức pháp luật", "⚖️"));
        categoriesList.add(new Category("7", "Giáo dục", "Tin tức giáo dục", "🎓"));
        categoriesList.add(new Category("8", "Sức khỏe", "Tin tức y tế, sức khỏe", "🏥"));
        return categoriesList;
    }

    // Inner adapter class cho danh mục và tin tức của mỗi danh mục
    private static class HomeCategoriesAdapter extends RecyclerView.Adapter<HomeCategoriesAdapter.CategoryViewHolder> {
        
        private Context context;
        private List<Category> categories;
        private Map<Integer, List<News>> categoryNewsMap;
        
        public HomeCategoriesAdapter(Context context, List<Category> categories, Map<Integer, List<News>> categoryNewsMap) {
            this.context = context;
            this.categories = categories;
            this.categoryNewsMap = categoryNewsMap;
        }
        
        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_category_with_news, parent, false);
            return new CategoryViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            Category category = categories.get(position);
            int categoryId;
            
            try {
                categoryId = Integer.parseInt(category.getId());
            } catch (NumberFormatException e) {
                Log.e("HomeCategoriesAdapter", "Failed to parse category ID: " + category.getId(), e);
                categoryId = 0;
            }
            
            holder.tvCategoryName.setText(category.getName());
            
            // Lấy danh sách tin tức cho danh mục hiện tại
            List<News> newsList = categoryNewsMap.get(categoryId);
            
            Log.d("HomeCategoriesAdapter", "Position: " + position + 
                    ", Category: " + category.getName() + 
                    ", ID: " + category.getId() + 
                    ", News count: " + (newsList != null ? newsList.size() : 0));
            
            if (newsList == null) {
                newsList = new ArrayList<>();
            }
            
            // Thiết lập adapter cho RecyclerView tin tức của danh mục
            CategoryNewsAdapter newsAdapter = new CategoryNewsAdapter(context, newsList);
            holder.rvCategoryNews.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.rvCategoryNews.setAdapter(newsAdapter);
            
            // Xử lý sự kiện khi nhấn vào "Xem tất cả"
            holder.tvViewAll.setOnClickListener(v -> {
                // First try to open the category in CategoriesActivity
                Intent intent = new Intent(context, com.example.appdocbao.ui.categories.CategoriesActivity.class);
                intent.putExtra("SELECTED_CATEGORY_ID", category.getId());
                intent.putExtra("CATEGORY_NAME", category.getName());
                context.startActivity(intent);
            });
            
            // Ẩn nút "Xem tất cả" nếu là danh mục bài viết nổi bật (ID = 0)
            if ("0".equals(category.getId())) {
                holder.tvViewAll.setVisibility(View.GONE);
            } else {
                holder.tvViewAll.setVisibility(View.VISIBLE);
            }
        }
        
        @Override
        public int getItemCount() {
            return categories != null ? categories.size() : 0;
        }
        
        public void updateData(List<Category> categories, Map<Integer, List<News>> categoryNewsMap) {
            this.categories = categories;
            this.categoryNewsMap = categoryNewsMap;
            
            // Log thông tin debug
            Log.d("HomeCategoriesAdapter", "Updated with " + categories.size() + " categories");
            for (Category category : categories) {
                int catId = Integer.parseInt(category.getId());
                List<News> news = categoryNewsMap.get(catId);
                Log.d("HomeCategoriesAdapter", "Category: " + category.getName() + 
                        ", ID: " + category.getId() + 
                        ", Has news: " + (news != null) + 
                        ", News count: " + (news != null ? news.size() : 0));
            }
            
            notifyDataSetChanged();
        }
        
        static class CategoryViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategoryName, tvViewAll;
            RecyclerView rvCategoryNews;
            
            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
                tvViewAll = itemView.findViewById(R.id.tvViewAll);
                rvCategoryNews = itemView.findViewById(R.id.rvCategoryNews);
            }
        }
    }

    // Inner adapter class cho tin tức trong mỗi danh mục
    private static class CategoryNewsAdapter extends RecyclerView.Adapter<CategoryNewsAdapter.NewsViewHolder> {
        
        private Context context;
        private List<News> newsList;
        
        public CategoryNewsAdapter(Context context, List<News> newsList) {
            this.context = context;
            this.newsList = newsList;
            Log.d("CategoryNewsAdapter", "Constructor - News count: " + (newsList != null ? newsList.size() : 0));
        }
        
        @NonNull
        @Override
        public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
            
            // Điều chỉnh chiều rộng của item để hiển thị đúng trong RecyclerView ngang
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = (int) (parent.getWidth() * 0.8); // 80% chiều rộng của parent
            view.setLayoutParams(params);
            
            return new NewsViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
            News news = newsList.get(position);
            
            Log.d("CategoryNewsAdapter", "Binding news at position " + position + 
                    ": " + news.getTitle() + 
                    ", Image URL: " + news.getImageUrl());
            
            holder.tvTitle.setText(news.getTitle());
            holder.tvDate.setText(news.getPublishDate());
            
            // Sử dụng Glide để load hình ảnh từ URL
            if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                try {
                    Glide.with(context)
                            .load(news.getImageUrl())
                            .placeholder(R.drawable.ic_category_default)
                            .error(R.drawable.ic_category_default)
                            .into(holder.ivThumbnail);
                } catch (Exception e) {
                    Log.e("CategoryNewsAdapter", "Error loading image", e);
                    holder.ivThumbnail.setImageResource(R.drawable.ic_category_default);
                }
            } else {
                holder.ivThumbnail.setImageResource(R.drawable.ic_category_default);
            }
            
            // Đảm bảo thông tin nguồn được hiển thị hoặc ẩn nếu không có
            if (holder.tvSource != null) {
                holder.tvSource.setText("Báo mới");
            }
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, NewsDetailActivity.class);
                intent.putExtra(com.example.appdocbao.utils.Constants.EXTRA_ARTICLE_ID, String.valueOf(news.getId()));
                context.startActivity(intent);
            });
        }
        
        @Override
        public int getItemCount() {
            return newsList != null ? newsList.size() : 0;
        }
        
        static class NewsViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumbnail;
            TextView tvTitle, tvDate, tvSource;
            
            public NewsViewHolder(@NonNull View itemView) {
                super(itemView);
                ivThumbnail = itemView.findViewById(R.id.imgThumbnail);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDate = itemView.findViewById(R.id.tvPublishedTime);
                tvSource = itemView.findViewById(R.id.tvSource);
            }
        }
    }
}