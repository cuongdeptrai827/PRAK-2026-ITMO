package com.example.crypto.visualization;

import com.example.crypto.model.ChartGenerationResult;
import com.example.crypto.model.MarketQuote;
import com.example.crypto.storage.MarketQuoteRepository;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import com.example.crypto.model.CorrelationResult;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ChartGenerator {

    private final MarketQuoteRepository marketQuoteRepository = new MarketQuoteRepository();

    public ChartGenerationResult generateCharts(
            List<String> assets,
            LocalDate from,
            LocalDate to,
            String outputDirectory
    ) {
        try {
            Files.createDirectories(Path.of(outputDirectory));

            String priceChartPath = generatePriceChart(assets, from, to, outputDirectory);
            String volumeChartPath = generateVolumeChart(assets, from, to, outputDirectory);
            String marketCapChartPath = generateMarketCapChart(assets, from, to, outputDirectory);
            String returnsChartPath = generateReturnsChart(assets, from, to, outputDirectory);

            return new ChartGenerationResult(
                    priceChartPath,
                    volumeChartPath,
                    marketCapChartPath,
                    returnsChartPath,
                    null
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate charts", e);
        }
    }

    private String generatePriceChart(
            List<String> assets,
            LocalDate from,
            LocalDate to,
            String outputDirectory
    ) throws IOException {
        XYChart chart = createBaseChart(
                "Cryptocurrency Price Dynamics",
                "Date",
                "Close price, USD"
        );

        boolean hasData = false;

        for (String asset : assets) {
            String symbol = asset.trim().toUpperCase();

            List<MarketQuote> quotes = marketQuoteRepository.findBestQuotesForPriceAnalysis(symbol, from, to);
            ChartData chartData = extractChartData(quotes, MarketQuote::getClosePrice);

            if (!chartData.dates().isEmpty()) {
                chart.addSeries(symbol, chartData.dates(), chartData.values());
                hasData = true;
            }
        }

        if (!hasData) {
            return null;
        }

        return saveChart(chart, outputDirectory, "price_chart");
    }

    private String generateVolumeChart(
            List<String> assets,
            LocalDate from,
            LocalDate to,
            String outputDirectory
    ) throws IOException {
        XYChart chart = createBaseChart(
                "Trading Volume Dynamics",
                "Date",
                "Volume"
        );

        boolean hasData = false;

        for (String asset : assets) {
            String symbol = asset.trim().toUpperCase();

            List<MarketQuote> quotes = marketQuoteRepository.findBestQuotesForPriceAnalysis(symbol, from, to);
            ChartData chartData = extractChartData(quotes, MarketQuote::getVolume);

            if (!chartData.dates().isEmpty()) {
                chart.addSeries(symbol, chartData.dates(), chartData.values());
                hasData = true;
            }
        }

        if (!hasData) {
            return null;
        }

        return saveChart(chart, outputDirectory, "volume_chart");
    }

    private String generateMarketCapChart(
            List<String> assets,
            LocalDate from,
            LocalDate to,
            String outputDirectory
    ) throws IOException {
        XYChart chart = createBaseChart(
                "Market Capitalization Dynamics",
                "Date",
                "Market capitalization, USD"
        );

        boolean hasData = false;

        for (String asset : assets) {
            String symbol = asset.trim().toUpperCase();

            List<MarketQuote> quotes = marketQuoteRepository.findByAssetSymbolAndSource(
                    symbol,
                    "CoinGecko",
                    from,
                    to
            );

            ChartData chartData = extractChartData(quotes, MarketQuote::getMarketCap);

            if (!chartData.dates().isEmpty()) {
                chart.addSeries(symbol, chartData.dates(), chartData.values());
                hasData = true;
            }
        }

        if (!hasData) {
            return null;
        }

        return saveChart(chart, outputDirectory, "market_cap_chart");
    }

    private String generateReturnsChart(
            List<String> assets,
            LocalDate from,
            LocalDate to,
            String outputDirectory
    ) throws IOException {
        XYChart chart = createBaseChart(
                "Daily Return Dynamics",
                "Date",
                "Daily return, %"
        );

        boolean hasData = false;

        for (String asset : assets) {
            String symbol = asset.trim().toUpperCase();

            List<MarketQuote> quotes = marketQuoteRepository.findBestQuotesForPriceAnalysis(symbol, from, to);
            ChartData chartData = calculateReturnsChartData(quotes);

            if (!chartData.dates().isEmpty()) {
                chart.addSeries(symbol, chartData.dates(), chartData.values());
                hasData = true;
            }
        }

        if (!hasData) {
            return null;
        }

        return saveChart(chart, outputDirectory, "returns_chart");
    }

    private XYChart createBaseChart(String title, String xAxisTitle, String yAxisTitle) {
        XYChart chart = new XYChartBuilder()
                .width(1000)
                .height(600)
                .title(title)
                .xAxisTitle(xAxisTitle)
                .yAxisTitle(yAxisTitle)
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setMarkerSize(0);
        chart.getStyler().setDatePattern("yyyy-MM-dd");
        chart.getStyler().setXAxisLabelRotation(45);

        return chart;
    }

    private ChartData extractChartData(
            List<MarketQuote> quotes,
            Function<MarketQuote, BigDecimal> valueExtractor
    ) {
        List<Date> dates = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        for (MarketQuote quote : quotes) {
            BigDecimal value = valueExtractor.apply(quote);

            if (value == null) {
                continue;
            }

            dates.add(toDate(quote));
            values.add(value.doubleValue());
        }

        return new ChartData(dates, values);
    }

    private ChartData calculateReturnsChartData(List<MarketQuote> quotes) {
        List<Date> dates = new ArrayList<>();
        List<Double> returns = new ArrayList<>();

        for (int i = 1; i < quotes.size(); i++) {
            BigDecimal previousPrice = quotes.get(i - 1).getClosePrice();
            BigDecimal currentPrice = quotes.get(i).getClosePrice();

            if (previousPrice == null || currentPrice == null || previousPrice.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            double dailyReturnPercent = currentPrice.subtract(previousPrice)
                    .divide(previousPrice, 8, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            dates.add(toDate(quotes.get(i)));
            returns.add(dailyReturnPercent);
        }

        return new ChartData(dates, returns);
    }

    private Date toDate(MarketQuote quote) {
        return Date.from(
                quote.getTimestamp()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
    }

    private String saveChart(
            XYChart chart,
            String outputDirectory,
            String fileNameWithoutExtension
    ) throws IOException {
        Path filePathWithoutExtension = Path.of(outputDirectory, fileNameWithoutExtension);

        BitmapEncoder.saveBitmap(
                chart,
                filePathWithoutExtension.toString(),
                BitmapEncoder.BitmapFormat.PNG
        );

        return filePathWithoutExtension + ".png";
    }

    private record ChartData(
            List<Date> dates,
            List<Double> values
    ) {
    }

    public String generateCorrelationHeatmap(
            CorrelationResult correlationResult,
            String outputDirectory
    ) {
        if (correlationResult == null || correlationResult.correlationValue() == null) {
            return null;
        }

        try {
            Files.createDirectories(Path.of(outputDirectory));

            int width = 600;
            int height = 500;
            int cellSize = 140;
            int startX = 180;
            int startY = 120;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();

            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);

            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font("Arial", Font.BOLD, 20));
            graphics.drawString("Correlation heatmap", 190, 50);

            String asset1 = correlationResult.asset1();
            String asset2 = correlationResult.asset2();
            double correlation = correlationResult.correlationValue().doubleValue();

            String[] labels = {asset1, asset2};
            double[][] matrix = {
                    {1.0, correlation},
                    {correlation, 1.0}
            };

            graphics.setFont(new Font("Arial", Font.BOLD, 16));

            for (int i = 0; i < labels.length; i++) {
                graphics.setColor(Color.BLACK);
                graphics.drawString(labels[i], startX + i * cellSize + 45, startY - 20);
                graphics.drawString(labels[i], startX - 80, startY + i * cellSize + 75);
            }

            graphics.setFont(new Font("Arial", Font.BOLD, 18));

            for (int row = 0; row < matrix.length; row++) {
                for (int col = 0; col < matrix[row].length; col++) {
                    double value = matrix[row][col];

                    graphics.setColor(colorForCorrelation(value));
                    graphics.fillRect(startX + col * cellSize, startY + row * cellSize, cellSize, cellSize);

                    graphics.setColor(Color.BLACK);
                    graphics.drawRect(startX + col * cellSize, startY + row * cellSize, cellSize, cellSize);

                    String text = String.format("%.4f", value);
                    graphics.drawString(text, startX + col * cellSize + 40, startY + row * cellSize + 75);
                }
            }

            graphics.setFont(new Font("Arial", Font.PLAIN, 12));
            graphics.setColor(Color.DARK_GRAY);
            graphics.drawString("Color intensity represents the strength and direction of correlation.", 120, 450);

            graphics.dispose();

            Path filePath = Path.of(outputDirectory, "correlation_heatmap.png");
            ImageIO.write(image, "png", filePath.toFile());

            return filePath.toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate correlation heatmap", e);
        }
    }

    private Color colorForCorrelation(double value) {
        value = Math.max(-1.0, Math.min(1.0, value));

        if (value >= 0) {
            int intensity = (int) (255 - value * 120);
            return new Color(intensity, 255, intensity);
        } else {
            int intensity = (int) (255 + value * 120);
            return new Color(255, intensity, intensity);
        }
    }
}