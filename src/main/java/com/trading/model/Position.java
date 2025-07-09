package com.trading.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private Security security;
    private BigDecimal quantity;
    private BigDecimal marketPrice;

    private static final BigDecimal OPTION_CONTRACT_SIZE = new BigDecimal("100");
    private static final int DECIMAL_PLACES = 2;

    public Position(Security security, BigDecimal quantity) {
        this.security = security;
        this.quantity = quantity;
        this.marketPrice = BigDecimal.ZERO;
    }

    public BigDecimal getMarketValue() {
        // For stocks: quantity * price
        // For options: quantity * price * 100 (standard contract size)
        BigDecimal value = quantity.multiply(marketPrice);

        if (security.getType() == SecurityType.STOCK) {
            return value.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
        } else {
            // For options, multiply by contract size (100 shares per contract)
            return value.multiply(OPTION_CONTRACT_SIZE).setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
        }
    }

    // Update the current price of the position
    public void updatePrice(BigDecimal newPrice) {
        this.marketPrice = newPrice;
    }
}