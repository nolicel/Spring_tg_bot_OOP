package com.example.spring_bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

enum CurrencyCodes {
    UAH,
    USD,
    EUR,
    JPY
}

@Entity(name = "walletsTable")
@Getter
@Setter
public class Wallet {
    @Id
    private Long Id;

    private double uahBalance;
    private double usdBalance;
    private double eurBalance;
    private double jpyBalance;

    public void AddMoney(String currency, double value)
    {
        CurrencyCodes currencyCode = CurrencyCodes.valueOf(currency);
        int index = currencyCode.ordinal();
        switch (index)
        {
            case 0:
                uahBalance += value;
                break;
            case 1:
                usdBalance+=value;
                break;
            case 2:
                eurBalance+=value;
                break;
            case 3:
                jpyBalance+=value;
                break;
            default:
                break;
        }
    }
    public String WithdrawMoney(String currency, double value)
    {
        CurrencyCodes currencyCode = CurrencyCodes.valueOf(currency);
        int index = currencyCode.ordinal();
        switch (index)
        {
            case 0:
                if(value<=uahBalance)
                {
                    uahBalance -= value;
                    return "Гроші успішно знято";
                }
                else {
                    return "Недостатньо грошей на рахунку";
                }
            case 1:
                if(value<=usdBalance)
                {
                    usdBalance -= value;
                    return "Гроші успішно знято";
                }
                else {
                    return "Недостатньо грошей на рахунку";
                }
            case 2:
                if(value<=eurBalance)
                {
                    eurBalance -= value;
                    return "Гроші успішно знято";
                }
                else {
                    return "Недостатньо грошей на рахунку";
                }
            case 3:
                if(value<=jpyBalance)
                {
                    jpyBalance -= value;
                    return "Гроші успішно знято";
                }
                else {
                    return "Недостатньо грошей на рахунку";
                }
            default:
                return "Невідомий код валюти";
        }
    }

    public String GetBalance()
    {
        return "Ваш баланс:\n" +
                "UAH: "+uahBalance+"\n" +
                "USD: "+usdBalance+"\n" +
                "EUR: "+eurBalance+"\n" +
                "JPY: "+jpyBalance+"\n";
    }
}