package com.example.crypto.model;

import java.time.LocalDateTime;

public class CryptoAsset {

    private Integer id;
    private String symbol;
    private String name;
    private LocalDateTime addedDate;

    public CryptoAsset() {
    }

    public CryptoAsset(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public CryptoAsset(Integer id, String symbol, String name, LocalDateTime addedDate) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.addedDate = addedDate;
    }

    public Integer getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getAddedDate() {
        return addedDate;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddedDate(LocalDateTime addedDate) {
        this.addedDate = addedDate;
    }

    @Override
    public String toString() {
        return "CryptoAsset{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", addedDate=" + addedDate +
                '}';
    }
}