package es.ieslosmontecillos.newsorganizer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private LinearProgressIndicator progressBar;
    private String currentCategory = "general";  // Default category

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.news_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.progress_bar);
        Button btnGeneral = findViewById(R.id.btn_general);
        Button btnBusiness = findViewById(R.id.btn_business);
        Button btnTechnology = findViewById(R.id.btn_technology);
        Button btnSports = findViewById(R.id.btn_sports);


        btnGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCategory = "general";
                fetchNews(currentCategory);
            }
        });

        btnBusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCategory = "business";
                fetchNews(currentCategory);
            }
        });

        btnTechnology.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCategory = "technology";
                fetchNews(currentCategory);
            }
        });

        btnSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCategory = "sports";
                fetchNews(currentCategory);
            }
        });

        // Fetch default category news
        fetchNews(currentCategory);
    }

    private void fetchNews(String category) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://newsapi.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NewsApi newsApi = retrofit.create(NewsApi.class);

        Call<NewsResponse> call = newsApi.getTopHeadlines("us",category, "fdc82c461311443b8e40d2037e0d1930");
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NewsArticle> newsArticles = response.body().getArticles();
                    newsAdapter = new NewsAdapter(newsArticles);
                    recyclerView.setAdapter(newsAdapter);
                    progressBar.setVisibility(View.GONE);  // Ocultar la barra de progreso
                } else {
                    // Manejar el caso de que la respuesta no sea exitosa
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                // Manejar el error
                progressBar.setVisibility(View.GONE);
                Log.i("GOT FAILURE", t.toString());
            }
        });
    }
}
