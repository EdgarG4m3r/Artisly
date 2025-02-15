package dev.apollo.artisly.models.pagination;

import dev.apollo.artisly.models.User;
import lombok.Getter;

import java.util.List;

public record PaginatedUser(List<User> users, String column, boolean asc, int page, int limit, int totalPages, long totalUsers) {

}
