package dev.apollo.artisly.services;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.datalayer.CRUDSStore;
import dev.apollo.artisly.datalayer.CRUDSUser;
import dev.apollo.artisly.exceptions.*;
import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.models.pagination.PaginatedStore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class StoreService {

    public static void createStore(UUID userId, String storeName, String storeNote) throws SQLException, UserNotFoundException, EmailNotVerifiedException, AlreadyHaveStoreException, AccountNotVerifiedException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<User> userOptional = CRUDSUser.readById(connection, userId);
            if(userOptional.isEmpty())
            {
                throw new UserNotFoundException("Akun tidak ditemukan");
            }

            User user = userOptional.get();
            if(user.emailVerified().isEmpty())
            {
                throw new EmailNotVerifiedException("Email belum terverifikasi");
            }

            if(user.nomorKTP().isEmpty())
            {
                throw new AccountNotVerifiedException("Harap masukan nomor KTP");
            }

            Optional<Store> storeOptional = CRUDSStore.readByUserId(connection, userId);
            if(storeOptional.isPresent())
            {
                throw new AlreadyHaveStoreException("Anda sudah memiliki toko");
            }

            CRUDSStore.create(connection, storeName, storeNote, userId);


        }
    }

    public static void updateStore(UUID userId, String storeName, String storeNote) throws SQLException, UserNotFoundException, EmailNotVerifiedException, StoreNotExist, AccountNotVerifiedException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection()) {
            //Connection connection, UUID storeId, String storeName, String storeNote,

            Optional<User> userOptional = CRUDSUser.readById(connection, userId);
            if (userOptional.isEmpty()) {
                throw new UserNotFoundException("Akun tidak ditemukan");
            }

            User user = userOptional.get();
            if (user.emailVerified().isEmpty()) {
                throw new EmailNotVerifiedException("Email belum terverifikasi");
            }

            if (user.nomorKTP().isEmpty()) {
                throw new AccountNotVerifiedException("Harap masukan nomor KTP");
            }

            Optional<Store> storeOptional = CRUDSStore.readByUserId(connection, userId);
            if (storeOptional.isEmpty()) {
                throw new StoreNotExist("Anda belum memiliki toko");
            }

            Store store = storeOptional.get();
            CRUDSStore.update(connection, store.id(), storeName, storeNote, store.storeVetId());
        }
    }


    public static Optional<Store> getStore(UUID userId) throws SQLException, UserNotFoundException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<User> userOptional = CRUDSUser.readById(connection, userId);
            if(userOptional.isEmpty())
            {
                throw new UserNotFoundException("Akun tidak ditemukan");
            }

            return CRUDSStore.readByUserId(connection, userId);
        }
    }

    public static PaginatedStore getStores(int page, int limit, String sort_by, boolean asc) throws SQLException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSStore.getAll(connection, page, limit, sort_by, asc);
        }
    }

    public static Optional<Store> getStoreById(UUID storeId) throws SQLException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSStore.readById(connection, storeId);
        }
    }

}
