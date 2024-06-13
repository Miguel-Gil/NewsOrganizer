package es.ieslosmontecillos.newsorganizer;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApi {
    @GET("v2/top-headlines")
    Call<NewsResponse> getTopHeadlines(
            @Query("country") String country,
            @Query("category") String category,
            @Query("apiKey") String apiKey
    );

    @GET("v2/everything")
    Call<NewsResponse> searchNews(
            @Query("language") String language,
            @Query("q") String query,
            @Query("apiKey") String apiKey
    );
}