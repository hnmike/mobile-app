/**
 * Lớp RetrofitClient chịu trách nhiệm cấu hình và cung cấp một instance duy nhất của Retrofit.
 * Nó được sử dụng để thực hiện các yêu cầu mạng đến API của VnExpress.
 *
 * Tác giả: Tran Quy Dinh
 * Ngày tạo: 27/05/2025
 * Người sửa đổi:
 */
package com.example.appdocbao.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String BASE_URL = "https://vnexpress.net/";
    private static Retrofit retrofit = null;

    /**
     * Trả về một instance duy nhất của Retrofit.
     * Nếu instance chưa được tạo, nó sẽ được khởi tạo với cấu hình OkHttpClient tùy chỉnh
     * bao gồm logging interceptor và một interceptor để thêm User-Agent header.
     *
     * @return instance của Retrofit.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Khởi tạo HttpLoggingInterceptor để log thông tin request và response
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Log toàn bộ body của request/response

            // Xây dựng OkHttpClient với các cấu hình timeout và interceptor
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS) // Thời gian chờ kết nối
                    .readTimeout(15, TimeUnit.SECONDS)    // Thời gian chờ đọc dữ liệu
                    .writeTimeout(15, TimeUnit.SECONDS)   // Thời gian chờ ghi dữ liệu
                    .addInterceptor(logging) // Thêm logging interceptor
                    .addInterceptor(chain -> { // Thêm interceptor để tùy chỉnh request
                        okhttp3.Request original = chain.request(); // Lấy request gốc
                        // Xây dựng request mới với User-Agent header được thêm vào
                        okhttp3.Request request = original.newBuilder()
                                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36")
                                .method(original.method(), original.body()) // Giữ nguyên phương thức và body của request gốc
                                .build();
                        return chain.proceed(request); // Thực hiện request mới
                    });

            // Xây dựng instance Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // Đặt URL cơ sở cho các request
                    .addConverterFactory(ScalarsConverterFactory.create()) // Thêm bộ chuyển đổi cho kiểu dữ liệu scalar (ví dụ: String)
                    .addConverterFactory(GsonConverterFactory.create()) // Thêm bộ chuyển đổi Gson để parse JSON
                    .client(httpClient.build()) // Sử dụng OkHttpClient đã cấu hình
                    .build();
        }
        return retrofit; // Trả về instance Retrofit
    }

    /**
     * Trả về một instance của VnExpressService được tạo bởi Retrofit.
     * VnExpressService là một interface định nghĩa các API endpoint của VnExpress.
     *
     * @return instance của VnExpressService.
     */
    public static VnExpressService getVnExpressService() {
        return getClient().create(VnExpressService.class); // Tạo và trả về service interface
    }
}