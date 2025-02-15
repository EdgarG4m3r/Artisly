package dev.apollo.artisly.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.simple.JSONObject;

public class ProductImage {
    @JsonProperty("filename")
    private String fileName;
    @JsonProperty("url")
    private String url;
    @JsonProperty("is_thumbnail")
    private boolean isThumbnail;

    public ProductImage(String fileName, String url, boolean isThumbnail) {
        this.fileName = fileName;
        this.url = url;
        this.isThumbnail = isThumbnail;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public boolean isThumbnail() {
        return isThumbnail;
    }

    public JSONObject toJSON()
    {
        JSONObject object = new JSONObject();
        object.put("filename", fileName);
        object.put("url", url);
        object.put("is_thumbnail", isThumbnail);
        return object;
    }
}
