package dev.apollo.artisly.services;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.datalayer.CRUDSDiscussionReply;
import dev.apollo.artisly.datalayer.CRUDSProduct;
import dev.apollo.artisly.datalayer.CRUDSStore;
import dev.apollo.artisly.exceptions.ProductNotExist;
import dev.apollo.artisly.exceptions.StoreNotExist;
import dev.apollo.artisly.models.Product;
import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.pagination.PaginatedDiscussionReply;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class DiscussionService {

    public static void postDiscussionReply(UUID userId, UUID productId, String content) throws SQLException, ProductNotExist, StoreNotExist {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            String sender = "SELLER";
            Optional<Product> product = CRUDSProduct.readByProductId(connection, productId);
            if (!product.isPresent()) {
                throw new ProductNotExist("Produk tidak ditemukan.");
            }
            Optional<Store> store = CRUDSStore.readById(connection, product.get().storeId());
            if (!store.isPresent()) {
                throw new StoreNotExist("Toko tidak ditemukan.");
            }

            if (!store.get().userId().equals(userId)) {
                sender = "BUYER";
            }

            CRUDSDiscussionReply.create(connection, productId, userId, sender, content);
        }
    }


    public static void deleteDiscussionReply(UUID discussionReplyId) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            CRUDSDiscussionReply.delete(connection, discussionReplyId);
        }
    }

    public static PaginatedDiscussionReply index(UUID productId, int page, int size, String sortBy, boolean ascending) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSDiscussionReply.readByDiscussionByProductId(connection, productId, page, size, sortBy, ascending);
        }
    }





}
