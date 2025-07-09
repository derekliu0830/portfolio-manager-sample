package com.trading.model;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class Account {
    private String accountId;
    private String accountName;
    private Portfolio portfolio;
    private BigDecimal cashBalance;
    private LocalDateTime createdAt;
    private AccountStatus status;

    public Account(String accountId, String accountName) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.portfolio = new Portfolio();
        this.cashBalance = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.status = AccountStatus.ACTIVE;
    }

    public BigDecimal getTotalValue() {
        return portfolio.getTotalValue().add(cashBalance);
    }

    public boolean hasSufficientCash(BigDecimal amount) {
        return cashBalance.compareTo(amount) >= 0;
    }

    public void addCash(BigDecimal amount) {
        this.cashBalance = this.cashBalance.add(amount);
    }

    public void deductCash(BigDecimal amount) {
        if (!hasSufficientCash(amount)) {
            throw new InsufficientFundsException("Insufficient cash balance");
        }
        this.cashBalance = this.cashBalance.subtract(amount);
    }

    public enum AccountStatus {
        ACTIVE, SUSPENDED, CLOSED
    }

    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }
}