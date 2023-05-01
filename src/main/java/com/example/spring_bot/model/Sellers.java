package com.example.spring_bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity(name = "SellersDataTable")
@Getter
@Setter
public class Sellers
{
    @Id
    private Long chatId;

    private double rate;

    private String currency;
}
