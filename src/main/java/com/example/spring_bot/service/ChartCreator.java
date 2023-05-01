package com.example.spring_bot.service;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ChartCreator {
    private static final OkHttpClient client = new OkHttpClient();
    static LocalDate today = LocalDate.now();
    static LocalDate startdate = today.minusDays(30);
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    static String todatestr = today.format(formatter);
    static String startdaystr = startdate.format(formatter);
    static String API_URL = "https://bank.gov.ua/NBU_Exchange/exchange_site?start=" + startdaystr + "&end=" + todatestr + "&valcode=jpy&sort=exchangedate&order=desc&json";

    public void GenerateChart(String currency) throws IOException, JSONException {
        API_URL="https://bank.gov.ua/NBU_Exchange/exchange_site?start=" + startdaystr + "&end=" + todatestr + "&valcode="+currency+"&sort=exchangedate&order=desc&json";
        String responseBody = fetchExchangeRates(API_URL);
        Map<LocalDate, Double> dailyRates = parseExchangeRates(responseBody);
        DefaultCategoryDataset dataset = createDataset(dailyRates);
        createGraph(dataset);
    }

    static String fetchExchangeRates(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    static Map<LocalDate, Double> parseExchangeRates(String responseBody) throws JSONException {
        JSONArray jsonArray = new JSONArray(responseBody);
        Map<LocalDate, Double> dailyRates = new HashMap<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String dateStr = jsonObject.getString("exchangedate");
            double rate = jsonObject.getDouble("rate");
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            dailyRates.put(date, rate);
        }
        return dailyRates;
    }

    public static DefaultCategoryDataset createDataset(Map<LocalDate, Double> dailyRates) {
        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = currentDate.minusMonths(1).withDayOfMonth(1);
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        LocalDate date = startDate;
        while (date.isBefore(currentDate) || date.isEqual(currentDate)) {
            Double rate = dailyRates.get(date);
            if (rate != null) {
                dataset.addValue(rate, "JPY/UAH", labelFormatter.format(date));
            }
            date = date.plusDays(1);
        }
        return dataset;
    }

    public static void createGraph(DefaultCategoryDataset dataset) throws IOException {
        JFreeChart chart = ChartFactory.createLineChart(
                "JPY/UAH Exchange Rates",
                "Date",
                "Exchange Rate for 10 Yen",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false

        );
        CategoryAxis xAxis = chart.getCategoryPlot().getDomainAxis();
        CategoryLabelPositions positions = CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0);
        xAxis.setCategoryLabelPositions(positions);


        ValueAxis yAxis = chart.getCategoryPlot().getRangeAxis();
        yAxis.setRange(2.5, yAxis.getUpperBound());
        if (yAxis instanceof NumberAxis) {
            ((NumberAxis) yAxis).setTickUnit(new NumberTickUnit(0.20));
        }
        ChartUtils.saveChartAsPNG(new File("chart.png"), chart, 800, 600);
    }
}