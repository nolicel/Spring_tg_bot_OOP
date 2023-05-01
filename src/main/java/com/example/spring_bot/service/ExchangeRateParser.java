package com.example.spring_bot.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ExchangeRateParser {
    private static final String URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";

    public static String getExchangeRates(List<String> currencyCodes) throws IOException {
        OkHttpClient client = new OkHttpClient();
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String date = currentDate.format(formatter);
        String url = URL + "&date=" + date;
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        String json = response.body().string();
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(json).getAsJsonArray();
        StringBuilder sb = new StringBuilder();
        for (String currencyCode : currencyCodes) {
            for (JsonElement element : jsonArray) {
                JsonObject object = element.getAsJsonObject();
                JsonElement cc = object.get("cc");
                if (cc != null && cc.getAsString().equals(currencyCode)) {
                    JsonElement rateElement = object.get("rate");
                    if (rateElement != null) {
                        double rate = rateElement.getAsDouble();
                        sb.append(currencyCode).append(": ").append(rate).append(System.lineSeparator());
                        break;
                    }
                }
            }
        }
        return sb.toString();
    }
}
