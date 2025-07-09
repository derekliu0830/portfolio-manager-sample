package com.trading.portfolio;

import com.trading.market.MarketDataListener;
import com.trading.market.MarketDataProvider;
import com.trading.model.Account;
import com.trading.model.Portfolio;
import com.trading.model.Position;
import com.trading.model.Security;
import com.trading.model.SecurityType;
import com.trading.pricing.OptionPriceCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.Month;

public class PortfolioSubscriber {
    private final Account account;
    private final Portfolio portfolio;
    private final MarketDataProvider marketDataProvider;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static final int DECIMAL_PLACES = 2;

    public PortfolioSubscriber(Account account, MarketDataProvider marketDataProvider) {
        this.account = account;
        this.portfolio = account.getPortfolio();
        this.marketDataProvider = marketDataProvider;
    }

    public PortfolioSubscriber(Portfolio portfolio, MarketDataProvider marketDataProvider) {
        this.account = null;
        this.portfolio = portfolio;
        this.marketDataProvider = marketDataProvider;
    }

    public void start() {
        // Subscribe to market data for each position
        for (Position position : portfolio.getPositions()) {
            Security security = position.getSecurity();
            marketDataProvider.subscribe(security.getTicker(), createListener(position));
        }

        // Start market data provider
        marketDataProvider.start();

        // Schedule portfolio value display
        executor.scheduleAtFixedRate(this::displayPortfolioValue, 0, 3, TimeUnit.SECONDS);
    }

    public void stop() {
        // Unsubscribe from market data
        for (Position position : portfolio.getPositions()) {
            Security security = position.getSecurity();
            marketDataProvider.unsubscribe(security.getTicker(), createListener(position));
        }

        // Stop market data provider and executor
        marketDataProvider.stop();
        executor.shutdown();
    }

    private MarketDataListener createListener(Position position) {
        return (ticker, price) -> {
            Security security = position.getSecurity();
            BigDecimal marketPrice = price;

            // Calculate option price if necessary
            if (security.getType() != SecurityType.STOCK) {
                marketPrice = OptionPriceCalculator.calculateOptionPrice(
                    price,
                    security.getStrike(),
                    security.getTimeToMaturity(),
                    security.getSigma(),
                    security.getType()
                );
            }

            // Update position market value
            position.updatePrice(marketPrice);
        };
    }

    private void displayPortfolioValue() {
        List<Position> positions = portfolio.getPositions();
        BigDecimal totalValue = BigDecimal.ZERO;

        System.out.println("\nPortfolio Value Update:");

        // Display account information if available
        if (account != null) {
            System.out.println("Account: " + account.getAccountName() + " (" + account.getAccountId() + ")");
        }

        System.out.println("Symbol                Type        Quantity    Price       Market Value");
        System.out.println("---------------------------------------------------------------------");

        for (Position position : positions) {
            Security security = position.getSecurity();
            BigDecimal marketValue = position.getMarketValue();
            totalValue = totalValue.add(marketValue);

            String symbolDisplay = getSymbolDisplay(security);
            String typeDisplay = getTypeDisplay(security);

            System.out.printf("%-20s %-12s %-12s %-12s %-12s%n",
                symbolDisplay,
                typeDisplay,
                position.getQuantity().setScale(DECIMAL_PLACES, RoundingMode.HALF_UP),
                position.getMarketPrice().setScale(DECIMAL_PLACES, RoundingMode.HALF_UP),
                marketValue.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP));
        }

        System.out.println("---------------------------------------------------------------------");
        System.out.printf("Portfolio Value: %s%n", totalValue.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP));
    }

    private String getSymbolDisplay(Security security) {
        if (security.getType() == SecurityType.STOCK) {
            return security.getTicker();
        } else {
            // For options, show ticker-month-year-strike-type format to match CSV
            String optionType = security.getType() == SecurityType.CALL_OPTION ? "C" : "P";
            String monthStr = getMonthString(security.getExpirationMonth());
            return String.format("%s-%s-%d-%s-%s",
                security.getTicker(),
                monthStr,
                security.getExpirationYear(),
                security.getStrike().setScale(0, RoundingMode.HALF_UP),
                optionType);
        }
    }

    private String getMonthString(Month month) {
        if (month == null) return "UNK";
        switch (month) {
            case JANUARY: return "JAN";
            case FEBRUARY: return "FEB";
            case MARCH: return "MAR";
            case APRIL: return "APR";
            case MAY: return "MAY";
            case JUNE: return "JUN";
            case JULY: return "JUL";
            case AUGUST: return "AUG";
            case SEPTEMBER: return "SEP";
            case OCTOBER: return "OCT";
            case NOVEMBER: return "NOV";
            case DECEMBER: return "DEC";
            default: return "UNK";
        }
    }

    private String getTypeDisplay(Security security) {
        switch (security.getType()) {
            case STOCK:
                return "STOCK";
            case CALL_OPTION:
                return "CALL";
            case PUT_OPTION:
                return "PUT";
            default:
                return "UNKNOWN";
        }
    }
}