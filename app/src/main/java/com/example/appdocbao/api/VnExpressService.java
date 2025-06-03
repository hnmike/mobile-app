package com.example.appdocbao.api;

import com.example.appdocbao.data.model.Article;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

import java.util.List;

public interface VnExpressService {
    
    // Since VnExpress doesn't have an open API, we'll use a custom parser to scrape the website
    /****
     * Sends an HTTP GET request to the specified URL and retrieves the raw HTML content as a string.
     *
     * @param url the full URL to fetch HTML content from
     * @return a Call object for the HTTP request, yielding the HTML response as a string
     */
    
    @GET
    Call<String> getHtmlContent(@Url String url);
} 