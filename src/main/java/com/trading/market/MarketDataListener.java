package com.trading.market;

import java.math.BigDecimal;

@FunctionalInterface
public interface MarketDataListener {
    void onPriceUpdate(String ticker, BigDecimal price);
}