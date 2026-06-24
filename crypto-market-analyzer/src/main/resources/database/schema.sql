CREATE TABLE IF NOT EXISTS crypto_assets (
                                             id SERIAL PRIMARY KEY,
                                             symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS exchanges (
                                         id SERIAL PRIMARY KEY,
                                         name VARCHAR(100) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS trading_pairs (
                                             id SERIAL PRIMARY KEY,
                                             base_asset_id INT NOT NULL REFERENCES crypto_assets(id),
    quote_asset VARCHAR(20) NOT NULL,
    exchange_id INT NOT NULL REFERENCES exchanges(id),
    symbol VARCHAR(30) NOT NULL,
    UNIQUE(base_asset_id, quote_asset, exchange_id, symbol)
    );

CREATE TABLE IF NOT EXISTS market_quotes (
                                             id SERIAL PRIMARY KEY,
                                             asset_id INT NOT NULL REFERENCES crypto_assets(id),
    exchange_id INT NOT NULL REFERENCES exchanges(id),
    trading_pair_id INT REFERENCES trading_pairs(id),
    timestamp TIMESTAMP NOT NULL,
    open_price NUMERIC(20, 8),
    close_price NUMERIC(20, 8),
    high_price NUMERIC(20, 8),
    low_price NUMERIC(20, 8),
    volume NUMERIC(30, 8),
    market_cap NUMERIC(30, 2),
    source VARCHAR(50) NOT NULL,
    UNIQUE(asset_id, exchange_id, trading_pair_id, timestamp, source)
    );

CREATE TABLE IF NOT EXISTS volatility_metrics (
                                                  id SERIAL PRIMARY KEY,
                                                  asset_id INT NOT NULL REFERENCES crypto_assets(id),
    period_type VARCHAR(20) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    volatility_value NUMERIC(20, 8),
    rmsd_return NUMERIC(20, 8)
    );

CREATE TABLE IF NOT EXISTS correlation_metrics (
                                                   id SERIAL PRIMARY KEY,
                                                   asset_1_id INT NOT NULL REFERENCES crypto_assets(id),
    asset_2_id INT NOT NULL REFERENCES crypto_assets(id),
    method VARCHAR(20) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    correlation_value NUMERIC(10, 6)
    );

CREATE TABLE IF NOT EXISTS anomaly_events (
                                              id SERIAL PRIMARY KEY,
                                              asset_id INT NOT NULL REFERENCES crypto_assets(id),
    event_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    value NUMERIC(30, 8),
    description TEXT,
    severity VARCHAR(20)
    );

CREATE TABLE IF NOT EXISTS forecast_results (
                                                id SERIAL PRIMARY KEY,
                                                asset_id INT NOT NULL REFERENCES crypto_assets(id),
    target_type VARCHAR(20) NOT NULL,
    forecast_date TIMESTAMP NOT NULL,
    predicted_value NUMERIC(30, 8),
    model_name VARCHAR(100)
    );

CREATE TABLE IF NOT EXISTS etl_logs (
                                        id SERIAL PRIMARY KEY,
                                        source VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    message TEXT
    );