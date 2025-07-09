package com.trading.pricing;

import org.apache.commons.math3.distribution.NormalDistribution;
import com.trading.model.SecurityType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OptionPriceCalculator {
    private static final NormalDistribution NORMAL = new NormalDistribution(0, 1);
    private static final BigDecimal RISK_FREE_RATE = new BigDecimal("0.02"); // 2% risk-free rate
    private static final int DECIMAL_PLACES = 4;

    public static BigDecimal calculateOptionPrice(BigDecimal spotPrice, BigDecimal strikePrice,
                                           BigDecimal timeToMaturityYears, BigDecimal volatility,
                                           SecurityType optionType) {
        if (timeToMaturityYears.compareTo(BigDecimal.ZERO) <= 0) {
            return calculateIntrinsicValue(spotPrice, strikePrice, optionType);
        }

        double d1 = calculateD1(spotPrice.doubleValue(), strikePrice.doubleValue(),
                              timeToMaturityYears.doubleValue(), volatility.doubleValue());
        double d2 = d1 - volatility.doubleValue() * Math.sqrt(timeToMaturityYears.doubleValue());

        switch (optionType) {
            case CALL_OPTION:
                return calculateCallPrice(spotPrice, strikePrice, timeToMaturityYears, d1, d2);
            case PUT_OPTION:
                return calculatePutPrice(spotPrice, strikePrice, timeToMaturityYears, d1, d2);
            default:
                throw new IllegalArgumentException("Invalid option type: " + optionType);
        }
    }

    private static BigDecimal calculateIntrinsicValue(BigDecimal spotPrice, BigDecimal strikePrice,
                                                    SecurityType optionType) {
        switch (optionType) {
            case CALL_OPTION:
                return spotPrice.subtract(strikePrice).max(BigDecimal.ZERO)
                       .setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
            case PUT_OPTION:
                return strikePrice.subtract(spotPrice).max(BigDecimal.ZERO)
                       .setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
            default:
                throw new IllegalArgumentException("Invalid option type: " + optionType);
        }
    }

    private static double calculateD1(double spotPrice, double strikePrice,
                                    double timeToMaturityYears, double volatility) {
        return (Math.log(spotPrice / strikePrice) +
                (RISK_FREE_RATE.doubleValue() + volatility * volatility / 2) * timeToMaturityYears) /
                (volatility * Math.sqrt(timeToMaturityYears));
    }

    private static BigDecimal calculateCallPrice(BigDecimal spotPrice, BigDecimal strikePrice,
                                          BigDecimal timeToMaturityYears, double d1, double d2) {
        BigDecimal term1 = spotPrice.multiply(new BigDecimal(NORMAL.cumulativeProbability(d1)));
        BigDecimal term2 = strikePrice.multiply(
            new BigDecimal(Math.exp(-RISK_FREE_RATE.doubleValue() * timeToMaturityYears.doubleValue()))
        ).multiply(new BigDecimal(NORMAL.cumulativeProbability(d2)));

        return term1.subtract(term2).setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculatePutPrice(BigDecimal spotPrice, BigDecimal strikePrice,
                                         BigDecimal timeToMaturityYears, double d1, double d2) {
        BigDecimal term1 = strikePrice.multiply(
            new BigDecimal(Math.exp(-RISK_FREE_RATE.doubleValue() * timeToMaturityYears.doubleValue()))
        ).multiply(new BigDecimal(NORMAL.cumulativeProbability(-d2)));

        BigDecimal term2 = spotPrice.multiply(new BigDecimal(NORMAL.cumulativeProbability(-d1)));

        return term1.subtract(term2).setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
    }
}