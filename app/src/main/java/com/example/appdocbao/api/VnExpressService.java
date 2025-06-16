/**
 * Interface VnExpressService định nghĩa các endpoint API để tương tác với trang web VnExpress.
 * Vì VnExpress không cung cấp API công khai, interface này chủ yếu được sử dụng
 * để lấy nội dung HTML thô từ các URL của VnExpress, sau đó nội dung này
 * sẽ được phân tích (parse) bởi một lớp khác (ví dụ: VnExpressParser).
 *
 * Tác giả: [Tên tác giả của bạn, ví dụ: Tran Quy Dinh]
 * Ngày tạo: [Ngày bạn tạo file, ví dụ: 27/05/2024]
 * Người sửa đổi:
 */
package com.example.appdocbao.api;

// Các import không được sử dụng đã được xóa để giữ code sạch sẽ.
// Nếu bạn dự định sử dụng chúng sau này, bạn có thể thêm lại.
// import com.example.appdocbao.data.model.Article;
// import retrofit2.http.Path;
// import retrofit2.http.Query;
// import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface VnExpressService {

    // Ghi chú: Vì VnExpress không có API mở, chúng ta sẽ sử dụng một bộ phân tích tùy chỉnh
    // để trích xuất dữ liệu từ trang web.
    // Đây chỉ là một cấu trúc giữ chỗ (placeholder) cho các phương thức có thể cần thiết.

    /**
     * Thực hiện một yêu cầu GET đến một URL bất kỳ được cung cấp để lấy nội dung HTML của trang đó.
     * Annotation @Url chỉ định rằng tham số 'url' sẽ được sử dụng làm URL đầy đủ cho yêu cầu,
     * bỏ qua base URL đã được cấu hình trong Retrofit client.
     *
     * @param url URL đầy đủ của trang web VnExpress cần lấy nội dung HTML.
     * @return Một đối tượng Call chứa nội dung HTML của trang dưới dạng String.
     */
    @GET // Mặc dù @GET thường đi kèm với một đường dẫn tương đối,
    // việc sử dụng @Url sẽ ghi đè hành vi đó và sử dụng URL được cung cấp làm URL tuyệt đối.
    Call<String> getHtmlContent(@Url String url);

    // Bạn có thể thêm các phương thức khác ở đây nếu cần, ví dụ:
    // @GET("{categoryPath}")
    // Call<String> getCategoryHtml(@Path("categoryPath") String categoryPath);
    //
    // @GET("tin-tuc-24h") // Ví dụ cho một trang cụ thể
    // Call<String> getHomepageHtml();
}