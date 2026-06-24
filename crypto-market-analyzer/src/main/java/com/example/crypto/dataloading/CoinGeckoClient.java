package com.example.crypto.dataloading;

import com.example.crypto.model.CoinGeckoMarketPoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CoinGeckoClient {

    private static final String BASE_URL = "https://api.coingecko.com/api/v3";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<CoinGeckoMarketPoint> getMarketChartRange(String coinId, LocalDate from, LocalDate to) {
        try {
            long fromEpochSeconds = from.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
            long toEpochSeconds = to.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC);

            String url = BASE_URL + "/coins/" + coinId + "/market_chart/range"
                    + "?vs_currency=usd"
                    + "&from=" + fromEpochSeconds
                    + "&to=" + toEpochSeconds;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("CoinGecko API error. Status: "
                        + response.statusCode()
                        + ", body: "
                        + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());

            JsonNode prices = root.get("prices");
            JsonNode marketCaps = root.get("market_caps");
            JsonNode totalVolumes = root.get("total_volumes");

            if (prices == null || marketCaps == null || totalVolumes == null) {
                throw new RuntimeException("Invalid CoinGecko response: missing prices, market_caps or total_volumes.");
            }

            int size = Math.min(prices.size(), Math.min(marketCaps.size(), totalVolumes.size()));
            List<CoinGeckoMarketPoint> result = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                long timestampMillis = prices.get(i).get(0).asLong();

                LocalDateTime timestamp = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestampMillis),
                        ZoneOffset.UTC
                );

                BigDecimal closePrice = prices.get(i).get(1).decimalValue();
                BigDecimal marketCap = marketCaps.get(i).get(1).decimalValue();
                BigDecimal volume = totalVolumes.get(i).get(1).decimalValue();

                result.add(new CoinGeckoMarketPoint(timestamp, closePrice, marketCap, volume));
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load data from CoinGecko for coinId: " + coinId, e);
        }
    }
}