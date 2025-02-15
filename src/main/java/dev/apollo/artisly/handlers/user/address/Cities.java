package dev.apollo.artisly.handlers.user.address;

import dev.apollo.artisly.handlers.APIHandler;
import dev.apollo.artisly.models.IndonesianCity;
import dev.apollo.artisly.response.StandarizedResponses;
import dev.apollo.artisly.services.AddressService;
import io.javalin.http.Context;
import org.json.simple.JSONArray;

import java.util.List;

public class Cities implements APIHandler {
    @Override
    public void handle(Context context) {
        List<String> cities = AddressService.getCityList();

        JSONArray city = new JSONArray();
        city.addAll(cities);

        StandarizedResponses.success(context, "SUCCESS", "Successfully retrieved cities", "cities", city);



    }
}
