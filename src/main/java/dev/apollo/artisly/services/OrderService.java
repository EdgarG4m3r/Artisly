package dev.apollo.artisly.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.datalayer.*;
import dev.apollo.artisly.exceptions.InvalidAddressException;
import dev.apollo.artisly.exceptions.InvalidOrderException;
import dev.apollo.artisly.exceptions.StoreNotExist;
import dev.apollo.artisly.models.*;
import dev.apollo.artisly.models.pagination.PaginatedOrder;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class OrderService {

    /**
     * Done by buyer
     * @param userId
     * @param addressId
     * @throws SQLException
     * @throws InvalidAddressException
     */
    public static void createOrder(UUID userId, UUID addressId) throws SQLException, InvalidAddressException, InvalidOrderException {
        Address address = AddressService.readAddress(userId, addressId);
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            connection.setAutoCommit(false);
            User buyer = CRUDSUser.readById(connection, userId).get();
            Cart cart = CartService.getCart(userId);
            Map<Product, Integer> productList = cart.getFullProductList();
            productList.entrySet().removeIf(entry -> entry.getKey().stock() < entry.getValue());

            if (productList.size() == 0)
                throw new InvalidOrderException("Tidak ada produk yang bisa dipesan dari cart");

            for (Map.Entry<Product, Integer> entry : productList.entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue();

                try {
                    ImmutableAddress immutableAddress = CRUDSImmutableAddress.create(connection, userId, addressId, address.receiverName(), address.receiverPhone(), address.content(), address.note(), address.city());
                    ImmutableProduct immutableProduct = CRUDSImmutableProduct.create(connection, product);
                    Artisly.instance.getMediaService().copyProductImagesToImmutable(product.id(), immutableProduct.id());
                    Order order = CRUDSOrder.create(connection, product.storeId(), userId, immutableProduct.id(), quantity, product.price() * quantity, immutableAddress.id(), OrderStatus.CREATED);
                    CRUDSOrderRecord.create(connection, order.id(), OrderStatus.CREATED, Optional.of("Order created by " + buyer.firstName() + " " + buyer.lastName() + " (" + buyer.email() + ")"));

                    Optional<Store> optionalStore = CRUDSStore.readById(connection, product.storeId());
                    if (optionalStore.isPresent()) {
                        Store store = optionalStore.get();
                        User storeOwner = CRUDSUser.readById(connection, store.userId()).get();
                        EmailService.queueEmail(storeOwner.email(), "New order #" + order.id(), "You have a new order #" + order.id().toString().split("-")[0] + " from " + buyer.firstName() + " " + buyer.lastName() + " (" + buyer.email() + "). " + quantity + "x " + product.name() + " for " + String.format("Rp%,.2f", product.price() * quantity) + ".");
                        byte[] outputStream = generatePdf(order.id(), buyer.firstName() + " " + buyer.lastName(), buyer.email(), storeOwner.firstName() + " " + storeOwner.lastName(), storeOwner.email(), productList);
                        EmailService.queueEmail(buyer.email(), "Order #" + order.id() + " created", "Your order #" + order.id().toString().split("-")[0] + " has been created. You will be notified when the order is shipped.", "INV-" + order.id() + ".pdf", outputStream);
                    }
                    CartService.removeProductFromCart(userId, product.id().toString());
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                } catch (DocumentException e) {
                    e.printStackTrace();
                    throw new InvalidOrderException("Failed to generate PDF");
                }
            }
            connection.commit();
        }
    }

    /**
     * Dome by the store owner
     * @param userId
     * @param orderId
     * @throws SQLException
     * @throws StoreNotExist
     * @throws InvalidOrderException
     */
    public static void processOrder(UUID userId, UUID orderId) throws SQLException, StoreNotExist, InvalidOrderException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            Optional<Store> optionalStore = CRUDSStore.readByUserId(connection, userId);
            if (!optionalStore.isPresent()) {
                throw new StoreNotExist("User is not a store owner");
            }

            Store store = optionalStore.get();
            Optional<Order> optionalOrder = CRUDSOrder.readByOrderId(connection, orderId);
            if (!optionalOrder.isPresent()) {
                throw new InvalidOrderException("Order does not exist");
            }
            Order order = optionalOrder.get();

            if (!order.storeId().equals(store.id()))
            {
                throw new InvalidOrderException("Order does not exist");
            }

            if (!order.orderStatus().equals(OrderStatus.CREATED))
            {
                throw new InvalidOrderException("Cannot process order that has been processed before!");
            }

            ImmutableProduct immutableProduct = CRUDSImmutableProduct.readByProductId(connection, order.immuteableProductId()).get();
            Product product = CRUDSProduct.readByProductId(connection, immutableProduct.productId()).get();
            connection.setAutoCommit(false);
            User buyer = CRUDSUser.readById(connection, order.userId()).get();
            try {
                CRUDSOrder.updateOrderStatus(connection, orderId, OrderStatus.PROCESSED);
                CRUDSOrderRecord.create(connection, orderId, OrderStatus.PROCESSED, Optional.of("Order accepted by " + CRUDSUser.readById(connection, userId).get().firstName() + " " + CRUDSUser.readById(connection, userId).get().lastName() + " (" + CRUDSUser.readById(connection, userId).get().email() + ")"));
                CRUDSProduct.update(connection, product.id(), product.categoryId(), product.name(), product.description(), product.price(), product.stock() - order.quantity());
                connection.commit();
                EmailService.queueEmail(buyer.email(), "Order #" + order.id() + " accepted", "Your order #" + order.id().toString().split("-")[0] + " has been accepted by " + CRUDSUser.readById(connection, userId).get().firstName() + " " + CRUDSUser.readById(connection, userId).get().lastName() + " (" + CRUDSUser.readById(connection, userId).get().email() + ").");
                EmailService.queueEmail(CRUDSUser.readById(connection, store.userId()).get().email(), "Order #" + order.id() + " accepted", "Your order #" + order.id().toString().split("-")[0] + " has been accepted by " + CRUDSUser.readById(connection, userId).get().firstName() + " " + CRUDSUser.readById(connection, userId).get().lastName() + " (" + CRUDSUser.readById(connection, userId).get().email() + ").");
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public static void completeOrder(UUID userId, UUID orderId) throws SQLException, InvalidOrderException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            connection.setAutoCommit(false);
            Optional<Order> optionalOrder = CRUDSOrder.readByOrderId(connection, orderId);
            if (optionalOrder.isEmpty())
            {
                throw new InvalidOrderException("Penanan dengan id " + orderId + " tidak ditemukan");
            }
            Order order = optionalOrder.get();
            if (!order.userId().equals(userId))
            {
                throw new InvalidOrderException("Penanan dengan id " + orderId + " tidak ditemukan");
            }

            if (order.orderStatus() != OrderStatus.PROCESSED)
            {
                throw new InvalidOrderException("Tidak bisa menyelesaikan pesanan yang belum diproses");
            }

            try
            {
                CRUDSOrder.updateOrderStatus(connection, orderId, OrderStatus.COMPLETED);
                CRUDSOrderRecord.create(connection, orderId, OrderStatus.COMPLETED, Optional.of("Buyer completed the order"));
                connection.commit();
            }
            catch (SQLException e)
            {
                connection.rollback();
                throw e;
            }
        }
    }


    public static void cancelOrder(UUID userId, UUID orderId) throws SQLException, InvalidOrderException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            connection.setAutoCommit(false);
            Optional<Order> optionalOrder = CRUDSOrder.readByOrderId(connection, orderId);
            if (optionalOrder.isEmpty())
            {
                throw new InvalidOrderException("Order dengan id #" + orderId + " tidak ditemukan");
            }
            Order order = optionalOrder.get();

            Optional<User> optionalBuyer = CRUDSUser.readById(connection, order.userId());
            Optional<Store> optionalStore = CRUDSStore.readById(connection, order.storeId());
            if (optionalBuyer.isEmpty() || optionalStore.isEmpty())
            {
                throw new InvalidOrderException("Order dengan id #" + orderId + " tidak ditemukan");
            }
            User buyer = optionalBuyer.get();
            Store store = optionalStore.get();
            User storeOwner = CRUDSUser.readById(connection, store.userId()).get();
            
            /**if (!order.userId().equals(userId) && !storeOwner.id().equals(userId))
            {
                throw new InvalidOrderException("Order dengan id #" + orderId + " tidak ditemukan");
            }**/
            if (!order.userId().equals(userId))
            {
                if (!store.userId().equals(userId))
                {
                    throw new InvalidOrderException("Order dengan id #" + orderId + " tidak ditemukan");
                }
            }

            if (order.orderStatus() != OrderStatus.CREATED)
            {
                throw new InvalidOrderException("Order dengan id #" + orderId + " tidak dapat dibatalkan karena sudah diproses");
            }


            String cancelledBy = "Buyer";
            if (storeOwner.id().equals(userId))
            {
                cancelledBy = "Seller";
            }

            try
            {
                CRUDSOrder.updateOrderStatus(connection, orderId, OrderStatus.CANCELLED);
                CRUDSOrderRecord.create(connection, orderId, OrderStatus.CANCELLED, Optional.of(cancelledBy + " cancelled the order"));
                connection.commit();
                User canceller = cancelledBy.equals("Buyer") ? buyer : storeOwner;
                EmailService.queueEmail(buyer.email(),
                        "Order #" + order.id() + " cancelled",
                        "Your order #" + order.id().toString().split("-")[0] + " has been cancelled by " + canceller.firstName() + " " + canceller.lastName() + " (" + canceller.email() + ").");
                EmailService.queueEmail(storeOwner.email(),
                        "Order #" + order.id() + " cancelled",
                        "Your order #" + order.id().toString().split("-")[0] + " has been cancelled by " + canceller.firstName() + " " + canceller.lastName() + " (" + canceller.email() + ").");
            }
            catch (SQLException e)
            {
                connection.rollback();
                throw e;
            }

        }
    }

    public static PaginatedOrder getOrders(UUID userId, int page, int pageSize) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSOrder.readByUserId(connection, userId, page, pageSize);
        }
    }

    public static List<Order> getOrders() throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSOrder.readAll(connection);
        }
    }

    public static PaginatedOrder getOrdersByStore(UUID storeId, int page, int pageSize) throws SQLException {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSOrder.readByStoreId(connection, storeId, page, pageSize);
        }
    }

    public static PaginatedOrder getOrdersByProduct(UUID productId, int page, int pageSize, String sort_by, boolean ascending) throws SQLException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSOrder.readByProductId(connection, productId, page, pageSize, sort_by, ascending);
        }
    }

    public static List<OrderRecord> getOrderRecords(UUID orderId) throws SQLException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSOrderRecord.readByOrderId(connection, orderId);
        }
    }

    public static Optional<Order> getOrder(UUID orderId) throws SQLException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection())
        {
            return CRUDSOrder.readByOrderId(connection, orderId);
        }
    }

    public static List<Order> getOrdersForAdmin(String range) throws SQLException
    {
        try(Connection connection = Artisly.instance.getMySQL().getConnection()) {
            if (range.equalsIgnoreCase("all")) return CRUDSOrder.readAll(connection);
            return CRUDSOrder.readAll(connection, range);
        }
    }

    private static byte[] generatePdf(UUID id, String buyerName, String buyerEmail, String sellerName, String sellerEmail, Map<Product, Integer> productList) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
        Paragraph header = new Paragraph("Invoice", headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        // Added space
        document.add(new Paragraph("\n"));

        Font recipientFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph recipient = new Paragraph("Ditujukan kepada:\n" + buyerName + "\n" + buyerEmail, recipientFont);
        document.add(recipient);

        // Added space
        document.add(new Paragraph("\n"));

        Paragraph issuer = new Paragraph("Diterbitkan oleh:\n" + sellerName + "\n" + sellerEmail, recipientFont);
        document.add(issuer);

        // Added space
        document.add(new Paragraph("\n"));

        // Add ID
        Paragraph idParagraph = new Paragraph("ID: " + id.toString(), recipientFont);
        document.add(idParagraph);

        // Added space
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(4); // Four columns table
        table.addCell("Nama Produk");
        table.addCell("Jumlah");
        table.addCell("Harga Satuan");
        table.addCell("Harga Total");

        for (Map.Entry<Product, Integer> entry : productList.entrySet()) {
            table.addCell(entry.getKey().name());
            table.addCell(String.valueOf(entry.getValue()));
            table.addCell(String.format("Rp. %,.2f", entry.getKey().price()));
            table.addCell(String.format("Rp. %,.2f", entry.getKey().price() * entry.getValue()));
        }

        document.add(table);

        // Added space
        document.add(new Paragraph("\n"));

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        String todaysDate = dateFormat.format(date);
        Paragraph dueDateParagraph = new Paragraph("Harus dibayar pada: " + todaysDate, recipientFont);
        document.add(dueDateParagraph);

        document.close();

        return out.toByteArray();
    }



}
