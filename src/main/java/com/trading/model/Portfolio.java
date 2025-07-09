package com.trading.model;

import lombok.Getter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Portfolio {
    private final List<Position> positions;
    private final Map<Security, Position> positionsBySecurity;
    private static final int DECIMAL_PLACES = 2;

    public Portfolio() {
        this.positions = new ArrayList<>();
        this.positionsBySecurity = new ConcurrentHashMap<>();
    }

    public Portfolio(List<Position> positions) {
        this.positions = new ArrayList<>();
        this.positionsBySecurity = new ConcurrentHashMap<>();
        positions.forEach(this::addPosition);
    }

    public void addPosition(Position position) {
        Security security = position.getSecurity();
        Position existingPosition = positionsBySecurity.get(security);

        if (existingPosition != null) {
            // Merge positions with the same security
            BigDecimal newQuantity = existingPosition.getQuantity().add(position.getQuantity());
            existingPosition.setQuantity(newQuantity);
        } else {
            positions.add(position);
            positionsBySecurity.put(security, position);
        }
    }

    public void updatePrice(String ticker, BigDecimal price) {
        positions.stream()
                .filter(position -> position.getSecurity().getTicker().equals(ticker))
                .forEach(position -> position.updatePrice(price));
    }

    public BigDecimal getTotalValue() {
        return positions.stream()
                .map(Position::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
    }

    public List<Position> getPositions() {
        return Collections.unmodifiableList(positions);
    }

    public Position getPosition(Security security) {
        return positionsBySecurity.get(security);
    }

    public List<Position> getPositionsByTicker(String ticker) {
        return positions.stream()
                .filter(position -> position.getSecurity().getTicker().equals(ticker))
                .collect(java.util.stream.Collectors.toList());
    }
}