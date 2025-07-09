package com.trading.account;

import com.trading.model.Account;
import com.trading.model.Portfolio;
import com.trading.model.Position;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AccountManager {
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    public Account createAccount(String accountId, String accountName) {
        Account account = new Account(accountId, accountName);
        accounts.put(accountId, account);
        return account;
    }

    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    public List<Account> getAllAccounts() {
        return accounts.values().stream().collect(Collectors.toList());
    }

    public void removeAccount(String accountId) {
        accounts.remove(accountId);
    }

    public boolean accountExists(String accountId) {
        return accounts.containsKey(accountId);
    }

    public void addPositionToAccount(String accountId, Position position) {
        Account account = getAccount(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        account.getPortfolio().addPosition(position);
    }

    public Portfolio getAccountPortfolio(String accountId) {
        Account account = getAccount(accountId);
        return account != null ? account.getPortfolio() : null;
    }
}