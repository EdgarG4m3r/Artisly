package dev.apollo.artisly.datalayer;

import dev.apollo.artisly.models.DiscussionReply;
import dev.apollo.artisly.models.pagination.PaginatedDiscussionReply;
import org.checkerframework.checker.index.qual.PolyUpperBound;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CRUDSDiscussionReply {

    public static DiscussionReply create(Connection connection, UUID productId, UUID userId, String discussionReplySender, String discussionReplyContent) throws SQLException {
        String SQL = "INSERT INTO discussion_replies (discussion_reply_id, product_id, user_id, discussion_reply_sender, discussion_reply_content) VALUES (?, ?, ?, ?, ?)";
        try (var ps = connection.prepareStatement(SQL)) {
            LocalDate discussionReplyCreated = LocalDate.now();
            UUID discussionReplyId = UUID.randomUUID();
            ps.setString(1, discussionReplyId.toString());
            ps.setString(2, productId.toString());
            ps.setString(3, userId.toString());
            ps.setString(4, discussionReplySender);
            ps.setString(5, discussionReplyContent);
            ps.executeUpdate();
            return new DiscussionReply(discussionReplyId, productId, userId, discussionReplySender, discussionReplyContent, discussionReplyCreated);
        }
    }

    public static void delete(Connection connection, UUID discussionReplyId) throws SQLException {
        String SQL = "DELETE FROM discussion_replies WHERE discussion_reply_id = ?";
        try (var ps = connection.prepareStatement(SQL)) {
            ps.setString(1, discussionReplyId.toString());
            ps.executeUpdate();
        }
    }

    public static void deleteAllByProductId(Connection connection, UUID productId) throws SQLException {
        String SQL = "DELETE FROM discussion_replies WHERE product_id = ?";
        try (var ps = connection.prepareStatement(SQL)) {
            ps.setString(1, productId.toString());
            ps.executeUpdate();
        }
    }

    public static PaginatedDiscussionReply readByDiscussionByProductId(Connection connection, UUID productId, int page, int size, String sort_by, boolean ascending) throws SQLException
    {
        int totalReplies = countByProductId(connection, productId);
        int totalPages = (int) Math.ceil((double) totalReplies / size);

        String SQL = "SELECT * FROM discussion_replies WHERE product_id = ? ORDER BY " + sort_by + " " + (ascending ? "ASC" : "DESC") + " LIMIT ? OFFSET ?";
        try(var ps = connection.prepareStatement(SQL)) {
            ps.setString(1, productId.toString());
            ps.setInt(2, size);
            ps.setInt(3, (page - 1) * size);
            try(var resultSet = ps.executeQuery()) {
                List<DiscussionReply> discussionReplies = new ArrayList<>();
                while (resultSet.next()) {
                    //UUID id, UUID productId, UUID userId, String sender, String content, LocalDate created
                    DiscussionReply discussionReply = new DiscussionReply(
                            UUID.fromString(resultSet.getString("discussion_reply_id")),
                            UUID.fromString(resultSet.getString("product_id")),
                            UUID.fromString(resultSet.getString("user_id")),
                            resultSet.getString("discussion_reply_sender"),
                            resultSet.getString("discussion_reply_content"),
                            resultSet.getDate("discussion_reply_created").toLocalDate()
                    );
                    discussionReplies.add(discussionReply);
                }
                return new PaginatedDiscussionReply(discussionReplies, sort_by, ascending, page, size, totalPages, totalReplies);
            }
        }
    }

    public static int countByProductId(Connection connection, UUID productId) throws SQLException
    {
        String SQL = "SELECT COUNT(*) FROM discussion_replies WHERE product_id = ?";
        try(var ps = connection.prepareStatement(SQL)) {
            ps.setString(1, productId.toString());
            try(var resultSet = ps.executeQuery()) {
                if(resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }
}
