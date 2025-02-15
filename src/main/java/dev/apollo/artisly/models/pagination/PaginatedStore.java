package dev.apollo.artisly.models.pagination;

import dev.apollo.artisly.models.Store;
import lombok.Getter;

import java.util.List;

public record PaginatedStore(List<Store> stores, String column, boolean asc, int page, int limit, int totalPages, long totalStores) {

}
