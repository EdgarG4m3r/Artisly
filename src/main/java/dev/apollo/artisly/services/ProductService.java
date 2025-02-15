package dev.apollo.artisly.services;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.datalayer.CRUDSDiscussionReply;
import dev.apollo.artisly.datalayer.CRUDSImmutableProduct;
import dev.apollo.artisly.datalayer.CRUDSOrder;
import dev.apollo.artisly.datalayer.CRUDSProduct;
import dev.apollo.artisly.exceptions.CategoryNotExist;
import dev.apollo.artisly.exceptions.ProductNotExist;
import dev.apollo.artisly.exceptions.StoreNotExist;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.models.Category;
import dev.apollo.artisly.models.ImmutableProduct;
import dev.apollo.artisly.models.Product;
import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.pagination.PaginatedProduct;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class ProductService {

    public static Product createProduct(UUID storeId, UUID categoryId, String productName, String productDescription, double productPrice, int productStock) throws SQLException, CategoryNotExist, StoreNotExist {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<Category> category = CategoryService.getCategory(categoryId);
            if (category.isEmpty())
            {
                throw new CategoryNotExist("Kategori tidak ditemukan. Harap cek kembali kategori yang anda masukkan");
            }

            Optional<Store> store = StoreService.getStoreById(storeId);

            if (store.isEmpty())
            {
                throw new StoreNotExist("Toko tidak ditemukan. Harap cek kembali toko yang anda masukkan");
            }

            return CRUDSProduct.create(connection, storeId, categoryId, productName, productDescription, productPrice, productStock);
        }
    }

    public static boolean deleteProduct(UUID productId) throws SQLException, ProductNotExist {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<Product> product = getProduct(productId);
            if (product.isEmpty())
            {
                throw new ProductNotExist("Produk tidak ditemukan. Harap cek kembali produk yang anda masukkan");
            }

            return CRUDSProduct.delete(connection, productId);
        }
    }

    public static boolean deleteProductOfUser(UUID userId, UUID productId) throws SQLException, ProductNotExist, StoreNotExist, UserNotFoundException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<Product> product = getProduct(productId);
            if (product.isEmpty())
            {
                throw new ProductNotExist("Produk tidak ditemukan. Harap cek kembali produk yang anda masukkan");
            }

            Optional<Store> store = StoreService.getStore(userId);
            if (store.isEmpty())
            {
                throw new StoreNotExist("Anda tidak memiliki toko. Silahkan buat toko terlebih dahulu");
            }

            if (product.get().storeId().compareTo(store.get().id()) != 0)
            {
                throw new ProductNotExist("Produk tidak ditemukan. Harap cek kembali produk yang anda masukkan");
            }
            connection.setAutoCommit(false);
            CRUDSDiscussionReply.deleteAllByProductId(connection, productId);
            CRUDSProduct.delete(connection, productId);
            connection.commit();
            return true;
        }
    }

    public static boolean updateProduct(UUID productId, UUID categoryId, String productName, String productDescription, double productPrice, int productStock) throws SQLException, CategoryNotExist, ProductNotExist {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<Category> category = CategoryService.getCategory(categoryId);
            if (category.isEmpty())
            {
                throw new CategoryNotExist("Kategori tidak ditemukan. Harap cek kembali kategori yang anda masukkan");
            }

            Optional<Product> product = getProduct(productId);
            if (product.isEmpty())
            {
                throw new ProductNotExist("Produk tidak ditemukan. Harap cek kembali produk yang anda masukkan");
            }

            return CRUDSProduct.update(connection, productId, categoryId, productName, productDescription, productPrice, productStock);
        }
    }

    public static boolean updateProductOfUser(UUID userId, UUID productId, UUID categoryId, String productName, String productDescription, double productPrice, int productStock) throws SQLException, CategoryNotExist, ProductNotExist, StoreNotExist, UserNotFoundException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<Category> category = CategoryService.getCategory(categoryId);
            if (category.isEmpty())
            {
                throw new CategoryNotExist("Kategori tidak ditemukan. Harap cek kembali kategori yang anda masukkan");
            }

            Optional<Product> product = getProduct(productId);
            if (product.isEmpty())
            {
                throw new ProductNotExist("Produk tidak ditemukan. Harap cek kembali produk yang anda masukkan");
            }

            Optional<Store> store = StoreService.getStore(userId);
            if (store.isEmpty())
            {
                throw new StoreNotExist("Anda tidak memiliki toko. Silahkan buat toko terlebih dahulu");
            }

            if (product.get().storeId().compareTo(store.get().id()) != 0)
            {
                throw new ProductNotExist("Produk tidak ditemukan. Harap cek kembali produk yang anda masukkan");
            }

            return CRUDSProduct.update(connection, productId, categoryId, productName, productDescription, productPrice, productStock);
        }
    }

    public static Optional<Product> getProduct(UUID id) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSProduct.readByProductId(connection, id);
        }
    }

    public static ImmutableProduct copyProduct(UUID productId) throws ProductNotExist, SQLException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Product product = getProduct(productId).orElseThrow(() -> new ProductNotExist("Produk tidak ditemukan"));
            return CRUDSImmutableProduct.create(connection, product);
        }
    }

    public static PaginatedProduct getProductsFromStore(UUID storeId, String query, int page, int limit, String sort_by, boolean asc) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSProduct.searchByStore(connection, storeId, query, page, limit, sort_by, asc);
        }
    }

    public static PaginatedProduct getProductsFromCategory(UUID categoryId, int page, int limit, String sort_by, boolean asc) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSProduct.readByCategoryId(connection, categoryId, page, limit, sort_by, asc);
        }
    }

    public static Optional<ImmutableProduct> readImmutableProduct(UUID immutableProductId) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSImmutableProduct.readByProductId(connection, immutableProductId);
        }
    }

    public static PaginatedProduct getProducts(String query, int page, int limit, String sort_by, boolean ascending) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSProduct.search(connection, query, page, limit, sort_by, ascending);
        }
    }

    public static PaginatedProduct getPriorityProducts(String query, int page, int limit, String sort_by, boolean ascending) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSProduct.searchPriority(connection, query, page, limit, sort_by, ascending);
        }
    }

}
