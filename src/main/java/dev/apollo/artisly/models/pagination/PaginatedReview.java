package dev.apollo.artisly.models.pagination;

import dev.apollo.artisly.models.Review;

import java.util.List;

public record PaginatedReview(List<Review> reviews, String column, boolean asc, int page, int limit, int totalPages, long totalReviews) {
}
