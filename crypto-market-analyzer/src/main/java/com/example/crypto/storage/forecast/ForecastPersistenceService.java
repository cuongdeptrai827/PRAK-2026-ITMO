package com.example.crypto.storage.forecast;

import com.example.crypto.model.CryptoAsset;
import com.example.crypto.model.ForecastResult;
import com.example.crypto.storage.CryptoAssetRepository;

import java.util.List;

public class ForecastPersistenceService {

    private final CryptoAssetRepository cryptoAssetRepository = new CryptoAssetRepository();
    private final ForecastResultRepository forecastResultRepository = new ForecastResultRepository();

    public void saveForecastResults(List<ForecastResult> forecastResults) {
        if (forecastResults == null || forecastResults.isEmpty()) {
            return;
        }

        for (ForecastResult forecastResult : forecastResults) {
            int assetId = findAssetIdBySymbol(forecastResult.symbol());
            forecastResultRepository.save(assetId, forecastResult);
        }
    }

    private int findAssetIdBySymbol(String symbol) {
        CryptoAsset asset = cryptoAssetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Asset not found in database: " + symbol));

        return asset.getId();
    }
}