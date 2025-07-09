package com.trading.market;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import org.apache.commons.math3.distribution.NormalDistribution;

public class MockMarketDataProvider implements MarketDataProvider {
    private final Map<String, Set<MarketDataListener>> listeners = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> currentPrices = new ConcurrentHashMap<>();
    private final NormalDistribution normalDist = new NormalDistribution(0, 1);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean running = false;

    private static final Map<String, BigDecimal> INITIAL_PRICES = new ConcurrentHashMap<>();
    private static final int DECIMAL_PLACES = 2;

    static {
        INITIAL_PRICES.put("AAPL", new BigDecimal("180.00"));
        INITIAL_PRICES.put("GOOGL", new BigDecimal("140.00"));
        INITIAL_PRICES.put("MSFT", new BigDecimal("350.00"));
    }

    @Override
    public void start() {
        if (!running) {
            running = true;
            // Schedule price updates every second
            executor.scheduleAtFixedRate(this::updatePrices, 0, 1000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void stop() {
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void subscribe(String ticker, MarketDataListener listener) {
        listeners.computeIfAbsent(ticker, k -> ConcurrentHashMap.newKeySet()).add(listener);
        // Initialize price if not exists
        if (!currentPrices.containsKey(ticker)) {
            BigDecimal initialPrice = INITIAL_PRICES.getOrDefault(ticker, new BigDecimal("100.00"));
            currentPrices.put(ticker, initialPrice);
            // Notify listener immediately with initial price
            listener.onPriceUpdate(ticker, initialPrice);
        } else {
            // Notify listener with current price
            listener.onPriceUpdate(ticker, currentPrices.get(ticker));
        }
    }

    @Override
    public void unsubscribe(String ticker, MarketDataListener listener) {
        Set<MarketDataListener> tickerListeners = listeners.get(ticker);
        if (tickerListeners != null) {
            tickerListeners.remove(listener);
            if (tickerListeners.isEmpty()) {
                listeners.remove(ticker);
                currentPrices.remove(ticker);
            }
        }
    }

    private void updatePrices() {
        currentPrices.forEach((ticker, price) -> {
            BigDecimal newPrice = calculateNextPrice(price);
            currentPrices.put(ticker, newPrice);

            // Notify listeners
            Set<MarketDataListener> tickerListeners = listeners.get(ticker);
            if (tickerListeners != null) {
                tickerListeners.forEach(listener -> listener.onPriceUpdate(ticker, newPrice));
            }
        });
    }

    private BigDecimal calculateNextPrice(BigDecimal currentPrice) {
        // Parameters for geometric Brownian motion
        BigDecimal mu = new BigDecimal("0.05");  // Expected return (5%)
        BigDecimal sigma = new BigDecimal("0.30"); // Volatility (30%)
        BigDecimal dt = new BigDecimal("1.0")
                .divide(new BigDecimal("252"), 10, RoundingMode.HALF_UP)
                .divide(new BigDecimal("6.5"), 10, RoundingMode.HALF_UP)
                .divide(new BigDecimal("60"), 10, RoundingMode.HALF_UP); // Time step in years (1 minute)

        // Generate random normal variable
        BigDecimal epsilon = new BigDecimal(normalDist.sample());

        // Calculate price change using geometric Brownian motion formula
        BigDecimal sqrtDt = new BigDecimal(Math.sqrt(dt.doubleValue()));
        BigDecimal dS = currentPrice.multiply(
                mu.multiply(dt).add(
                        sigma.multiply(sqrtDt).multiply(epsilon)
                )
        );
        BigDecimal newPrice = currentPrice.add(dS);

        // Ensure price doesn't go below 0.01
        return newPrice.max(new BigDecimal("0.01")).setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
    }
}