package com.trading.market;

public interface MarketDataProvider {
    void start();
    void stop();
    void subscribe(String ticker, MarketDataListener listener);
    void unsubscribe(String ticker, MarketDataListener listener);
}