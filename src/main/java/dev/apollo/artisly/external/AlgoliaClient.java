package dev.apollo.artisly.external;

import com.algolia.search.*;
import dev.apollo.artisly.models.Product;

public class AlgoliaClient {

    private final SearchClient searchClient;
    private SearchIndex<Product> productIndex;

    public AlgoliaClient(String applicationId, String apiKey) {
        this.searchClient = DefaultSearchClient.create(applicationId, apiKey);
        productIndex = searchClient.initIndex("products", Product.class);

    }

    public void indexProduct(Product product) {
        productIndex.saveObject(product);
    }

    public void deleteProduct(String productId) {
        productIndex.deleteObject(productId);
    }

    public SearchClient getSearchClient() {
        return searchClient;
    }

    public SearchIndex<Product> getProductIndex() {
        return productIndex;
    }
}
