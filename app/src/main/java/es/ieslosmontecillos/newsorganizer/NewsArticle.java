package es.ieslosmontecillos.newsorganizer;

public class NewsArticle {
    private String title;
    private String description;
    private String url;
    private String urlToImage;

    public NewsArticle(String title, String description, String url, String urlToImage) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.urlToImage = urlToImage;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }
}
