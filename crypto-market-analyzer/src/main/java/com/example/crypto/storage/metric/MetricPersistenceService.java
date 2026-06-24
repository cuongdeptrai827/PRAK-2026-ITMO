package com.example.crypto.storage.metric;

import com.example.crypto.model.CorrelationResult;
import com.example.crypto.model.CryptoAsset;
import com.example.crypto.model.VolatilityResult;
import com.example.crypto.storage.CryptoAssetRepository;

import java.time.LocalDate;
import java.util.List;

public class MetricPersistenceService {

    private final CryptoAssetRepository cryptoAssetRepository = new CryptoAssetRepository();
    private final VolatilityMetricRepository volatilityMetricRepository = new VolatilityMetricRepository();
    private final CorrelationMetricRepository correlationMetricRepository = new CorrelationMetricRepository();

    public void saveVolatilityResults(
            List<VolatilityResult> volatilityResults,
            LocalDate from,
            LocalDate to
    ) {
        for (VolatilityResult result : volatilityResults) {
            int assetId = findAssetIdBySymbol(result.symbol());

            volatilityMetricRepository.save(
                    assetId,
                    "DAILY",
                    from,
                    to,
                    result.dailyVolatility(),
                    result.rmsdReturn()
            );

            volatilityMetricRepository.save(
                    assetId,
                    "WEEKLY",
                    from,
                    to,
                    result.weeklyVolatility(),
                    null
            );

            volatilityMetricRepository.save(
                    assetId,
                    "MONTHLY",
                    from,
                    to,
                    result.monthlyVolatility(),
                    null
            );
        }
    }

    public void saveCorrelationResult(
            CorrelationResult correlationResult,
            LocalDate from,
            LocalDate to
    ) {
        if (correlationResult == null) {
            return;
        }

        int asset1Id = findAssetIdBySymbol(correlationResult.asset1());
        int asset2Id = findAssetIdBySymbol(correlationResult.asset2());

        correlationMetricRepository.save(
                asset1Id,
                asset2Id,
                correlationResult.method(),
                from,
                to,
                correlationResult.correlationValue()
        );
    }

    private int findAssetIdBySymbol(String symbol) {
        CryptoAsset asset = cryptoAssetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Asset not found in database: " + symbol));

        return asset.getId();
    }
}