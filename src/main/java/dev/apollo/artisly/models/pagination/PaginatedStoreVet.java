package dev.apollo.artisly.models.pagination;

import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.StoreVet;

import java.util.List;

public record PaginatedStoreVet(List<StoreVet> stores, String column, boolean asc, int page, int limit, int totalPages, long totalStores) {

}
