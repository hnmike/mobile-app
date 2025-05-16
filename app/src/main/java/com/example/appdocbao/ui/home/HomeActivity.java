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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    private HomeCategoriesAdapter homeCategoriesAdapter;
    
    private List<News> allNewsList = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Trang chủ");
        }
        
        // Khởi tạo các view
        rvCategories = findViewById(R.id.rvCategories);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        
        // Thiết lập SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadData);
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
        
        // Tải dữ liệu
        loadData();
    }

    private void loadData() {
        // Hiển thị loading indicator
        swipeRefreshLayout.setRefreshing(true);
        
        // Tạo tin tức
        allNewsList = createSampleNews();
        
        // Lọc tin tức nổi bật
        List<News> featuredNews = allNewsList.stream()
                .filter(News::isFeatured)
                .collect(Collectors.toList());
        
        // Tạo danh mục
        categories = createSampleCategories();
        
        // Thêm "danh mục" đặc biệt cho bài viết nổi bật ở đầu danh sách
        Category featuredCategory = new Category("0", "Bài viết nổi bật", "Các bài viết nổi bật trên hệ thống", "🔥");
        List<Category> allCategories = new ArrayList<>();
        allCategories.add(featuredCategory);
        allCategories.addAll(categories);
        
        // Nhóm tin tức theo danh mục
        Map<Integer, List<News>> categoryNewsMap = new HashMap<>();
        // Thêm danh sách bài viết nổi bật vào map với ID là 0
        categoryNewsMap.put(0, featuredNews);
        
        for (Category category : categories) {
            final int categoryId = Integer.parseInt(category.getId());
            List<News> newsForCategory = allNewsList.stream()
                    .filter(news -> news.getCategoryId() == categoryId)
                    .limit(5) // Giới hạn 5 bài viết cho mỗi danh mục
                    .collect(Collectors.toList());
            categoryNewsMap.put(categoryId, newsForCategory);
        }
        
        // In thông tin debug
        for (Map.Entry<Integer, List<News>> entry : categoryNewsMap.entrySet()) {
            Log.d("HomeActivity", "Category ID: " + entry.getKey() + ", News count: " + 
                    (entry.getValue() != null ? entry.getValue().size() : 0));
        }
        
        // Cập nhật adapter cho danh mục (đã bao gồm cả phần bài viết nổi bật)
        homeCategoriesAdapter.updateData(allCategories, categoryNewsMap);
        
        // Ẩn loading indicator
        swipeRefreshLayout.setRefreshing(false);
        
        // Thông báo cho người dùng biết dữ liệu đã được tải
        Toast.makeText(this, "Đã tải dữ liệu mới nhất", Toast.LENGTH_SHORT).show();
    }

    private List<Category> createSampleCategories() {
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

    private List<News> createSampleNews() {
        List<News> newsList = new ArrayList<>();
        
        // Tin tức danh mục Thời sự
        newsList.add(new News(1, "Hơn 1.000 người diễn tập chữa cháy ở chung cư cao tầng", 
                "Các lực lượng phối hợp diễn tập tình huống cháy ở tầng 15...", 
                "https://example.com/image1.jpg", "08:00 - 10/05/2023", 1, true));
        newsList.add(new News(2, "Nhiều đường TP HCM ngập do triều cường",
                "Đường Huỳnh Tấn Phát, Trần Xuân Soạn... ngập sâu...", 
                "https://example.com/image2.jpg", "10:30 - 10/05/2023", 1, false));
        newsList.add(new News(3, "Người mẫu Nga được 'bảo lãnh' trở lại Việt Nam",
                "Ekaterina Kuznetsova về nước sau khi được nhà ngoại giao Việt Nam bảo lãnh...", 
                "https://example.com/image3.jpg", "14:15 - 10/05/2023", 1, false));
        
        // Tin tức danh mục Thế giới
        newsList.add(new News(4, "Nhật Bản cân nhắc chi 42 tỷ USD hỗ trợ gia đình sinh con",
                "Chính phủ Nhật Bản cân nhắc chi 6.000 tỷ yen mỗi năm để trợ cấp cho các gia đình sinh con...", 
                "https://example.com/image4.jpg", "07:45 - 10/05/2023", 2, true));
        newsList.add(new News(5, "Đối phương của Trump trong cuộc tranh luận ngày 10/9",
                "Phó tổng thống Mỹ Kamala Harris có bề dày kinh nghiệm tranh luận chính trị...", 
                "https://example.com/image5.jpg", "09:20 - 10/05/2023", 2, false));
        
        // Tin tức danh mục Kinh doanh
        newsList.add(new News(6, "Giá vàng miếng giảm, nhẫn tăng",
                "Giá vàng miếng SJC giảm 300.000 đồng mỗi lượng...", 
                "https://example.com/image6.jpg", "15:40 - 10/05/2023", 3, true));
        newsList.add(new News(7, "Thời đại của 'đồng đô xanh' đang kết thúc?",
                "Vụ ám sát Tổng thống Iran Raisi đang thúc đẩy Iran...", 
                "https://example.com/image7.jpg", "11:10 - 10/05/2023", 3, false));
        
        // Tin tức danh mục Giải trí
        newsList.add(new News(8, "Cô dâu duy nhất của Jack Nicholson",
                "Sandra Knight là người phụ nữ duy nhất Jack Nicholson cưới...", 
                "https://example.com/image8.jpg", "16:30 - 10/05/2023", 4, true));
        newsList.add(new News(9, "Jolie không muốn Pitt gặp các con",
                "Angelina Jolie không muốn Brad Pitt có quan hệ với các con...", 
                "https://example.com/image9.jpg", "13:45 - 10/05/2023", 4, false));
        
        // Tin tức danh mục Thể thao
        newsList.add(new News(10, "Lukaku ghi bàn ngày ra mắt Napoli",
                "Romelu Lukaku mất chưa đầy 10 phút để ghi bàn ra mắt...", 
                "https://example.com/image10.jpg", "08:15 - 10/05/2023", 5, true));
        newsList.add(new News(11, "Djokovic: 'Tôi không thích tiệm cận sự hoàn hảo'",
                "Novak Djokovic không còn khát khao trọn vẹn mọi khía cạnh...", 
                "https://example.com/image11.jpg", "12:20 - 10/05/2023", 5, false));
        
        return newsList;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                Intent intent = new Intent(context, NewsListActivity.class);
                intent.putExtra("CATEGORY_ID", category.getId());
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
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .into(holder.ivThumbnail);
                } catch (Exception e) {
                    Log.e("CategoryNewsAdapter", "Error loading image", e);
                    holder.ivThumbnail.setImageResource(R.drawable.placeholder_image);
                }
            } else {
                holder.ivThumbnail.setImageResource(R.drawable.placeholder_image);
            }
            
            // Đảm bảo thông tin nguồn được hiển thị hoặc ẩn nếu không có
            if (holder.tvSource != null) {
                holder.tvSource.setText("Báo mới");
            }
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, NewsDetailActivity.class);
                intent.putExtra("NEWS_ID", news.getId());
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