package dev.apollo.artisly.handlers.admin.order;

import dev.apollo.artisly.authentication.AuthHandler;
import dev.apollo.artisly.exceptions.UserNotFoundException;
import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.*;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.security.InputFilter;
import dev.apollo.artisly.security.ParamField;
import dev.apollo.artisly.services.*;
import io.javalin.http.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class Index implements APIHandler {
    @Override
    public void handle(Context context) {
        UserSessionContainer userSessionContainer = AuthHandler.authenticateAdmin(context);
        if (userSessionContainer == null) {
            return;
        }

        InputFilter.validateString("range", ParamField.QUERY, context, new String[]{"all", "today", "last_week", "last_month", "last_year"});

        if (context.attribute("hasErrors") != null) {
            StandarizedResponses.invalidParameter(context);
            return;
        }

        String range = context.queryParam("range");
        try {
            JSONObject statistics = new JSONObject();
            List<Order> orderList = OrderService.getOrdersForAdmin(range);
            JSONArray orderArray = new JSONArray();
            double total_order_value = 0;
            HashMap<UUID, ValuePairContainer> storeSales = new HashMap<>();
            HashMap<UUID, ValuePairContainer> customerSales = new HashMap<>();
            HashSet<UUID> userIds = new HashSet<>();
            HashSet<UUID> storeIds = new HashSet<>();
            int total_order_count = 0;

            for (Order order : orderList) {
                if (!order.orderStatus().equals(OrderStatus.COMPLETED))
                {
                    continue;
                }

                total_order_value += order.price();

                if (storeSales.containsKey(order.storeId()))
                {
                    storeSales.get(order.storeId()).addValue(order.price());
                }
                else
                {
                    ValuePairContainer valuePairContainer = new ValuePairContainer(order.storeId(), order.price());
                    storeSales.put(order.storeId(), valuePairContainer);
                }

                if (customerSales.containsKey(order.userId()))
                {
                    customerSales.get(order.userId()).addValue(order.price());
                }
                else
                {
                    ValuePairContainer valuePairContainer = new ValuePairContainer(order.userId(), order.price());
                    customerSales.put(order.userId(), valuePairContainer);
                }


                userIds.add(order.userId());
                storeIds.add(order.storeId());
                total_order_count++;
            }

            List<ValuePairContainer> storeSalesArray = new ArrayList<>(storeSales.values());
            List<ValuePairContainer> customerSalesArray = new ArrayList<>(customerSales.values());

            CountDownLatch latch = new CountDownLatch(2);
            CompletableFuture.runAsync(() -> {
                storeSalesArray.sort((o1, o2) -> {
                    if (o1.value() > o2.value())
                    {
                        return 1;
                    }
                    else if (o1.value() < o2.value())
                    {
                        return -1;
                    }
                    else
                    {
                        return 0;
                    }
                });
                latch.countDown();
            });

            CompletableFuture.runAsync(() -> {
                customerSalesArray.sort((o1, o2) -> {
                    if (o1.value() > o2.value())
                    {
                        return 1;
                    }
                    else if (o1.value() < o2.value())
                    {
                        return -1;
                    }
                    else
                    {
                        return 0;
                    }
                });
                latch.countDown();
            });

            try
            {
                latch.await();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }


            JSONArray storeSalesJsonArray = new JSONArray();
            JSONArray customerSalesJsonArray = new JSONArray();
            LinkedList<ValuePairContainer> top5StoreSales = new LinkedList<>();
            LinkedList<ValuePairContainer> top5CustomerSales = new LinkedList<>();
            for (int i = 0; i < 5; i++)
            {
                if (i >= storeSalesArray.size())
                {
                    break;
                }
                ValuePairContainer valuePairContainer = storeSalesArray.get(i);
                top5StoreSales.addFirst(valuePairContainer);
            }

            for (int i = 0; i < 5; i++)
            {
                if (i >= customerSalesArray.size())
                {
                    break;
                }
                ValuePairContainer valuePairContainer = customerSalesArray.get(i);
                top5CustomerSales.addFirst(valuePairContainer);
            }

            for (ValuePairContainer valuePairContainer : top5StoreSales)
            {
                JSONObject jsonObject = new JSONObject();
                Optional<Store> storeOptional = StoreService.getStoreById(valuePairContainer.id());
                if (!storeOptional.isPresent())
                {
                    continue;
                }
                jsonObject.put("store", storeOptional.get().toJSON());
                jsonObject.put("total_sales", valuePairContainer.value());
                storeSalesJsonArray.add(jsonObject);
            }

            for (ValuePairContainer valuePairContainer : top5CustomerSales)
            {
                JSONObject jsonObject = new JSONObject();
                User user;
                try
                {
                    user = UserService.show(valuePairContainer.id());
                }
                catch (UserNotFoundException e)
                {
                    e.printStackTrace();
                    continue;
                }

                jsonObject.put("user", user.toJSON());
                jsonObject.put("total_sales", valuePairContainer.value());
                customerSalesJsonArray.add(jsonObject);
            }

            statistics.put("total_order_value", total_order_value);
            statistics.put("total_order_count", orderList.size());
            statistics.put("total_user_count", userIds.size());
            statistics.put("total_store_count", storeIds.size());
            statistics.put("total_order_count", total_order_count);
            statistics.put("top_5_store_sales", storeSalesJsonArray);
            statistics.put("top_5_customer_sales", customerSalesJsonArray);

            Collections.reverse(orderList);
            for (Order order : orderList)
            {
                JSONObject orderEntry = new JSONObject();
                Optional<Store> storeOptional = StoreService.getStoreById(order.storeId());
                Optional<ImmutableAddress> optionalImmutableAddress = AddressService.readImmutableAddress(order.immuteableAddressId());
                Optional<ImmutableProduct> optionalImmutableProduct = ProductService.readImmutableProduct(order.immuteableProductId());
                User buyer;
                try
                {
                     buyer = UserService.show(order.userId());
                }
                catch (UserNotFoundException e)
                {
                    continue;
                }

                orderEntry.put("order", order.toJSON());
                orderEntry.put("buyer", buyer.toJSON());
                orderEntry.put("store", storeOptional.get().toJSON());
                orderEntry.put("immutable_address", optionalImmutableAddress.get().toJSON());
                orderEntry.put("immutable_product", optionalImmutableProduct.get().toJSON());

                orderArray.add(orderEntry);
            }

            JSONObject response = new JSONObject();
            response.put("statistics", statistics);
            response.put("orders", orderArray);

            StandarizedResponses.success(context, "SUCCESS", "Berhasil mengambil data statistik", "response", response);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            StandarizedResponses.generalFailure(context, 500, "SQL_EXCEPTION", "Kesalahan saat mengambil data kategori");
        }

    }
}
