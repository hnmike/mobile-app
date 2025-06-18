# HomeActivity Refactoring Documentation

## Tổng quan
File `HomeActivity.java` ban đầu rất lớn (795 dòng) và chứa nhiều logic phức tạp. Đã được chia nhỏ thành các component riêng biệt để dễ bảo trì và mở rộng.

## Cấu trúc mới

### 1. HomeViewModel.java
**Vị trí**: `app/src/main/java/com/example/appdocbao/ui/home/HomeViewModel.java`

**Chức năng**:
- Xử lý tất cả logic nghiệp vụ
- Quản lý việc tải dữ liệu từ API
- Sử dụng LiveData để cập nhật UI
- Xử lý lỗi và trạng thái loading

**Các phương thức chính**:
- `loadDataFromApi()`: Tải dữ liệu từ API
- `refreshData()`: Làm mới dữ liệu
- `getCategories()`: Lấy danh sách danh mục
- `getCategoryNewsMap()`: Lấy map tin tức theo danh mục

**Danh mục hiển thị trên trang chủ**:
- 🔥 Bài viết nổi bật (ID: 0)
- 📰 Thời sự (ID: 1)
- 🌎 Thế giới (ID: 2)
- 💼 Kinh doanh (ID: 3)
- 🎭 Giải trí (ID: 4)
- ⚽ Thể thao (ID: 5)
- ⚖️ Pháp luật (ID: 6)
- 🎓 Giáo dục (ID: 7)
- 🏥 Sức khỏe (ID: 8)
- 🏠 Đời sống (ID: 9)
- ✈️ Du lịch (ID: 10)
- 🔬 Khoa học (ID: 11)
- 💻 Số hóa (ID: 12)

### 2. ArticleAdapter.java
**Vị trí**: `app/src/main/java/com/example/appdocbao/ui/home/ArticleAdapter.java`

**Chức năng**:
- Hiển thị danh sách bài viết theo chiều ngang
- Xử lý sự kiện click vào bài viết
- Tải hình ảnh bằng Glide
- Format ngày tháng (hiển thị "Vừa đăng" thay vì "N/A")

**Layout**: `item_news_horizontal.xml`

### 3. TrendingArticleAdapter.java
**Vị trí**: `app/src/main/java/com/example/appdocbao/ui/home/TrendingArticleAdapter.java`

**Chức năng**:
- Hiển thị bài viết nổi bật với rank
- Styling khác biệt cho top 3 bài viết
- Xử lý sự kiện click
- Format ngày tháng (hiển thị "Vừa đăng" thay vì "N/A")

**Layout**: `item_trending_news.xml`

### 4. HomeCategoriesAdapter.java
**Vị trí**: `app/src/main/java/com/example/appdocbao/ui/home/HomeCategoriesAdapter.java`

**Chức năng**:
- Hiển thị danh mục và bài viết của từng danh mục
- Quản lý adapter con cho mỗi danh mục
- Xử lý sự kiện "Xem tất cả"
- Ẩn nút "Xem tất cả" cho danh mục "Bài viết nổi bật"

**Layout**: `item_category_with_news.xml`

### 5. HomeActivity.java (Đã refactor)
**Vị trí**: `app/src/main/java/com/example/appdocbao/ui/home/HomeActivity.java`

**Chức năng**:
- Chỉ xử lý UI và navigation
- Sử dụng ViewModel để lấy dữ liệu
- Setup observers cho LiveData
- Xử lý bottom navigation

## Layout Files

### item_news_horizontal.xml
- Layout cho bài viết hiển thị theo chiều ngang
- Kích thước cố định 280dp width
- Hình ảnh 120x80dp

### item_trending_news.xml
- Layout cho bài viết nổi bật
- Có rank indicator
- Hình ảnh 80x60dp

### item_category_with_news.xml
- Layout cho mỗi danh mục
- Chứa tên danh mục, nút "Xem tất cả"
- RecyclerView con cho bài viết

## Drawable Resources

### bg_trending_normal.xml
- Background cho rank bình thường
- Màu xám nhạt

### bg_trending_top.xml
- Background cho top 3 rank
- Gradient màu cam

## Cải tiến gần đây

### 1. Danh mục được tối ưu
- **Đã bỏ**: Xe, Ý kiến, Tâm sự khỏi trang chủ
- **Giữ lại**: 13 danh mục chính + Bài viết nổi bật
- **Lý do**: Tập trung vào nội dung quan trọng nhất

### 2. Cải thiện hiển thị ngày tháng
- **Thay đổi**: "N/A" → "Vừa đăng" → **Thời gian tương đối thông minh**
- **Áp dụng**: Cả ArticleAdapter và TrendingArticleAdapter
- **Lý do**: Text có nghĩa hơn cho người dùng
- **Cải tiến mới**: 
  - Sử dụng `DateUtils.getRelativeTimeSpan()` để hiển thị thời gian tương đối
  - Format: "Vừa xong", "5 phút trước", "2 giờ trước", "3 ngày trước"
  - Cải thiện VnExpressParser để lấy thời gian thực từ HTML thay vì thời gian hiện tại

## Lợi ích của việc refactor

1. **Tách biệt trách nhiệm**: UI và logic nghiệp vụ được tách riêng
2. **Dễ bảo trì**: Mỗi component có chức năng rõ ràng
3. **Tái sử dụng**: Các adapter có thể dùng ở nhiều nơi
4. **Test dễ dàng**: ViewModel có thể test độc lập
5. **Mở rộng**: Dễ thêm tính năng mới

## Cách sử dụng

1. **HomeActivity**: Khởi tạo ViewModel và setup observers
2. **HomeViewModel**: Gọi `loadDataFromApi()` để tải dữ liệu
3. **Adapters**: Tự động cập nhật khi dữ liệu thay đổi
4. **Navigation**: Xử lý qua HomeActivity

## Lưu ý

- Tất cả API calls được xử lý trong ViewModel
- UI chỉ phản ứng với thay đổi dữ liệu qua LiveData
- Error handling được tập trung trong ViewModel
- Loading state được quản lý tự động
- Danh mục "Bài viết nổi bật" không có nút "Xem tất cả" 