package dev.apollo.artisly.models.pagination;

import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.StoreReport;

import java.util.List;

public record PaginatedStoreReport(List<StoreReport> stores, String column, boolean asc, int page, int limit, int totalPages, long totalStores) {

}
