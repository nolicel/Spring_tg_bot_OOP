package com.example.spring_bot.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CurrencyCalculator {
   public static String convertCurrency(double amount, String currencyCode) {
       try {
            String exchangeRates = ExchangeRateParser.getExchangeRates(List.of(currencyCode));
           String[] exchangeRatesArr = exchangeRates.split(": ");
           double exchangeRate = Double.parseDouble(exchangeRatesArr[1].trim());
            double convertedAmount = amount / exchangeRate;
           return String.format("%.2f грн = %.2f %s", amount, convertedAmount, currencyCode);
       } catch (IOException e) {
            return "Ошибка при получении курсов валют: " + e.getMessage();
       }
    }
}

