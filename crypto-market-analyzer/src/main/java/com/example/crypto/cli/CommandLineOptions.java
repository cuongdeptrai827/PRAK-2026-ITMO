package com.example.crypto.cli;

import com.example.crypto.dataloading.ETLService;
import com.example.crypto.reporting.ReportService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.LocalDate;
import java.util.List;

@Command(
        name = "crypto-analyzer",
        mixinStandardHelpOptions = true,
        version = "crypto-analyzer 1.0",
        description = "Console application for cryptocurrency market analysis and PDF report generation."
)
public class CommandLineOptions implements Runnable {

    @Option(
            names = "--assets",
            required = true,
            split = ",",
            description = "Cryptocurrency symbols, for example: BTC,ETH"
    )
    private List<String> assets;

    @Option(
            names = "--from",
            required = true,
            description = "Start date in format YYYY-MM-DD"
    )
    private LocalDate from;

    @Option(
            names = "--to",
            required = true,
            description = "End date in format YYYY-MM-DD"
    )
    private LocalDate to;

    @Option(
            names = "--source",
            defaultValue = "both",
            description = "Data source: coingecko, binance, both"
    )
    private String source;

    @Option(
            names = "--report",
            required = true,
            description = "Report type: asset, comparison, correlation, anomalies, forecast"
    )
    private String reportType;

    @Option(
            names = "--output",
            required = true,
            description = "Output PDF file path"
    )
    private String output;


    @Override
    public void run() {
        printStartInfo();
        validateArguments();

        System.out.println("Arguments are valid.");
        System.out.println();

        ETLService etlService = new ETLService();
        etlService.loadMarketData(assets, from, to, source);

        ReportService reportService = new ReportService();
        reportService.generateReport(
                assets,
                from,
                to,
                source,
                reportType,
                output
        );

        System.out.println();
        System.out.println("Done.");
    }

    private void printStartInfo() {
        System.out.println();
        System.out.println("Crypto Market Analyzer");
        System.out.println("--------------------------------");
        System.out.println("Report type: " + reportType);
        System.out.println("Assets: " + String.join(", ", assets));
        System.out.println("Period: " + from + " -> " + to);
        System.out.println("Source: " + source);
        System.out.println("Output: " + output);
        System.out.println("--------------------------------");
    }

    private void validateArguments() {
        if (assets == null || assets.isEmpty()) {
            throw new IllegalArgumentException("At least one asset must be provided.");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        List<String> allowedSources = List.of("coingecko", "binance", "both");
        if (!allowedSources.contains(source.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid source. Allowed values: coingecko, binance, both."
            );
        }

        List<String> allowedReports = List.of(
                "asset",
                "comparison",
                "correlation",
                "anomalies",
                "forecast"
        );

        if (!allowedReports.contains(reportType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid report type. Allowed values: asset, comparison, correlation, anomalies, forecast."
            );
        }

        if (!output.endsWith(".pdf")) {
            throw new IllegalArgumentException("Output file must have .pdf extension.");
        }
    }

    public List<String> getAssets() {
        return assets;
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getTo() {
        return to;
    }

    public String getSource() {
        return source;
    }

    public String getReportType() {
        return reportType;
    }

    public String getOutput() {
        return output;
    }
}