package com.example.crypto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MarketQuote {

    private Integer id;
    private Integer assetId;
    private Integer exchangeId;
    private Integer tradingPairId;
    private LocalDateTime timestamp;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal volume;
    private BigDecimal marketCap;
    private String source;

    public MarketQuote() {
    }

    public MarketQuote(
            Integer assetId,
            Integer exchangeId,
            Integer tradingPairId,
            LocalDateTime timestamp,
            BigDecimal openPrice,
            BigDecimal closePrice,
            BigDecimal highPrice,
            BigDecimal lowPrice,
            BigDecimal volume,
            BigDecimal marketCap,
            String source
    ) {
        this.assetId = assetId;
        this.exchangeId = exchangeId;
        this.tradingPairId = tradingPairId;
        this.timestamp = timestamp;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.volume = volume;
        this.marketCap = marketCap;
        this.source = source;
    }

    public Integer getId() {
        return id;
    }

    public Integer getAssetId() {
        return assetId;
    }

    public Integer getExchangeId() {
        return exchangeId;
    }

    public Integer getTradingPairId() {
        return tradingPairId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }

    public String getSource() {
        return source;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setAssetId(Integer assetId) {
        this.assetId = assetId;
    }

    public void setExchangeId(Integer exchangeId) {
        this.exchangeId = exchangeId;
    }

    public void setTradingPairId(Integer tradingPairId) {
        this.tradingPairId = tradingPairId;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }

    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }

    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }

    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public void setMarketCap(BigDecimal marketCap) {
        this.marketCap = marketCap;
    }

    public void setSource(String source) {
        this.source = source;
    }
}