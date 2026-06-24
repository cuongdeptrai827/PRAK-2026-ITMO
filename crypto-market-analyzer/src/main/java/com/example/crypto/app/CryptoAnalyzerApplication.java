package com.example.crypto.app;

import com.example.crypto.cli.CommandLineOptions;
import picocli.CommandLine;

public class CryptoAnalyzerApplication {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CommandLineOptions()).execute(args);
        System.exit(exitCode);
    }
}