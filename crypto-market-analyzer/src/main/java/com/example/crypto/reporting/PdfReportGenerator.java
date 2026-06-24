package com.example.crypto.reporting;

import com.example.crypto.model.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.example.crypto.model.LeaderRankingResult;
import com.example.crypto.model.LatestQuoteResult;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class PdfReportGenerator {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

    public void generateReport(
            List<String> assets,
            LocalDate from,
            LocalDate to,
            String source,
            String reportType,
            String outputFile,
            List<LatestQuoteResult> latestQuotes,
            List<AnalysisResult> analysisResults,
            List<VolatilityResult> volatilityResults,
            CorrelationResult correlationResult,
            CorrelationResult spearmanCorrelationResult,
            List<LeaderRankingResult> leaderRankings,
            List<ForecastResult> forecasts,
            ChartGenerationResult chartGenerationResult,
            List<AnomalyEvent> anomalies
    ) {
        try {
            createParentDirectoryIfNeeded(outputFile);

            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, new FileOutputStream(outputFile));

            document.open();

            addTitle(document, reportType);
            addInputParameters(document, assets, from, to, source, reportType);

            addLatestQuoteSection(document, latestQuotes);
            addAnalysisSection(document, analysisResults);
            addVolatilitySection(document, volatilityResults);
            addLeaderRankingSection(document, leaderRankings);
            addCorrelationSection(document, correlationResult, spearmanCorrelationResult);
            addForecastSection(document, forecasts, reportType);
            addAnomalySection(document, anomalies, reportType);
            addChartsSection(document, chartGenerationResult);
            addConclusion(document, reportType, analysisResults, volatilityResults, correlationResult, forecasts, anomalies);

            document.close();

        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to generate PDF report: " + outputFile, e);
        }
    }

    private void addLeaderRankingSection(
            Document document,
            List<LeaderRankingResult> leaderRankings
    ) throws DocumentException {
        if (leaderRankings == null || leaderRankings.isEmpty()) {
            return;
        }

        addSectionTitle(document, "Market leaders ranking");

        addRankingTable(document, leaderRankings, "MARKET_CAP", "Ranking by market capitalization");
        addRankingTable(document, leaderRankings, "TRADING_VOLUME", "Ranking by trading volume");
        addRankingTable(document, leaderRankings, "RETURN", "Ranking by return");

        addEmptyLine(document);
    }

    private void addRankingTable(
            Document document,
            List<LeaderRankingResult> leaderRankings,
            String metricType,
            String title
    ) throws DocumentException {
        List<LeaderRankingResult> filteredRankings = leaderRankings.stream()
                .filter(result -> metricType.equals(result.metricType()))
                .toList();

        if (filteredRankings.isEmpty()) {
            return;
        }

        Paragraph tableTitle = new Paragraph(title, BOLD_FONT);
        tableTitle.setSpacingBefore(8);
        tableTitle.setSpacingAfter(6);
        document.add(tableTitle);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(80);

        try {
            table.setWidths(new float[]{15, 25, 40});
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to set ranking table widths", e);
        }

        addHeaderCell(table, "Rank");
        addHeaderCell(table, "Asset");
        addHeaderCell(table, "Value");

        for (LeaderRankingResult ranking : filteredRankings) {
            addValueCell(table, String.valueOf(ranking.rank()));
            addValueCell(table, ranking.symbol());
            addValueCell(table, formatRankingValue(ranking));
        }

        document.add(table);
    }

    private String formatRankingValue(LeaderRankingResult ranking) {
        if (ranking == null || ranking.value() == null) {
            return "N/A";
        }

        return switch (ranking.metricType()) {
            case "MARKET_CAP" -> formatMoney(ranking.value());
            case "TRADING_VOLUME" -> formatDecimal(ranking.value());
            case "RETURN" -> formatPercent(ranking.value());
            default -> formatDecimal(ranking.value());
        };
    }

    private void createParentDirectoryIfNeeded(String outputFile) throws IOException {
        Path outputPath = Path.of(outputFile);
        Path parent = outputPath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private void addTitle(Document document, String reportType) throws DocumentException {
        Paragraph title = new Paragraph("Cryptocurrency Market Analysis Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(8);
        document.add(title);

        Paragraph subtitle = new Paragraph("Report type: " + reportType, NORMAL_FONT);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);
    }

    private void addInputParameters(
            Document document,
            List<String> assets,
            LocalDate from,
            LocalDate to,
            String source,
            String reportType
    ) throws DocumentException {
        addSectionTitle(document, "Input parameters");

        PdfPTable table = createTwoColumnTable();

        addRow(table, "Assets", String.join(", ", assets));
        addRow(table, "Date from", from.toString());
        addRow(table, "Date to", to.toString());
        addRow(table, "Data source", source);
        addRow(table, "Report type", reportType);

        document.add(table);
        addEmptyLine(document);
    }

    private void addAnalysisSection(
            Document document,
            List<AnalysisResult> analysisResults
    ) throws DocumentException {
        if (analysisResults == null || analysisResults.isEmpty()) {
            return;
        }

        addSectionTitle(document, "Basic analytical results");

        for (AnalysisResult result : analysisResults) {
            Paragraph assetTitle = new Paragraph("Asset: " + result.symbol(), BOLD_FONT);
            assetTitle.setSpacingBefore(8);
            assetTitle.setSpacingAfter(6);
            document.add(assetTitle);

            PdfPTable table = createTwoColumnTable();

            addRow(table, "Source", result.source());
            addRow(table, "Records count", String.valueOf(result.recordsCount()));

            addRow(table, "Minimum price", formatMoney(result.minPrice()));
            addRow(table, "Maximum price", formatMoney(result.maxPrice()));
            addRow(table, "Average price", formatMoney(result.averagePrice()));
            addRow(table, "Median price", formatMoney(result.medianPrice()));
            addRow(table, "Standard deviation", formatDecimal(result.standardDeviation()));

            addRow(table, "First price", formatMoney(result.firstPrice()));
            addRow(table, "Last price", formatMoney(result.lastPrice()));
            addRow(table, "Price change", formatMoney(result.priceChange()));
            addRow(table, "Price change, %", formatPercent(result.priceChangePercent()));

            addRow(table, "Average volume", formatDecimal(result.averageVolume()));
            addRow(table, "Volume change", formatDecimal(result.volumeChange()));
            addRow(table, "Volume change, %", formatPercent(result.volumeChangePercent()));

            addRow(table, "First market cap", formatMoney(result.firstMarketCap()));
            addRow(table, "Last market cap", formatMoney(result.lastMarketCap()));
            addRow(table, "Market cap change", formatMoney(result.marketCapChange()));
            addRow(table, "Market cap change, %", formatPercent(result.marketCapChangePercent()));

            document.add(table);
        }

        addEmptyLine(document);
    }

    private void addVolatilitySection(
            Document document,
            List<VolatilityResult> volatilityResults
    ) throws DocumentException {
        if (volatilityResults == null || volatilityResults.isEmpty()) {
            return;
        }

        addSectionTitle(document, "Volatility analysis");

        for (VolatilityResult result : volatilityResults) {
            Paragraph assetTitle = new Paragraph("Asset: " + result.symbol(), BOLD_FONT);
            assetTitle.setSpacingBefore(8);
            assetTitle.setSpacingAfter(6);
            document.add(assetTitle);

            PdfPTable table = createTwoColumnTable();

            addRow(table, "Daily volatility", formatRatioAsPercent(result.dailyVolatility()));
            addRow(table, "Weekly volatility", formatRatioAsPercent(result.weeklyVolatility()));
            addRow(table, "Monthly volatility", formatRatioAsPercent(result.monthlyVolatility()));
            addRow(table, "RMSD return", formatRatioAsPercent(result.rmsdReturn()));

            document.add(table);
        }

        addEmptyLine(document);
    }

    private void addCorrelationSection(
            Document document,
            CorrelationResult pearsonCorrelationResult,
            CorrelationResult spearmanCorrelationResult
    ) throws DocumentException {
        if (pearsonCorrelationResult == null && spearmanCorrelationResult == null) {
            return;
        }

        addSectionTitle(document, "Correlation report");

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        try {
            table.setWidths(new float[]{20, 20, 20, 20, 20});
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to set correlation table widths", e);
        }

        addHeaderCell(table, "Assets");
        addHeaderCell(table, "Method");
        addHeaderCell(table, "Correlation value");
        addHeaderCell(table, "Data points");
        addHeaderCell(table, "Interpretation");

        addCorrelationRow(table, pearsonCorrelationResult);
        addCorrelationRow(table, spearmanCorrelationResult);

        document.add(table);
        addEmptyLine(document);

        if (pearsonCorrelationResult != null) {
            addCorrelationMatrix(document, pearsonCorrelationResult);
            addRelationshipGraphDescription(document, pearsonCorrelationResult);
        }
    }

    private void addCorrelationRow(
            PdfPTable table,
            CorrelationResult result
    ) {
        if (result == null) {
            return;
        }

        addValueCell(table, result.asset1() + " / " + result.asset2());
        addValueCell(table, result.method());
        addValueCell(table, formatDecimal(result.correlationValue()));
        addValueCell(table, String.valueOf(result.dataPoints()));
        addValueCell(table, interpretCorrelation(result.correlationValue()));
    }

    private void addCorrelationMatrix(
            Document document,
            CorrelationResult correlationResult
    ) throws DocumentException {
        Paragraph title = new Paragraph("Correlation matrix", BOLD_FONT);
        title.setSpacingBefore(8);
        title.setSpacingAfter(6);
        document.add(title);

        PdfPTable matrix = new PdfPTable(3);
        matrix.setWidthPercentage(70);

        addHeaderCell(matrix, "");
        addHeaderCell(matrix, correlationResult.asset1());
        addHeaderCell(matrix, correlationResult.asset2());

        addHeaderCell(matrix, correlationResult.asset1());
        addValueCell(matrix, "1.0000");
        addValueCell(matrix, formatDecimal(correlationResult.correlationValue()));

        addHeaderCell(matrix, correlationResult.asset2());
        addValueCell(matrix, formatDecimal(correlationResult.correlationValue()));
        addValueCell(matrix, "1.0000");

        document.add(matrix);
        addEmptyLine(document);
    }

    private void addRelationshipGraphDescription(
            Document document,
            CorrelationResult correlationResult
    ) throws DocumentException {
        Paragraph title = new Paragraph("Relationship graph", BOLD_FONT);
        title.setSpacingBefore(8);
        title.setSpacingAfter(6);
        document.add(title);

        Paragraph graph = new Paragraph(
                correlationResult.asset1()
                        + "  -- "
                        + formatDecimal(correlationResult.correlationValue())
                        + " -->  "
                        + correlationResult.asset2()
                        + "\nRelationship: "
                        + interpretCorrelation(correlationResult.correlationValue()),
                NORMAL_FONT
        );

        graph.setSpacingAfter(10);
        document.add(graph);
    }

    private void addForecastSection(
            Document document,
            List<ForecastResult> forecasts,
            String reportType
    ) throws DocumentException {
        boolean isForecastReport = "forecast".equalsIgnoreCase(reportType);

        if ((forecasts == null || forecasts.isEmpty()) && !isForecastReport) {
            return;
        }

        addSectionTitle(document, "Forecast report");

        Paragraph note = new Paragraph(
                "Forecast values are calculated using a simple linear regression model. "
                        + "These values are model estimates for educational analysis and should not be interpreted as financial advice.",
                NORMAL_FONT
        );
        note.setSpacingAfter(8);
        document.add(note);

        if (forecasts == null || forecasts.isEmpty()) {
            Paragraph paragraph = new Paragraph("No forecast results generated.", NORMAL_FONT);
            paragraph.setSpacingAfter(10);
            document.add(paragraph);
            return;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        try {
            table.setWidths(new float[]{12, 14, 18, 22, 20, 14});
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to set forecast table widths", e);
        }

        addHeaderCell(table, "Asset");
        addHeaderCell(table, "Target");
        addHeaderCell(table, "Forecast date");
        addHeaderCell(table, "Predicted value");
        addHeaderCell(table, "Model");
        addHeaderCell(table, "Training points");

        int maxRows = Math.min(forecasts.size(), 40);

        for (int i = 0; i < maxRows; i++) {
            ForecastResult forecast = forecasts.get(i);

            addValueCell(table, forecast.symbol());
            addValueCell(table, forecast.targetType());
            addValueCell(table, forecast.forecastDate().toString());
            addValueCell(table, formatForecastValue(forecast));
            addValueCell(table, forecast.modelName());
            addValueCell(table, String.valueOf(forecast.trainingPoints()));
        }

        document.add(table);

        if (forecasts.size() > maxRows) {
            Paragraph paragraph = new Paragraph(
                    "Only first " + maxRows + " forecast rows are displayed in the report.",
                    NORMAL_FONT
            );
            paragraph.setSpacingBefore(6);
            document.add(paragraph);
        }

        addEmptyLine(document);
    }

    private void addAnomalySection(
            Document document,
            List<AnomalyEvent> anomalies,
            String reportType
    ) throws DocumentException {
        boolean isAnomalyReport = "anomalies".equalsIgnoreCase(reportType);

        if ((anomalies == null || anomalies.isEmpty()) && !isAnomalyReport) {
            return;
        }

        addSectionTitle(document, "Anomaly report");

        if (anomalies == null || anomalies.isEmpty()) {
            Paragraph paragraph = new Paragraph("No anomalies detected for the selected period.", NORMAL_FONT);
            paragraph.setSpacingAfter(10);
            document.add(paragraph);
            return;
        }

        Paragraph countParagraph = new Paragraph("Detected anomalies: " + anomalies.size(), NORMAL_FONT);
        countParagraph.setSpacingAfter(8);
        document.add(countParagraph);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        try {
            table.setWidths(new float[]{12, 22, 22, 16, 12, 36});
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to set anomaly table widths", e);
        }

        addHeaderCell(table, "Asset");
        addHeaderCell(table, "Type");
        addHeaderCell(table, "Timestamp");
        addHeaderCell(table, "Value");
        addHeaderCell(table, "Severity");
        addHeaderCell(table, "Description");

        int maxRows = Math.min(anomalies.size(), 20);

        for (int i = 0; i < maxRows; i++) {
            AnomalyEvent anomaly = anomalies.get(i);

            addValueCell(table, anomaly.symbol());
            addValueCell(table, anomaly.eventType());
            addValueCell(table, anomaly.timestamp().toString());
            addValueCell(table, formatDecimal(anomaly.value()));
            addValueCell(table, anomaly.severity());
            addValueCell(table, anomaly.description());
        }

        document.add(table);

        if (anomalies.size() > maxRows) {
            Paragraph note = new Paragraph(
                    "Only first " + maxRows + " anomalies are displayed in the report.",
                    NORMAL_FONT
            );
            note.setSpacingBefore(6);
            document.add(note);
        }

        addEmptyLine(document);
    }

    private void addChartsSection(
            Document document,
            ChartGenerationResult chartGenerationResult
    ) throws DocumentException, IOException {
        if (chartGenerationResult == null) {
            return;
        }

        document.newPage();
        addSectionTitle(document, "Visualization");

        addChart(document, "Price dynamics", chartGenerationResult.priceChartPath());
        addChart(document, "Trading volume dynamics", chartGenerationResult.volumeChartPath());
        addChart(document, "Market capitalization dynamics", chartGenerationResult.marketCapChartPath());
        addChart(document, "Daily return dynamics", chartGenerationResult.returnsChartPath());
        addChart(document, "Correlation heatmap", chartGenerationResult.correlationHeatmapPath());
    }

    private void addChart(
            Document document,
            String title,
            String chartPath
    ) throws DocumentException, IOException {
        if (chartPath == null || !Files.exists(Path.of(chartPath))) {
            return;
        }

        Paragraph chartTitle = new Paragraph(title, BOLD_FONT);
        chartTitle.setSpacingBefore(12);
        chartTitle.setSpacingAfter(6);
        document.add(chartTitle);

        Image image = Image.getInstance(chartPath);
        image.scaleToFit(500, 280);
        image.setAlignment(Element.ALIGN_CENTER);
        document.add(image);
    }

    private void addConclusion(
            Document document,
            String reportType,
            List<AnalysisResult> analysisResults,
            List<VolatilityResult> volatilityResults,
            CorrelationResult correlationResult,
            List<ForecastResult> forecasts,
            List<AnomalyEvent> anomalies
    ) throws DocumentException {
        document.newPage();
        addSectionTitle(document, "Conclusion");

        Paragraph paragraph = new Paragraph("", NORMAL_FONT);
        paragraph.setSpacingAfter(10);

        paragraph.add("The report was generated by the cryptocurrency market analysis system. ");

        if ("asset".equalsIgnoreCase(reportType)) {
            paragraph.add("The asset report contains historical price dynamics, trading volume, volatility and return indicators for the selected cryptocurrency. ");
        } else if ("comparison".equalsIgnoreCase(reportType)) {
            paragraph.add("The comparison report contains analytical indicators and visual comparison of selected cryptocurrencies, including price, volume and market capitalization dynamics. ");
        } else if ("correlation".equalsIgnoreCase(reportType)) {
            paragraph.add("The correlation report evaluates the relationship between selected assets using the Pearson correlation coefficient. ");
        } else if ("anomalies".equalsIgnoreCase(reportType)) {
            paragraph.add("The anomaly report lists detected market events, including sharp price changes, abnormal trading volumes and unusual volatility. ");
        } else if ("forecast".equalsIgnoreCase(reportType)) {
            paragraph.add("The forecast report presents model-based predictions for future price and trading volume using linear regression. ");
        }

        if (correlationResult != null) {
            paragraph.add("The Pearson correlation coefficient for ");
            paragraph.add(correlationResult.asset1() + " and " + correlationResult.asset2());
            paragraph.add(" is ");
            paragraph.add(formatDecimal(correlationResult.correlationValue()));
            paragraph.add(", which can be interpreted as ");
            paragraph.add(interpretCorrelation(correlationResult.correlationValue()));
            paragraph.add(". ");
        }

        if (analysisResults != null && !analysisResults.isEmpty()) {
            paragraph.add("The calculated analytical indicators can be used to compare the behavior of selected crypto assets over the analyzed period. ");
        }

        if (volatilityResults != null && !volatilityResults.isEmpty()) {
            paragraph.add("Volatility indicators describe the degree of price variability for the selected assets. ");
        }

        if (forecasts != null && !forecasts.isEmpty()) {
            paragraph.add("Forecast results are approximate model estimates and should be used only for educational and analytical purposes. ");
        }

        if (anomalies != null && !anomalies.isEmpty()) {
            paragraph.add("Detected anomalies highlight unusual market behavior that may require additional analysis. ");
        }

        document.add(paragraph);
    }

    private void addSectionTitle(Document document, String title) throws DocumentException {
        Paragraph section = new Paragraph(title, SECTION_FONT);
        section.setSpacingBefore(10);
        section.setSpacingAfter(8);
        document.add(section);
    }

    private void addEmptyLine(Document document) throws DocumentException {
        Paragraph emptyLine = new Paragraph(" ");
        emptyLine.setSpacingAfter(6);
        document.add(emptyLine);
    }

    private PdfPTable createTwoColumnTable() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        try {
            table.setWidths(new float[]{35, 65});
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to set table widths", e);
        }

        return table;
    }

    private void addRow(PdfPTable table, String key, String value) {
        PdfPCell keyCell = new PdfPCell(new Phrase(key, BOLD_FONT));
        keyCell.setBackgroundColor(new Color(230, 230, 230));
        keyCell.setPadding(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value == null ? "N/A" : value, NORMAL_FONT));
        valueCell.setPadding(5);

        table.addCell(keyCell);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value, BOLD_FONT));
        cell.setBackgroundColor(new Color(200, 200, 200));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addValueCell(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "N/A" : value, NORMAL_FONT));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "N/A";
        }

        return formatDecimal(value) + " USD";
    }

    private String formatPercent(BigDecimal value) {
        if (value == null) {
            return "N/A";
        }

        return value.setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString() + "%";
    }

    private String formatRatioAsPercent(BigDecimal value) {
        if (value == null) {
            return "N/A";
        }

        return value.multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString() + "%";
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "N/A";
        }

        return value.setScale(4, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private String formatForecastValue(ForecastResult forecast) {
        if (forecast == null || forecast.predictedValue() == null) {
            return "N/A";
        }

        if ("PRICE".equalsIgnoreCase(forecast.targetType())) {
            return formatMoney(forecast.predictedValue());
        }

        return formatDecimal(forecast.predictedValue());
    }

    private String interpretCorrelation(BigDecimal value) {
        if (value == null) {
            return "N/A";
        }

        double correlation = value.doubleValue();

        if (correlation > 0.7) {
            return "strong positive relationship";
        } else if (correlation > 0.3) {
            return "moderate positive relationship";
        } else if (correlation > -0.3) {
            return "weak or almost no relationship";
        } else if (correlation > -0.7) {
            return "moderate negative relationship";
        } else {
            return "strong negative relationship";
        }
    }

    private void addLatestQuoteSection(
            Document document,
            List<LatestQuoteResult> latestQuotes
    ) throws DocumentException {
        if (latestQuotes == null || latestQuotes.isEmpty()) {
            return;
        }

        addSectionTitle(document, "Latest available quotes");

        Paragraph note = new Paragraph(
                "The latest quote is represented as the most recent available quote in the selected period.",
                NORMAL_FONT
        );
        note.setSpacingAfter(8);
        document.add(note);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        try {
            table.setWidths(new float[]{12, 22, 14, 18, 18, 22});
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to set latest quote table widths", e);
        }

        addHeaderCell(table, "Asset");
        addHeaderCell(table, "Timestamp");
        addHeaderCell(table, "Source");
        addHeaderCell(table, "Close price");
        addHeaderCell(table, "Volume");
        addHeaderCell(table, "Market cap");

        for (LatestQuoteResult quote : latestQuotes) {
            addValueCell(table, quote.symbol());
            addValueCell(table, quote.timestamp().toString());
            addValueCell(table, quote.source());
            addValueCell(table, formatMoney(quote.closePrice()));
            addValueCell(table, formatDecimal(quote.volume()));
            addValueCell(table, formatMoney(quote.marketCap()));
        }

        document.add(table);
        addEmptyLine(document);
    }
}