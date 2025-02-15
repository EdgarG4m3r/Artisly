package dev.apollo.artisly.models.pagination;

import dev.apollo.artisly.models.Order;

import java.util.List;

public record PaginatedOrder(List<Order> orders, String column, boolean asc, int page, int limit, int totalPages, long totalOrders) {
}
