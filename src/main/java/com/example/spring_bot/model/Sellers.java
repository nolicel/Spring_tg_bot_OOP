package com.example.spring_bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
enum RatesCodes {
    USD,
    EUR,
    JPY
}
@Entity(name = "SellersDataTable")
@Getter
@Setter
public class Sellers
{
    @Id
    private Long chatId;

    private double usdRate;
    private double eurRate;
    private double jpyRate;

    public double getRate(String currency)
    {
        RatesCodes currencyCode = RatesCodes.valueOf(currency);
        int index = currencyCode.ordinal();
        switch (index)
        {
            case 0:
                return usdRate;

            case 1:
                return eurRate;

            case 2:
                return jpyRate;

            default:
                return 0.0;

        }
    }

    public void setRate(String currency,double value)
    {
        RatesCodes currencyCode = RatesCodes.valueOf(currency);
        int index = currencyCode.ordinal();
        switch (index)
        {
            case 0:
                usdRate=value;
                break;
            case 1:
                eurRate=value;
                break;
            case 2:
                jpyRate=value;
                break;
            default:
                break;

        }
    }
}
