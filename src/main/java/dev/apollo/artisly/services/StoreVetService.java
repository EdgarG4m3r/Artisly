package dev.apollo.artisly.services;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.datalayer.CRUDSStore;
import dev.apollo.artisly.datalayer.CRUDSStoreVet;
import dev.apollo.artisly.exceptions.AlreadyHaveVetRequest;
import dev.apollo.artisly.exceptions.RequirementNotMet;
import dev.apollo.artisly.exceptions.StoreNotExist;
import dev.apollo.artisly.models.*;
import dev.apollo.artisly.models.pagination.PaginatedOrder;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StoreVetService {

    public static void requestVet(UUID userId, String note) throws AlreadyHaveVetRequest, SQLException, StoreNotExist, RequirementNotMet {
        try (Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<Store> storeOptional = CRUDSStore.readByUserId(connection, userId);
            if(!storeOptional.isPresent())
            {
                throw new StoreNotExist("Toko tidak ditemukan");
            }

            //if (ReviewService.getAverageRatingOfStore(storeOptional.get().id()) < 4.0)
            //{
            //    throw new RequirementNotMet("Toko harus memiliki rating minimal 4.0");
            //}

            PaginatedOrder paginatedOrder = OrderService.getOrdersByStore(storeOptional.get().id(), 1, Integer.MAX_VALUE);
            List<Order> allTimeOrders = paginatedOrder.orders();
            int totalCompletedOrders = 0;
            for (Order order : allTimeOrders)
            {
                if (order.orderStatus().equals(OrderStatus.COMPLETED))
                {
                    totalCompletedOrders++;
                }
            }

            //if (totalCompletedOrders < 10)
            //{
            //    throw new RequirementNotMet("Toko harus memiliki minimal 10 pesanan yang telah selesai");
            //}

            Optional<StoreVet> storeVetOptional = CRUDSStoreVet.readByStoreId(connection, storeOptional.get().id());
            if(storeVetOptional.isPresent())
            {
                throw new AlreadyHaveVetRequest("Toko sudah memiliki permintaan vet");
            }

            CRUDSStoreVet.create(connection, userId, storeOptional.get().id(), note);
        }

    }

    public static List<StoreVet> getStoreVetRequestForAdmins() throws SQLException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection()) {
            return CRUDSStoreVet.readAll(connection);
        }
    }

    public static void acceptVetRequest(UUID storeReportId) throws SQLException, StoreNotExist {
        try(Connection connection = Artisly.instance.getMySQL().getConnection()) {
            connection.setAutoCommit(false);
            Optional<StoreVet> storeVetOptional = CRUDSStoreVet.readById(connection, storeReportId);
            Optional<Store> storeOptional = CRUDSStore.readById(connection, storeVetOptional.get().storeId());
            if(!storeVetOptional.isPresent())
            {
                throw new StoreNotExist("Request vet tidak ditemukan");
            }
            if (!storeOptional.isPresent())
            {
                throw new StoreNotExist("Toko tidak ditemukan");
            }

            StoreVet storeVet = storeVetOptional.get();
            Store store = storeOptional.get();

            CRUDSStoreVet.update(connection, storeVet.id(), storeVet.userId(), storeVet.storeId(), storeVet.note(), storeVet.created(), true, Optional.of(LocalDate.now()));
            CRUDSStore.update(connection, store.id(), store.name(), store.note(), Optional.of(storeVet.id()));

            try
            {
                User user = UserService.show(storeVet.id());
                EmailService.queueEmail(user.email(),
                        "Permintaan vet anda telah diterima",
                    "Permintaan vet anda telah diterima, silahkan login ke website untuk melihat status toko anda");
            }
            catch (Exception e)
            {
                System.out.println("Email gagal dikirim");
            }

            connection.commit();
        }
    }

    public static void rejectVetRequest(UUID storeReportId) throws SQLException, StoreNotExist {
        try(Connection connection = Artisly.instance.getMySQL().getConnection()) {
            connection.setAutoCommit(false);
            Optional<StoreVet> storeVetOptional = CRUDSStoreVet.readById(connection, storeReportId);
            Optional<Store> storeOptional = CRUDSStore.readById(connection, storeVetOptional.get().storeId());
            if(!storeVetOptional.isPresent())
            {
                throw new StoreNotExist("Request vet tidak ditemukan");
            }
            if (!storeOptional.isPresent())
            {
                throw new StoreNotExist("Toko tidak ditemukan");
            }

            StoreVet storeVet = storeVetOptional.get();
            Store store = storeOptional.get();
            CRUDSStore.update(connection, store.id(), store.name(), store.note(), Optional.empty());
            CRUDSStoreVet.delete(connection, storeVet.id());
            try
            {
                User user = UserService.show(storeVet.id());
                EmailService.queueEmail(user.email(),
                        "Permintaan vet anda ditolak",
                        "Mohon maaf, permintaan vet anda ditolak, silahkan pastikan toko anda sudah memenuhi syarat dan ketentuan");
            }
            catch (Exception e)
            {
                System.out.println("Email gagal dikirim");
            }
            connection.commit();
        }
    }

}
