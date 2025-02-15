package dev.apollo.artisly.managers.storage;

public enum Bucket {
    PROFILE_PICTURE("media/user-pictures"),
    PRODUCT_MEDIA("media/product-media"),
    STORE_LOGO("media/store-logos"),
    STORE_BANNER("media/store-banners"),
    DISCUSSION_FILE("media/discussion-files");

    private final String path;

    Bucket(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
