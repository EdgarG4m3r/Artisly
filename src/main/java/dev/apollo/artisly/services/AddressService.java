package dev.apollo.artisly.services;

import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.datalayer.CRUDSAddress;
import dev.apollo.artisly.datalayer.CRUDSImmutableAddress;
import dev.apollo.artisly.exceptions.AddressMaximumCountException;
import dev.apollo.artisly.exceptions.InvalidAddressException;
import dev.apollo.artisly.models.Address;
import dev.apollo.artisly.models.ImmutableAddress;
import dev.apollo.artisly.models.IndonesianCity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AddressService {

    // Maximum address count for each user
    // Hardcoded for now, but can be moved to configuration in the future
    private static final int MAX_ADDRESS_COUNT = 10;

    public static Address createAddress(UUID userId, String receiverName, String receiverPhone, String content, String note, IndonesianCity city) throws SQLException, AddressMaximumCountException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            List<Address> addresses = CRUDSAddress.readByUserId(connection, userId);
            if (addresses.size() >= MAX_ADDRESS_COUNT) {
                throw new AddressMaximumCountException("Anda hanya dapat memiliki maksimal " + MAX_ADDRESS_COUNT + " alamat");
            }

            Address address = CRUDSAddress.create(connection, userId, receiverName, receiverPhone, content, note, city);
            return address;
        }
    }

    public static boolean updateAddress(UUID userId, UUID addressId, String receiverName, String receiverPhone, String content, String note, IndonesianCity city) throws SQLException, InvalidAddressException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            List<Address> addresses = CRUDSAddress.readByUserId(connection, userId);
            for (Address address : addresses) {
                if (address.id().equals(addressId)) {
                    return CRUDSAddress.update(connection, addressId, receiverName, receiverPhone, content, note, city);
                }
            }
            throw new InvalidAddressException("Alamat tidak ditemukan");
        }
    }

    public static boolean deleteAddress(UUID userId, UUID addressId) throws SQLException, InvalidAddressException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            List<Address> addresses = CRUDSAddress.readByUserId(connection, userId);
            for (Address address : addresses) {
                if (address.id().equals(addressId)) {
                    return CRUDSAddress.delete(connection, addressId);
                }
            }
            throw new InvalidAddressException("Alamat tidak ditemukan");
        }
    }

    public static List<Address> readAllAddress(UUID userId) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSAddress.readByUserId(connection, userId);
        }
    }

    public static Address readAddress(UUID userId, UUID addressId) throws SQLException, InvalidAddressException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            List<Address> addresses = CRUDSAddress.readByUserId(connection, userId);
            for (Address address : addresses) {
                if (address.id().equals(addressId)) {
                    return address;
                }
            }
            throw new InvalidAddressException("Alamat tidak ditemukan");
        }
    }

    public static Optional<ImmutableAddress> readImmutableAddress(UUID addressId) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSImmutableAddress.readByImmutableAddressId(connection, addressId);
        }
    }

    public static List<String> getCityList()
    {
        return IndonesianCity.getList();
    }


}
