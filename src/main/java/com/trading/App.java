package com.trading;

import com.trading.io.CSVPositionReader;
import com.trading.market.MockMarketDataProvider;
import com.trading.model.Account;
import com.trading.model.Portfolio;
import com.trading.model.Position;
import com.trading.portfolio.PortfolioSubscriber;

import java.io.IOException;
import java.util.List;

public class App {
    public static void main(String[] args) {
        try {
            // Read positions from CSV in resources
            CSVPositionReader reader = new CSVPositionReader();
            List<Position> positions = reader.readPositions("/positions.csv");

            // Create account with portfolio
            Account account = new Account("ACC001", "Demo Account");
            Portfolio portfolio = account.getPortfolio();

            // Add positions to portfolio
            positions.forEach(portfolio::addPosition);

            // Create market data provider
            MockMarketDataProvider marketDataProvider = new MockMarketDataProvider();

            // Create and start portfolio subscriber with account
            PortfolioSubscriber subscriber = new PortfolioSubscriber(account, marketDataProvider);
            subscriber.start();

            // Keep the application running
            System.out.println("Press Enter to exit...");
            System.in.read();

            // Clean up
            subscriber.stop();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}