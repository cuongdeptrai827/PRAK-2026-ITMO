package com.example.crypto.dataloading;

import com.example.crypto.model.BinanceKlinePoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class BinanceClient {

    private static final String BASE_URL = "https://api.binance.com";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<BinanceKlinePoint> getDailyKlines(String symbol, LocalDate from, LocalDate to) {
        try {
            long startTimeMillis = from.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
            long endTimeMillis = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();

            String url = BASE_URL + "/api/v3/klines"
                    + "?symbol=" + symbol
                    + "&interval=1d"
                    + "&startTime=" + startTimeMillis
                    + "&endTime=" + endTimeMillis
                    + "&limit=1000";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Binance API error. Status: "
                        + response.statusCode()
                        + ", body: "
                        + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            List<BinanceKlinePoint> result = new ArrayList<>();

            for (JsonNode item : root) {
                long openTimeMillis = item.get(0).asLong();

                LocalDateTime timestamp = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(openTimeMillis),
                        ZoneOffset.UTC
                );

                BigDecimal openPrice = new BigDecimal(item.get(1).asText());
                BigDecimal highPrice = new BigDecimal(item.get(2).asText());
                BigDecimal lowPrice = new BigDecimal(item.get(3).asText());
                BigDecimal closePrice = new BigDecimal(item.get(4).asText());
                BigDecimal volume = new BigDecimal(item.get(5).asText());

                result.add(new BinanceKlinePoint(
                        timestamp,
                        openPrice,
                        highPrice,
                        lowPrice,
                        closePrice,
                        volume
                ));
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load data from Binance for symbol: " + symbol, e);
        }
    }
}