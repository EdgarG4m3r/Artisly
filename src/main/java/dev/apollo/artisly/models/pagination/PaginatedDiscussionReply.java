package dev.apollo.artisly.models.pagination;

import dev.apollo.artisly.models.DiscussionReply;

import java.util.List;

public record PaginatedDiscussionReply(List<DiscussionReply> discussionReplies, String column, boolean asc, int page, int limit, int totalPages,
                                       long totalDiscussionReplies) {


}
