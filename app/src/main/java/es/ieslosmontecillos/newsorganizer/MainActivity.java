package es.ieslosmontecillos.newsorganizer;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;

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
    private Spinner regionSpinner;
    private String selectedRegion = "us"; // Default region
    //private SearchView searchView;
    private NewsDatabaseHelper dbHelper;

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

        //searchView = findViewById(R.id.search_view);

        // Configurar el Spinner
        regionSpinner = findViewById(R.id.region_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.regions_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(adapter);

        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedRegion = getResources().getStringArray(R.array.regions_values)[position];
                fetchNews(currentCategory);  // Fetch news based on selected region and category
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });

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

        // Configurar el SearchView
        /**searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchNews(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });*/

        // Fetch default category news
        fetchNews(currentCategory);
    }

    private void fetchNews(String category) {
        progressBar.setVisibility(View.VISIBLE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://newsapi.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NewsApi newsApi = retrofit.create(NewsApi.class);

        Call<NewsResponse> call = newsApi.getTopHeadlines(selectedRegion,category, "fdc82c461311443b8e40d2037e0d1930");
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
                Log.i("ERROR", t.toString());
            }
        });
    }

    /**private void searchNews(String query) {
        progressBar.setVisibility(View.VISIBLE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://newsapi.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NewsApi newsApi = retrofit.create(NewsApi.class);

        Call<NewsResponse> call = newsApi.searchNews(selectedRegion, query, "fdc82c461311443b8e40d2037e0d1930");
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
    }*/
    private void saveNewsToDatabase(List<NewsArticle> newsArticles) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (NewsArticle article : newsArticles) {
            // Verificar si la noticia ya está en la base de datos
            Cursor cursor = db.query(NewsDatabaseHelper.TABLE_NEWS,
                    new String[]{NewsDatabaseHelper.COLUMN_URL},
                    NewsDatabaseHelper.COLUMN_URL + " = ?",
                    new String[]{article.getUrl()},
                    null, null, null);

            if (cursor.getCount() == 0) {
                // La noticia no está en la base de datos, así que la insertamos
                ContentValues values = new ContentValues();
                values.put(NewsDatabaseHelper.COLUMN_TITLE, article.getTitle());
                values.put(NewsDatabaseHelper.COLUMN_DESCRIPTION, article.getDescription());
                values.put(NewsDatabaseHelper.COLUMN_URL, article.getUrl());
                values.put(NewsDatabaseHelper.COLUMN_URL_TO_IMAGE, article.getUrlToImage());

                db.insert(NewsDatabaseHelper.TABLE_NEWS, null, values);
            }
            cursor.close();
        }

        // Eliminar noticias viejas para mantener solo las 50 más recientes
        db.execSQL("DELETE FROM " + NewsDatabaseHelper.TABLE_NEWS +
                " WHERE " + NewsDatabaseHelper.COLUMN_ID + " NOT IN (" +
                "SELECT " + NewsDatabaseHelper.COLUMN_ID +
                " FROM " + NewsDatabaseHelper.TABLE_NEWS +
                " ORDER BY " + NewsDatabaseHelper.COLUMN_ID + " DESC" +
                " LIMIT 50)");

        db.close();
    }
}

//TODO fix: SearchView always returning 0 news