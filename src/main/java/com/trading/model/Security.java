package com.trading.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.Objects;

@Getter
@Setter
public class Security {
    private String ticker;
    private SecurityType type;
    private BigDecimal strike;        // Strike price for options
    private BigDecimal timeToMaturity; // Time to maturity in years
    private BigDecimal mu;           // Expected return (between 0 and 1)
    private BigDecimal sigma;        // Annualized standard deviation (between 0 and 1)

    // Original option details for display purposes
    private Month expirationMonth;   // Original expiration month
    private Integer expirationYear;  // Original expiration year

    private static final int DECIMAL_PLACES = 4;

    public Security() {
        this.mu = new BigDecimal("0.05");     // 5% expected return
        this.sigma = new BigDecimal("0.30");   // 30% volatility
    }

    public Security(String ticker) {
        this();
        this.ticker = ticker;
        this.type = SecurityType.STOCK;
        this.strike = BigDecimal.ZERO;
        this.timeToMaturity = BigDecimal.ZERO;
    }

    public Security(String ticker, SecurityType type, BigDecimal strike, BigDecimal timeToMaturity) {
        if (type == SecurityType.STOCK) {
            throw new IllegalArgumentException("Use the single argument constructor for stocks");
        }
        this.ticker = ticker;
        this.type = type;
        this.strike = strike;
        this.timeToMaturity = timeToMaturity;
        this.mu = new BigDecimal("0.05");     // Default expected return
        this.sigma = new BigDecimal("0.30");   // Default volatility
    }

    public Security(String ticker, SecurityType type, BigDecimal strike, BigDecimal timeToMaturity,
                   BigDecimal mu, BigDecimal sigma) {
        this.ticker = ticker;
        this.type = type;
        this.strike = strike.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
        this.timeToMaturity = timeToMaturity.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
        this.mu = mu.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
        this.sigma = sigma.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
    }

    public Security(String ticker, SecurityType type, BigDecimal strike, BigDecimal timeToMaturity,
                   BigDecimal mu, BigDecimal sigma, Month expirationMonth, Integer expirationYear) {
        this.ticker = ticker;
        this.type = type;
        this.strike = strike.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
        this.timeToMaturity = timeToMaturity.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
        this.mu = mu.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
        this.sigma = sigma.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
        this.expirationMonth = expirationMonth;
        this.expirationYear = expirationYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Security security = (Security) o;
        return Objects.equals(ticker, security.ticker) &&
               type == security.type &&
               Objects.equals(strike, security.strike) &&
               Objects.equals(timeToMaturity, security.timeToMaturity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker, type, strike, timeToMaturity);
    }

    @Override
    public String toString() {
        if (type == SecurityType.STOCK) {
            return ticker;
        } else {
            return String.format("%s-%s-%s-%s", ticker, type, strike, timeToMaturity);
        }
    }
}