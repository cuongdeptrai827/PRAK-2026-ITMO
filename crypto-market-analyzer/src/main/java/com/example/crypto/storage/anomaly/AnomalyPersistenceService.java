package com.example.crypto.storage.anomaly;

import com.example.crypto.model.AnomalyEvent;
import com.example.crypto.model.CryptoAsset;
import com.example.crypto.storage.CryptoAssetRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnomalyPersistenceService {

    private final CryptoAssetRepository cryptoAssetRepository = new CryptoAssetRepository();
    private final AnomalyEventRepository anomalyEventRepository = new AnomalyEventRepository();

    public void saveAnomalies(
            List<AnomalyEvent> anomalies,
            List<String> assets,
            LocalDate from,
            LocalDate to
    ) {
        deleteOldAnomalies(assets, from, to);

        for (AnomalyEvent anomaly : anomalies) {
            int assetId = findAssetIdBySymbol(anomaly.symbol());
            anomalyEventRepository.save(assetId, anomaly);
        }
    }

    private void deleteOldAnomalies(
            List<String> assets,
            LocalDate from,
            LocalDate to
    ) {
        Set<String> uniqueAssets = new HashSet<>();

        for (String asset : assets) {
            uniqueAssets.add(asset.trim().toUpperCase());
        }

        for (String symbol : uniqueAssets) {
            int assetId = findAssetIdBySymbol(symbol);
            anomalyEventRepository.deleteByAssetAndPeriod(assetId, from, to);
        }
    }

    private int findAssetIdBySymbol(String symbol) {
        CryptoAsset asset = cryptoAssetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Asset not found in database: " + symbol));

        return asset.getId();
    }
}