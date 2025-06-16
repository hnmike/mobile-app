package com.example.appdocbao.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
// Bỏ import MenuItem nếu không sử dụng trực tiếp trong Activity này
// import android.view.MenuItem;
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
import com.example.appdocbao.R;
import com.example.appdocbao.api.RetrofitClient;
import com.example.appdocbao.api.VnExpressParser;
import com.example.appdocbao.api.VnExpressService;
import com.example.appdocbao.data.News;
import com.example.appdocbao.data.model.Category;
import com.example.appdocbao.ui.bookmarks.BookmarksActivity;
import com.example.appdocbao.ui.categories.CategoriesActivity;
// Bỏ NewsAdapter và NewsListActivity nếu không sử dụng trực tiếp trong Activity này
// import com.example.appdocbao.ui.newslist.NewsAdapter;
// import com.example.appdocbao.ui.newslist.NewsListActivity;
import com.example.appdocbao.ui.newsdetail.NewsDetailActivity;
import com.example.appdocbao.ui.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth; // Import trực tiếp FirebaseAuth

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// Bỏ import Collectors nếu không sử dụng trực tiếp
// import java.util.stream.Collectors;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình chính (Home) của ứng dụng, hiển thị danh sách các danh mục tin tức
 * và các bài viết nổi bật trong mỗi danh mục.
 * <p>
 * Người dùng có thể làm mới dữ liệu bằng cách vuốt xuống (swipe to refresh) và
 * điều hướng đến các màn hình khác thông qua thanh điều hướng dưới cùng (Bottom Navigation).
 * </p>
 *
 * Tác giả: [Tên của bạn]
 * Ngày tạo: [Ngày bạn tạo file này, ví dụ: 05/08/2024]
 * Người sửa đổi:
 */
public class HomeActivity extends AppCompatActivity {

    // Tag dùng cho việc logging, giúp dễ dàng lọc log trong Logcat
    private static final String TAG = "HomeActivity";

    // Khai báo các thành phần UI
    private RecyclerView rvCategories; // RecyclerView để hiển thị danh sách các danh mục và tin tức
    private SwipeRefreshLayout swipeRefreshLayout; // Layout cho phép người dùng vuốt xuống để làm mới
    private BottomNavigationView bottomNavigationView; // Thanh điều hướng ở cuối màn hình

    // Adapter cho RecyclerView chính (hiển thị danh mục và tin tức của danh mục đó)
    private HomeCategoriesAdapter homeCategoriesAdapter;

    // Danh sách chứa tất cả tin tức (hiện tại chưa được sử dụng trực tiếp, có thể dùng cho logic khác sau này)
    private List<News> allNewsList = new ArrayList<>();
    // Danh sách các danh mục chính (không bao gồm "Bài viết nổi bật" ban đầu)
    private List<Category> categories = new ArrayList<>();
    // Executor để thực hiện các tác vụ nặng (như parsing HTML) trên một luồng nền riêng biệt
    private final Executor executor = Executors.newSingleThreadExecutor();
    // Service của Retrofit để gọi API
    private VnExpressService vnExpressService;
    // Parser để phân tích HTML từ VnExpress và trích xuất tin tức
    private VnExpressParser vnExpressParser;

    /**
     * Được gọi khi Activity được tạo lần đầu.
     * <p>
     * Tại đây, chúng ta khởi tạo giao diện người dùng, thiết lập các listener
     * và bắt đầu quá trình tải dữ liệu ban đầu.
     * </p>
     *
     * @param savedInstanceState Nếu Activity đang được khởi tạo lại sau khi bị hủy trước đó,
     *                           Bundle này chứa dữ liệu mà nó đã cung cấp gần đây nhất trong
     *                           onSaveInstanceState(Bundle). Ngược lại, nó là null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Thiết lập layout cho Activity

        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Đặt Toolbar làm ActionBar cho Activity này
        if (getSupportActionBar() != null) {
            // Không hiển thị nút back (mũi tên quay lại) trên trang chủ
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("Trang chủ"); // Đặt tiêu đề cho Toolbar
        }

        // Ánh xạ các view từ layout XML
        rvCategories = findViewById(R.id.rvCategories);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Khởi tạo các service và parser cần thiết cho việc lấy dữ liệu
        vnExpressService = RetrofitClient.getClient().create(VnExpressService.class);
        vnExpressParser = new VnExpressParser();

        // Thiết lập SwipeRefreshLayout
        // Đăng ký listener để khi người dùng vuốt xuống, phương thức loadDataFromApi() sẽ được gọi
        swipeRefreshLayout.setOnRefreshListener(this::loadDataFromApi);
        // Thiết lập màu sắc cho animation của SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        // Thiết lập RecyclerView cho danh mục và bài viết nổi bật của mỗi danh mục
        rvCategories.setLayoutManager(new LinearLayoutManager(this)); // Sử dụng LinearLayoutManager mặc định (dọc)
        // Khởi tạo adapter với danh sách rỗng ban đầu
        homeCategoriesAdapter = new HomeCategoriesAdapter(this, new ArrayList<>(), new HashMap<>());
        rvCategories.setAdapter(homeCategoriesAdapter); // Gắn adapter vào RecyclerView

        // Thiết lập Bottom Navigation
        setupBottomNavigation();

        // Tải dữ liệu tin tức từ API khi Activity được tạo
        loadDataFromApi();
    }

    /**
     * Thiết lập các hành vi cho BottomNavigationView.
     * Bao gồm việc xử lý sự kiện khi người dùng chọn một mục trên thanh điều hướng
     * để chuyển đến các màn hình tương ứng.
     */
    private void setupBottomNavigation() {
        // Kiểm tra xem bottomNavigationView có null không (ví dụ: nếu layout không có view này)
        if (bottomNavigationView == null) {
            Log.w(TAG, "Bottom navigation view is null, possibly not in the layout");
            return;
        }

        // Đặt mục "Home" là mục được chọn mặc định khi vào màn hình
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        // Thiết lập listener cho sự kiện chọn mục
        bottomNavigationView.setOnItemSelectedListener(item -> {
            try {
                int itemId = item.getItemId(); // Lấy ID của mục được chọn
                if (itemId == R.id.nav_home) {
                    // Người dùng đang ở màn hình Home, không làm gì cả
                    return true; // Trả về true để đánh dấu sự kiện đã được xử lý
                } else if (itemId == R.id.nav_categories) {
                    // Chuyển đến màn hình CategoriesActivity
                    Intent intent = new Intent(HomeActivity.this, CategoriesActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_bookmarks) {
                    // Chuyển đến màn hình BookmarksActivity
                    Intent intent = new Intent(HomeActivity.this, BookmarksActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    // Kiểm tra trạng thái đăng nhập và chuyển đến trang phù hợp
                    if (isUserLoggedIn()) {
                        // Nếu đã đăng nhập, mở ProfileActivity
                        Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
                        startActivity(profileIntent);
                    } else {
                        // Nếu chưa đăng nhập, mở SignInActivity
                        // Sử dụng tên lớp đầy đủ để tránh lỗi nếu có sự nhầm lẫn về package
                        Intent loginIntent = new Intent();
                        loginIntent.setClassName(getPackageName(), "com.example.appdocbao.ui.auth.SignInActivity");
                        startActivity(loginIntent);
                    }
                    return true;
                }
                return false; // Trả về false nếu không có mục nào được xử lý
            } catch (Exception e) {
                // Ghi log lỗi nếu có vấn đề trong quá trình điều hướng
                Log.e(TAG, "Error in navigation: " + e.getMessage(), e);
                return false;
            }
        });
    }

    /**
     * Kiểm tra xem người dùng hiện tại đã đăng nhập vào ứng dụng hay chưa
     * bằng cách sử dụng Firebase Authentication.
     *
     * @return true nếu người dùng đã đăng nhập, false nếu chưa.
     */
    private boolean isUserLoggedIn() {
        try {
            // Lấy instance của FirebaseAuth và kiểm tra xem có người dùng hiện tại không
            return FirebaseAuth.getInstance().getCurrentUser() != null;
        } catch (Exception e) {
            // Ghi log lỗi nếu có vấn đề khi kiểm tra trạng thái đăng nhập
            Log.e(TAG, "Error checking login status: " + e.getMessage(), e);
            return false; // Mặc định trả về false nếu có lỗi
        }
    }

    /**
     * Tải dữ liệu tin tức từ API.
     * <p>
     * Phương thức này khởi tạo danh sách các danh mục, bao gồm cả một "danh mục đặc biệt"
     * cho các bài viết nổi bật, sau đó gọi API để lấy tin tức cho từng danh mục.
     * </p>
     */
    private void loadDataFromApi() {
        // Hiển thị thanh tiến trình (loading indicator) của SwipeRefreshLayout
        swipeRefreshLayout.setRefreshing(true);

        // Tạo danh sách các danh mục tin tức chính
        categories = createCategories();

        // Tạo một "danh mục" đặc biệt cho các bài viết nổi bật, sẽ được hiển thị ở đầu danh sách
        Category featuredCategory = new Category("0", "Bài viết nổi bật", "Các bài viết nổi bật trên hệ thống", "🔥");
        List<Category> allCategoriesWithFeatured = new ArrayList<>(); // Danh sách tổng hợp tất cả các danh mục
        allCategoriesWithFeatured.add(featuredCategory); // Thêm danh mục nổi bật vào đầu
        allCategoriesWithFeatured.addAll(categories); // Thêm các danh mục còn lại

        // Map để lưu trữ danh sách tin tức cho mỗi ID danh mục
        Map<Integer, List<News>> categoryNewsMap = new HashMap<>();

        // Biến đếm số lượng danh mục đã được xử lý (tải xong tin tức)
        // Sử dụng mảng một phần tử để có thể thay đổi giá trị bên trong lambda expressions
        final int[] processedCategoriesCount = {0};
        final int totalCategoriesToLoad = allCategoriesWithFeatured.size(); // Tổng số danh mục cần tải

        // Bắt đầu quá trình tải tin tức từ API cho tất cả các danh mục
        loadCategoriesNewsFromApi(allCategoriesWithFeatured, categoryNewsMap, processedCategoriesCount, totalCategoriesToLoad);
    }

    /**
     * Tải tin tức từ API cho một danh sách các danh mục.
     * <p>
     * Phương thức này lặp qua từng danh mục, lấy URL tương ứng và gọi
     * {@link #loadNewsForCategory(String, int, Category, Map, int[], int)} để tải tin tức.
     * </p>
     *
     * @param allCategories Danh sách tất cả các danh mục (bao gồm cả "Bài viết nổi bật").
     * @param categoryNewsMap Map để lưu trữ tin tức theo ID danh mục.
     * @param processedCategoriesCount Mảng chứa số lượng danh mục đã xử lý.
     * @param totalCategories Tổng số danh mục cần tải.
     */
    private void loadCategoriesNewsFromApi(List<Category> allCategories,
                                           Map<Integer, List<News>> categoryNewsMap,
                                           final int[] processedCategoriesCount,
                                           final int totalCategories) {
        // Lặp qua từng danh mục trong danh sách
        for (Category category : allCategories) {
            final int categoryId;
            try {
                // Chuyển đổi ID danh mục từ String sang int
                categoryId = Integer.parseInt(category.getId());
            } catch (NumberFormatException e) {
                // Ghi log lỗi nếu ID danh mục không hợp lệ và bỏ qua danh mục này
                Log.e(TAG, "Failed to parse category ID: " + category.getId(), e);
                continue; // Chuyển sang danh mục tiếp theo
            }

            // Lấy URL của RSS feed hoặc trang HTML cho danh mục này
            final String url = getCategoryUrl(categoryId);

            if (url != null) {
                // Nếu có URL, tiến hành tải tin tức cho danh mục này
                loadNewsForCategory(url, categoryId, category, categoryNewsMap, processedCategoriesCount, totalCategories);
            } else {
                // Nếu không có URL (ví dụ: danh mục không được hỗ trợ),
                // tăng biến đếm và thêm danh sách rỗng vào map
                processedCategoriesCount[0]++;
                categoryNewsMap.put(categoryId, new ArrayList<>());

                // Kiểm tra xem đã tải xong tất cả danh mục chưa để cập nhật UI
                checkAndUpdateUIIfComplete(categoryNewsMap, processedCategoriesCount[0], totalCategories);
                Log.w(TAG, "No URL found for category: " + category.getName() + " (ID: " + categoryId + ")");
            }
        }
    }

    /**
     * Tải tin tức cho một danh mục cụ thể từ URL được cung cấp.
     * <p>
     * Sử dụng Retrofit để gọi API, sau đó dùng Executor để thực hiện việc phân tích HTML
     * trên một luồng nền. Kết quả sẽ được cập nhật vào {@code categoryNewsMap} và UI
     * sẽ được làm mới nếu tất cả danh mục đã được xử lý.
     * </p>
     *
     * @param url URL để tải nội dung HTML/RSS.
     * @param categoryId ID của danh mục.
     * @param category Đối tượng Category.
     * @param categoryNewsMap Map để lưu trữ tin tức.
     * @param processedCategoriesCount Mảng chứa số lượng danh mục đã xử lý.
     * @param totalCategories Tổng số danh mục cần tải.
     */
    private void loadNewsForCategory(String url,
                                     int categoryId, // ID gốc, có thể dùng để debug
                                     Category category, // Đối tượng Category, dùng để lấy tên khi báo lỗi
                                     Map<Integer, List<News>> categoryNewsMap,
                                     final int[] processedCategoriesCount,
                                     final int totalCategories) {
        // Tạo bản sao final của categoryId để sử dụng trong lambda (callback của Retrofit)
        // Điều này đảm bảo giá trị categoryId không bị thay đổi bởi các vòng lặp khác
        final int finalCategoryId = Integer.parseInt(category.getId()); // Lấy lại ID từ category object để đảm bảo chính xác

        // Gọi API bằng Retrofit để lấy nội dung HTML từ URL
        vnExpressService.getHtmlContent(url).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                // Thực thi việc phân tích HTML trên một luồng nền để không chặn UI thread
                executor.execute(() -> {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            // Nếu gọi API thành công và có nội dung trả về
                            // Phân tích nội dung HTML để lấy danh sách bài viết
                            List<News> newsForCategory = vnExpressParser.parseNews(response.body(), finalCategoryId);

                            // Cập nhật danh sách tin tức cho danh mục trên UI thread
                            runOnUiThread(() -> {
                                categoryNewsMap.put(finalCategoryId, newsForCategory);
                                processedCategoriesCount[0]++; // Tăng biến đếm

                                // Kiểm tra và cập nhật UI nếu đã hoàn thành tất cả các danh mục
                                checkAndUpdateUIIfComplete(categoryNewsMap, processedCategoriesCount[0], totalCategories);
                            });
                        } else {
                            // Nếu gọi API không thành công hoặc không có nội dung
                            Log.e(TAG, "API call not successful or empty body for category " +
                                    category.getName() + " (ID: " + finalCategoryId + "). Code: " + response.code());
                            runOnUiThread(() -> {
                                processedCategoriesCount[0]++;
                                categoryNewsMap.put(finalCategoryId, new ArrayList<>()); // Đặt danh sách rỗng

                                checkAndUpdateUIIfComplete(categoryNewsMap, processedCategoriesCount[0], totalCategories);

                                // Hiển thị thông báo lỗi nếu cần (ví dụ: khi tất cả đã xong nhưng có lỗi)
                                if (processedCategoriesCount[0] >= totalCategories) {
                                    // Kiểm tra xem có phải lỗi này không hay lỗi khác
                                    // Chỉ hiển thị Toast nếu lỗi này là lỗi cuối cùng hoặc lỗi cụ thể
                                    // Tránh spam Toast
                                }
                            });
                        }
                    } catch (Exception e) {
                        // Ghi log lỗi nếu có vấn đề trong quá trình phân tích HTML
                        Log.e(TAG, "Error parsing news for category " + category.getName() + " (ID: " + finalCategoryId + ")", e);
                        runOnUiThread(() -> {
                            processedCategoriesCount[0]++;
                            categoryNewsMap.put(finalCategoryId, new ArrayList<>()); // Đặt danh sách rỗng
                            checkAndUpdateUIIfComplete(categoryNewsMap, processedCategoriesCount[0], totalCategories);
                        });
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                // Được gọi khi có lỗi mạng hoặc lỗi khác trong quá trình gọi API
                Log.e(TAG, "API call failed for category " + category.getName() + " (ID: " + finalCategoryId + ")", t);
                runOnUiThread(() -> {
                    processedCategoriesCount[0]++;
                    categoryNewsMap.put(finalCategoryId, new ArrayList<>()); // Đặt danh sách rỗng
                    checkAndUpdateUIIfComplete(categoryNewsMap, processedCategoriesCount[0], totalCategories);

                    // Có thể hiển thị một Toast thông báo lỗi chung ở đây nếu tất cả đã hoàn thành
                    // và có ít nhất một lỗi xảy ra.
                    if (processedCategoriesCount[0] >= totalCategories) {
                        Toast.makeText(HomeActivity.this,
                                "Lỗi tải dữ liệu cho một số danh mục.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * Kiểm tra xem tất cả các danh mục đã được xử lý (tải xong tin tức) hay chưa.
     * Nếu đã xong, cập nhật dữ liệu cho adapter và ẩn thanh tiến trình làm mới.
     *
     * @param categoryNewsMap Map chứa tin tức của các danh mục.
     * @param processedCount Số lượng danh mục đã được xử lý.
     * @param totalCount Tổng số danh mục cần xử lý.
     */
    private void checkAndUpdateUIIfComplete(Map<Integer, List<News>> categoryNewsMap,
                                            int processedCount, int totalCount) {
        // Chỉ cập nhật UI khi tất cả các danh mục đã được xử lý
        if (processedCount >= totalCount) {
            // Tạo lại danh sách danh mục để đảm bảo thứ tự đúng khi cập nhật adapter
            // (bao gồm cả danh mục "Bài viết nổi bật")
            List<Category> finalCategoriesForAdapter = new ArrayList<>();

            // Thêm danh mục đặc biệt cho bài viết nổi bật
            Category featuredCategory = new Category("0", "Bài viết nổi bật", "Các bài viết nổi bật trên hệ thống", "🔥");
            finalCategoriesForAdapter.add(featuredCategory);

            // Thêm các danh mục thông thường đã được định nghĩa
            finalCategoriesForAdapter.addAll(this.categories); // this.categories là danh sách gốc không có featured

            // Cập nhật dữ liệu mới cho HomeCategoriesAdapter
            // Adapter sẽ tự động cập nhật RecyclerView
            if (homeCategoriesAdapter != null) {
                homeCategoriesAdapter.updateData(finalCategoriesForAdapter, categoryNewsMap);
            }

            // Ẩn thanh tiến trình (loading indicator) của SwipeRefreshLayout
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }

            // Ghi log thống kê số lượng tin tức cho từng danh mục để gỡ lỗi
            logCategoryNewsStats(finalCategoriesForAdapter, categoryNewsMap);
        }
    }

    /**
     * Ghi log thống kê số lượng tin tức cho mỗi danh mục và tổng số tin tức.
     * Hữu ích cho việc gỡ lỗi và kiểm tra dữ liệu.
     *
     * @param categoriesToList Log Danh sách các danh mục.
     * @param categoryNewsMap Map chứa tin tức của các danh mục.
     */
    private void logCategoryNewsStats(List<Category> categoriesToListLog, Map<Integer, List<News>> categoryNewsMap) {
        int totalNewsCount = 0;
        Log.d(TAG, "--- Category News Statistics ---");
        for (Category category : categoriesToListLog) {
            try {
                int categoryId = Integer.parseInt(category.getId());
                List<News> news = categoryNewsMap.get(categoryId); // Lấy danh sách tin tức từ map
                int newsCountForCategory = (news != null) ? news.size() : 0;
                totalNewsCount += newsCountForCategory;

                Log.d(TAG, "Category: " + category.getName() +
                        " (ID: " + category.getId() + ") - News count: " + newsCountForCategory);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid category ID during logging: " + category.getId());
            } catch (NullPointerException e) {
                Log.e(TAG, "NullPointerException for category: " + category.getName() + " (ID: " + category.getId() + ") during logging. categoryNewsMap might be missing this ID or category object is null.", e);
            }
        }
        Log.d(TAG, "Total categories for display: " + categoriesToListLog.size() + ", Total news items fetched: " + totalNewsCount);
        Log.d(TAG, "--- End of Statistics ---");
    }


    /**
     * Trả về URL của RSS feed hoặc trang HTML cho một ID danh mục cụ thể.
     *
     * @param categoryId ID của danh mục.
     * @return Chuỗi URL, hoặc null nếu không có URL nào được định nghĩa cho ID đó.
     */
    private String getCategoryUrl(int categoryId) {
        // Sử dụng switch-case để trả về URL tương ứng với ID danh mục
        switch (categoryId) {
            case 0: // ID 0 được dùng cho "Bài viết nổi bật"
                return "https://vnexpress.net/tin-tuc-24h";
            case 1:
                return "https://vnexpress.net/thoi-su";
            case 2:
                return "https://vnexpress.net/the-gioi";
            case 3:
                return "https://vnexpress.net/kinh-doanh";
            case 4:
                return "https://vnexpress.net/giai-tri";
//            Hiện tại các case này đang bị comment, nếu cần dùng thì bỏ comment
            case 5:
                return "https://vnexpress.net/the-thao";
            case 6:
                return "https://vnexpress.net/phap-luat";
            case 7:
                return "https://vnexpress.net/giao-duc";
            case 8:
                return "https://vnexpress.net/suc-khoe";
            default:
                // Trả về null nếu không có URL nào khớp với ID danh mục
                // Điều này sẽ được xử lý trong phương thức gọi nó
                Log.w(TAG, "No URL defined for category ID: " + categoryId);
                return null;
        }
    }

    /**
     * Tạo và trả về danh sách các đối tượng Category (danh mục tin tức).
     * <p>
     * Danh sách này không bao gồm "Bài viết nổi bật" vì nó được thêm riêng.
     * </p>
     * @return Danh sách các {@link Category}.
     */
    private List<Category> createCategories() {
        List<Category> categoriesList = new ArrayList<>();
        // Thêm các danh mục vào danh sách
        // Sử dụng constructor: Category(String id, String name, String description, String iconEmoji)
        categoriesList.add(new Category("1", "Thời sự", "Tin tức thời sự trong nước", "📰"));
        categoriesList.add(new Category("2", "Thế giới", "Tin tức quốc tế", "🌎"));
        categoriesList.add(new Category("3", "Kinh doanh", "Tin tức kinh tế, tài chính", "💼"));
        categoriesList.add(new Category("4", "Giải trí", "Tin tức giải trí, showbiz", "🎭"));
//        Hiện tại các danh mục này đang bị comment, nếu cần dùng thì bỏ comment
        categoriesList.add(new Category("5", "Thể thao", "Tin tức thể thao", "⚽"));
        categoriesList.add(new Category("6", "Pháp luật", "Tin tức pháp luật", "⚖️"));
        categoriesList.add(new Category("7", "Giáo dục", "Tin tức giáo dục", "🎓"));
        categoriesList.add(new Category("8", "Sức khỏe", "Tin tức y tế, sức khỏe", "🏥"));
        return categoriesList;
    }

    // --- Inner Adapter Classes ---

    /**
     * Adapter cho RecyclerView chính trong {@link HomeActivity}.
     * Mỗi item trong RecyclerView này đại diện cho một danh mục, bao gồm tên danh mục,
     * một RecyclerView ngang hiển thị các tin tức của danh mục đó, và nút "Xem tất cả".
     */
    private static class HomeCategoriesAdapter extends RecyclerView.Adapter<HomeCategoriesAdapter.CategoryViewHolder> {

        private final Context context; // Context để sử dụng cho LayoutInflater và Intent
        private List<Category> categories; // Danh sách các đối tượng Category để hiển thị
        private Map<Integer, List<News>> categoryNewsMap; // Map chứa danh sách tin tức cho mỗi ID danh mục

        /**
         * Constructor cho HomeCategoriesAdapter.
         *
         * @param context       Context của ứng dụng.
         * @param categories    Danh sách các danh mục.
         * @param categoryNewsMap Map chứa tin tức theo ID danh mục.
         */
        public HomeCategoriesAdapter(Context context, List<Category> categories, Map<Integer, List<News>> categoryNewsMap) {
            this.context = context;
            this.categories = categories;
            this.categoryNewsMap = categoryNewsMap;
        }

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Tạo view cho mỗi item từ layout XML item_category_with_news.xml
            View view = LayoutInflater.from(context).inflate(R.layout.item_category_with_news, parent, false);
            return new CategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            Category category = categories.get(position); // Lấy đối tượng Category tại vị trí hiện tại
            int categoryId;

            try {
                // Chuyển đổi ID danh mục từ String sang int
                categoryId = Integer.parseInt(category.getId());
            } catch (NumberFormatException e) {
                Log.e("HomeCategoriesAdapter", "Failed to parse category ID for binding: " + category.getId(), e);
                // Nếu ID không hợp lệ, có thể gán một giá trị mặc định hoặc bỏ qua việc hiển thị tin tức
                // Ở đây, tạm thời không hiển thị tin tức nếu ID lỗi, hoặc có thể gán categoryId = 0
                // để hiển thị tin tức nổi bật (cần xem xét logic này)
                // For now, if ID is bad, we might not get news.
                holder.tvCategoryName.setText(category.getName() + " (Lỗi ID)");
                holder.rvCategoryNews.setAdapter(new CategoryNewsAdapter(context, new ArrayList<>())); // Hiển thị rỗng
                holder.tvViewAll.setVisibility(View.GONE);
                return; // Không xử lý tiếp cho item này nếu ID lỗi
            }

            holder.tvCategoryName.setText(category.getName()); // Đặt tên danh mục

            // Lấy danh sách tin tức cho danh mục hiện tại từ map
            List<News> newsListForThisCategory = categoryNewsMap.get(categoryId);

            // Ghi log để kiểm tra dữ liệu
            Log.d("HomeCategoriesAdapter", "Binding category: " + category.getName() +
                    " (ID: " + categoryId + "), Position: " + position +
                    ", News count in map: " + (newsListForThisCategory != null ? newsListForThisCategory.size() : "null or 0"));

            if (newsListForThisCategory == null) {
                newsListForThisCategory = new ArrayList<>(); // Khởi tạo danh sách rỗng nếu không có tin tức
            }

            // Thiết lập adapter cho RecyclerView con (hiển thị tin tức theo chiều ngang)
            CategoryNewsAdapter newsAdapter = new CategoryNewsAdapter(context, newsListForThisCategory);
            holder.rvCategoryNews.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)); // Layout ngang
            holder.rvCategoryNews.setAdapter(newsAdapter);

            // Xử lý sự kiện khi nhấn vào nút "Xem tất cả"
            holder.tvViewAll.setOnClickListener(v -> {
                // Tạo Intent để chuyển đến CategoriesActivity (hoặc NewsListActivity tùy logic)
                // và truyền ID hoặc tên danh mục để màn hình đó biết cần hiển thị gì.
                Intent intent = new Intent(context, CategoriesActivity.class);
                intent.putExtra("SELECTED_CATEGORY_ID", category.getId()); // Truyền ID dưới dạng String
                intent.putExtra("CATEGORY_NAME", category.getName());
                context.startActivity(intent);
            });

            // Ẩn nút "Xem tất cả" nếu là danh mục "Bài viết nổi bật" (có ID là "0")
            if ("0".equals(category.getId())) {
                holder.tvViewAll.setVisibility(View.GONE);
            } else {
                holder.tvViewAll.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            // Trả về số lượng danh mục cần hiển thị
            return categories != null ? categories.size() : 0;
        }

        /**
         * Cập nhật dữ liệu mới cho adapter và thông báo cho RecyclerView để vẽ lại.
         *
         * @param newCategories Danh sách danh mục mới.
         * @param newCategoryNewsMap Map tin tức mới theo ID danh mục.
         */
        public void updateData(List<Category> newCategories, Map<Integer, List<News>> newCategoryNewsMap) {
            this.categories = newCategories;
            this.categoryNewsMap = newCategoryNewsMap;

            // Ghi log thông tin debug khi cập nhật dữ liệu
            Log.d("HomeCategoriesAdapter", "Data updated. Category count: " +
                    (this.categories != null ? this.categories.size() : 0));
            if (this.categories != null && this.categoryNewsMap != null) {
                for (Category category : this.categories) {
                    try {
                        int catId = Integer.parseInt(category.getId());
                        List<News> news = this.categoryNewsMap.get(catId);
                        Log.d("HomeCategoriesAdapter", "Updating UI - Category: " + category.getName() +
                                " (ID: " + category.getId() +
                                "), Has news in map: " + (this.categoryNewsMap.containsKey(catId)) +
                                ", News list is null: " + (news == null) +
                                ", News count: " + (news != null ? news.size() : "N/A"));
                    } catch (NumberFormatException e){
                        Log.e("HomeCategoriesAdapter", "Error parsing category ID in updateData: " + category.getId());
                    } catch (NullPointerException e) {
                        Log.e("HomeCategoriesAdapter", "Null category object in list during updateData logging.");
                    }
                }
            }
            notifyDataSetChanged(); // Thông báo cho RecyclerView rằng dữ liệu đã thay đổi
        }

        /**
         * ViewHolder cho mỗi item danh mục.
         * Chứa các view như tên danh mục, nút "Xem tất cả", và RecyclerView cho tin tức.
         */
        static class CategoryViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategoryName, tvViewAll;
            RecyclerView rvCategoryNews; // RecyclerView con để hiển thị tin tức theo chiều ngang

            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                // Ánh xạ các view từ layout của item
                tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
                tvViewAll = itemView.findViewById(R.id.tvViewAll);
                rvCategoryNews = itemView.findViewById(R.id.rvCategoryNews);
            }
        }
    }
    /**
     * Adapter cho RecyclerView con bên trong mỗi item của {@link HomeCategoriesAdapter}.
     * RecyclerView này hiển thị danh sách các bài viết (News) theo chiều ngang cho một danh mục cụ thể.
     */
    private static class CategoryNewsAdapter extends RecyclerView.Adapter<CategoryNewsAdapter.NewsViewHolder> {

        private final Context context; // Context để sử dụng cho LayoutInflater và Intent
        private final List<News> newsList; // Danh sách các bài viết để hiển thị

        /**
         * Constructor cho CategoryNewsAdapter.
         *
         * @param context  Context của ứng dụng.
         * @param newsList Danh sách các bài viết.
         */
        public CategoryNewsAdapter(Context context, List<News> newsList) {
            this.context = context;
            this.newsList = (newsList != null) ? newsList : new ArrayList<>(); // Đảm bảo newsList không bao giờ null
            Log.d("CategoryNewsAdapter", "Constructor - Initial news count: " + this.newsList.size());
        }

        @NonNull
        @Override
        public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Tạo view cho mỗi item tin tức từ layout XML item_news.xml
            View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);

            // Điều chỉnh chiều rộng của mỗi item tin tức để hiển thị tốt hơn trong RecyclerView ngang.
            // Mỗi item sẽ chiếm khoảng 80% chiều rộng của RecyclerView cha (rvCategoryNews).
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (parent.getWidth() > 0) { // Đảm bảo parent đã có kích thước
                params.width = (int) (parent.getWidth() * 0.8);
            } else {
                // Nếu parent chưa có kích thước, có thể đặt một giá trị cố định hoặc dựa vào resources
                // Ví dụ: params.width = context.getResources().getDimensionPixelSize(R.dimen.category_news_item_width);
                // Hoặc để nó tự điều chỉnh ban đầu và có thể tính toán lại sau nếu cần.
                // Ở đây, tạm thời không thay đổi nếu parent.getWidth() <= 0
            }
            view.setLayoutParams(params);

            return new NewsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
            if (newsList.isEmpty()) {
                // Trường hợp danh sách rỗng, không làm gì cả hoặc hiển thị thông báo "không có tin tức"
                Log.d("CategoryNewsAdapter", "Binding an empty news list.");
                // Có thể ẩn view của holder hoặc hiển thị một placeholder
                return;
            }

            News news = newsList.get(position); // Lấy đối tượng News tại vị trí hiện tại

            Log.d("CategoryNewsAdapter", "Binding news at position " + position +
                    ": \"" + news.getTitle() + "\"" +
                    ", Image URL: " + news.getImageUrl());

            holder.tvTitle.setText(news.getTitle()); // Đặt tiêu đề bài viết
            holder.tvDate.setText(news.getPublishDate()); // Đặt ngày đăng

            // Sử dụng thư viện Glide để tải và hiển thị hình ảnh từ URL
            if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                try {
                    Glide.with(context)
                            .load(news.getImageUrl())
                            .placeholder(R.drawable.placeholder_image) // Hình ảnh hiển thị trong khi tải
                            .error(R.drawable.placeholder_image)       // Hình ảnh hiển thị nếu có lỗi tải
                            .into(holder.ivThumbnail);
                } catch (Exception e) {
                    // Ghi log lỗi nếu Glide không thể tải hình ảnh
                    Log.e("CategoryNewsAdapter", "Error loading image with Glide: " + news.getImageUrl(), e);
                    holder.ivThumbnail.setImageResource(R.drawable.placeholder_image); // Đặt ảnh placeholder mặc định
                }
            } else {
                // Nếu không có URL hình ảnh, đặt ảnh placeholder mặc định
                Log.w("CategoryNewsAdapter", "Image URL is null or empty for news: " + news.getTitle());
                holder.ivThumbnail.setImageResource(R.drawable.placeholder_image);
            }

            // Hiển thị nguồn tin (nếu có TextView tvSource trong layout item_news.xml)
            // Trong layout hiện tại, tvSource có thể không phải lúc nào cũng có sẵn
            if (holder.tvSource != null) {
                // Hiện tại đang gán cứng "Báo mới", có thể thay đổi để lấy từ đối tượng News nếu có
                holder.tvSource.setText("VnExpress"); // Hoặc news.getSourceName() nếu có
            }

            // Xử lý sự kiện khi người dùng nhấn vào một item tin tức
            holder.itemView.setOnClickListener(v -> {
                // Tạo Intent để mở màn hình chi tiết bài viết (NewsDetailActivity)
                Intent intent = new Intent(context, NewsDetailActivity.class);
                // Truyền ID hoặc URL của bài viết để NewsDetailActivity biết cần hiển thị bài nào
                // Nên sử dụng một hằng số cho key của extra data
                // Ví dụ: intent.putExtra(NewsDetailActivity.EXTRA_NEWS_URL, news.getArticleUrl());
                intent.putExtra(com.example.appdocbao.utils.Constants.EXTRA_ARTICLE_ID, String.valueOf(news.getId()));
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            // Trả về số lượng bài viết trong danh sách
            return newsList.size();
        }

        /**
         * ViewHolder cho mỗi item tin tức.
         * Chứa các view như hình ảnh thumbnail, tiêu đề, ngày đăng, và nguồn tin.
         */
        static class NewsViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumbnail;
            TextView tvTitle, tvDate, tvSource; // tvSource có thể là null nếu không có trong layout

            public NewsViewHolder(@NonNull View itemView) {
                super(itemView);
                // Ánh xạ các view từ layout của item
                ivThumbnail = itemView.findViewById(R.id.imgThumbnail);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDate = itemView.findViewById(R.id.tvPublishedTime);
                tvSource = itemView.findViewById(R.id.tvSource); // Sẽ là null nếu R.id.tvSource không tồn tại trong item_news.xml
            }
        }
    }
}
