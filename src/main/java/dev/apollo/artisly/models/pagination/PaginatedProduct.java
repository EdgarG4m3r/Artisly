package dev.apollo.artisly.models.pagination;

import dev.apollo.artisly.models.Product;

import java.util.List;

public record PaginatedProduct(List<Product> products, String column, boolean asc, int page, int limit, int totalPages, long totalProducts) {

}
