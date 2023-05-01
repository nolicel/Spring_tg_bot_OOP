package com.example.spring_bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "adsTable")
@Getter
@Setter
public class Ads {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    private String ad;
}
