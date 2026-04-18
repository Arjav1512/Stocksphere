package com.stocksphere.model;

public class NewsArticle {
    private String title;
    private String description;
    private String url;
    private String publishedAt;
    private String source;

    public NewsArticle() {}

    public NewsArticle(String title, String description, String url,
                       String publishedAt, String source) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.publishedAt = publishedAt;
        this.source = source;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
