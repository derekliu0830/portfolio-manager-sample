package com.trading.io;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.trading.model.Position;
import com.trading.model.Security;
import com.trading.model.SecurityType;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVPositionReader {
    private static final Map<String, Month> MONTH_MAP = new HashMap<>();
    private static final int DECIMAL_PLACES = 4;

    static {
        MONTH_MAP.put("JAN", Month.JANUARY);
        MONTH_MAP.put("FEB", Month.FEBRUARY);
        MONTH_MAP.put("MAR", Month.MARCH);
        MONTH_MAP.put("APR", Month.APRIL);
        MONTH_MAP.put("MAY", Month.MAY);
        MONTH_MAP.put("JUN", Month.JUNE);
        MONTH_MAP.put("JUL", Month.JULY);
        MONTH_MAP.put("AUG", Month.AUGUST);
        MONTH_MAP.put("SEP", Month.SEPTEMBER);
        MONTH_MAP.put("OCT", Month.OCTOBER);
        MONTH_MAP.put("NOV", Month.NOVEMBER);
        MONTH_MAP.put("DEC", Month.DECEMBER);
    }

    public List<Position> readPositions(String filePath) throws IOException {
        List<Position> positions = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(getClass().getResourceAsStream(filePath)))) {
            // Skip header
            reader.readNext();

            String[] line;
            while ((line = reader.readNext()) != null) {
                positions.add(parsePosition(line));
            }
        } catch (CsvValidationException e) {
            throw new IOException("Error reading CSV file", e);
        }

        return positions;
    }

    private Position parsePosition(String[] line) {
        String symbol = line[0].trim();
        BigDecimal quantity = new BigDecimal(line[1].trim());

        if (symbol.contains("-")) {
            // Parse option
            String[] parts = symbol.split("-");
            String ticker = parts[0];

            // Parse expiration date
            Month month = MONTH_MAP.get(parts[1].toUpperCase());
            int year = Integer.parseInt(parts[2]);
            LocalDate maturity = LocalDate.of(year, month, 1);

            BigDecimal strike = new BigDecimal(parts[3]);
            SecurityType type = parts[4].equals("C") ? SecurityType.CALL_OPTION : SecurityType.PUT_OPTION;

            BigDecimal timeToMaturity = new BigDecimal(ChronoUnit.DAYS.between(LocalDate.now(), maturity))
                    .divide(new BigDecimal("365"), DECIMAL_PLACES, RoundingMode.HALF_UP);
            BigDecimal volatility = new BigDecimal("0.30"); // Default volatility of 30%
            BigDecimal mu = new BigDecimal("0.05"); // Default expected return of 5%

            Security security = new Security(ticker, type, strike, timeToMaturity, mu, volatility, month, year);
            return new Position(security, quantity);
        } else {
            // Parse stock
            Security security = new Security(symbol);
            return new Position(security, quantity);
        }
    }
}