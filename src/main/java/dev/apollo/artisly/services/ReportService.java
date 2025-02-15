package dev.apollo.artisly.services;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.datalayer.CRUDSImmutableProduct;
import dev.apollo.artisly.datalayer.CRUDSStoreReport;
import dev.apollo.artisly.exceptions.ProductNotExist;
import dev.apollo.artisly.exceptions.StoreNotExist;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.models.ImmutableProduct;
import dev.apollo.artisly.models.Store;
import dev.apollo.artisly.models.StoreReport;
import dev.apollo.artisly.models.User;
import dev.apollo.artisly.response.StandarizedResponses;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ReportService {

    public static void createReport(UUID reportedStore, UUID reportedBy, Optional<UUID> reportedProduct, String reportReason) throws SQLException, ProductNotExist {
        try(Connection connection = Artisly.instance.getMySQL().getConnection()) {
            connection.setAutoCommit(false);
            if (reportedProduct.isPresent()) {
                ImmutableProduct immutableProduct = ProductService.copyProduct(reportedProduct.get());
                Artisly.instance.getMediaService().copyProductImagesToImmutable(reportedProduct.get(), immutableProduct.id());
                CRUDSStoreReport.create(connection, reportedStore, reportedBy, Optional.of(immutableProduct.id()), reportReason);
                return;
            }
            CRUDSStoreReport.create(connection, reportedStore, reportedBy, Optional.empty(), reportReason);
            connection.commit();
            return;
        }
    }

    public static List<StoreReport> getStoreReportsForAdmins() throws SQLException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection()) {
            return CRUDSStoreReport.readAll(connection);
        }
    }

    public static void processReport(UUID reportId, boolean isResolved) throws SQLException, StoreNotExist, UserNotFoundException {
        try (Connection connection = Artisly.instance.getMySQL().getConnection()) {
            Optional<StoreReport> storeReportOptional = CRUDSStoreReport.readById(connection, reportId);
            if (!storeReportOptional.isPresent()) {
                throw new StoreNotExist("Laporan tidak ditemukan");
            }

            StoreReport storeReport = storeReportOptional.get();
            CRUDSStoreReport.update(connection, storeReport.id(), isResolved);

            if (isResolved) {
                Optional<Store> storeOptional = StoreService.getStoreById(storeReport.storeId());
                if (storeOptional.isPresent()) {
                    Store store = storeOptional.get();
                    User user = UserService.show(store.userId());
                    User reporter = UserService.show(storeReport.userId());
                    EmailService.queueEmail(user.email(),
                            "Penanganan Laporan Toko",
                            "Halo " + user.firstName() + " " + user.lastName() + ", Toko Anda telah dilaporkan oleh pengguna lain. Kami telah meninjau laporan tersebut dan menemukan bahwa laporan tersebut benar. Kami akan mengambil tindakan bila terjadi lagi.");

                    EmailService.queueEmail(reporter.email(),
                            "Penanganan Laporan Toko",
                            "Halo " + reporter.firstName() + " " + reporter.lastName() + ", Laporan Anda telah ditangani oleh admin. Kami telah meninjau laporan tersebut dan menemukan bahwa laporan tersebut benar. Kami akan mengambil tindakan bila terjadi lagi.");
                }
            } else {
                User reporter = UserService.show(storeReport.userId());
                EmailService.queueEmail(reporter.email(),
                        "Penanganan Laporan Toko",
                        "Halo " + reporter.firstName() + " " + reporter.lastName() + ", Laporan Anda telah ditangani oleh admin. Kami telah meninjau laporan tersebut dan belum menemukan bukti yang cukup. Kami akan mengambil tindakan bila terjadi lagi.");
            }

            CRUDSStoreReport.delete(connection, storeReport.id());
        }
    }


}
