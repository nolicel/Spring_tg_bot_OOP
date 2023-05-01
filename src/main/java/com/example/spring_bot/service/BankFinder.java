package com.example.spring_bot.service;


import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;


public class BankFinder {
    private static final double R = 6371; // Радиус Земли в км
    public static String FindNearestBank(double latitude, double longitude)
    {
        try {
            // Чтение файла с координатами в формате JSON
            JsonReader reader = new JsonReader(new FileReader("coordinates.json"));
            JsonArray points = JsonParser.parseReader(reader).getAsJsonArray();

            double lat = latitude;
            double lon = longitude;
            // Нахождение ближайшей точки
            double minDistance = Double.MAX_VALUE;
            JsonElement nearestPoint = null;
            for (JsonElement point : points) {
                double distance = calculateDistance(lat, lon, point.getAsJsonObject().get("lat").getAsDouble(), point.getAsJsonObject().get("lon").getAsDouble());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestPoint = point;
                }
            }

            String message = "Ближайший к вам банк: "+nearestPoint.getAsJsonObject().get("name").getAsString() +". Расстояние - "+String.format("%.2f", minDistance)+" км!" +
                    nearestPoint.getAsJsonObject().get("lat").getAsString()+"!"+nearestPoint.getAsJsonObject().get("lon").getAsString();
            return message;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "Неудалось найти ближайший к вам банк. Повторите попытку позже";
    }


    // Метод для вычисления расстояния между двумя точками на сфере
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }
}
