# Portfolio Manager Sample

This is a sample implementation of a real-time portfolio valuation system. The system handles common stocks, European Call options, and European Put options.

## Project Structure

```
portfolio-manager-sample/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── trading/
│   │   │           ├── App.java
│   │   │           ├── io/
│   │   │           │   └── CSVPositionReader.java
│   │   │           ├── market/
│   │   │           │   ├── MarketDataListener.java
│   │   │           │   ├── MarketDataProvider.java
│   │   │           │   └── MockMarketDataProvider.java
│   │   │           ├── model/
│   │   │           │   ├── Position.java
│   │   │           │   ├── Security.java
│   │   │           │   └── SecurityType.java
│   │   │           ├── portfolio/
│   │   │           │   ├── Portfolio.java
│   │   │           │   └── PortfolioSubscriber.java
│   │   │           └── pricing/
│   │   │               └── OptionPriceCalculator.java
│   │   └── resources/
│   │       └── positions.csv
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── trading/
│       │           └── ...
│       └── resources/
│           └── test-positions.csv
├── build.gradle
├── gradle.properties
├── settings.gradle
└── README.md
```

## Features

- Real-time portfolio valuation
- Support for multiple security types:
  - Common stocks
  - European Call options
  - European Put options
- CSV-based position loading
- Embedded H2 database for security definitions
- Mock market data provider using geometric Brownian motion
- Option pricing calculations

## Building the Project

```bash
./gradlew clean build
```

## Running the Application

```bash
./gradlew run
```

## Dependencies

- H2 Database (1.4.200)
- Apache Commons Math (3.6.1)
- OpenCSV (5.5.2)
- Lombok (1.18.22)
- JUnit Jupiter (5.8.1)
- Mockito (3.12.4)

## Configuration

The application reads positions from `src/main/resources/positions.csv` file in the following format:

```csv
Symbol,Quantity
AAPL,100
AAPL-JAN-2024-150-C,10
TESLA,50
```

For options, the symbol format is: `TICKER-MONTH-YEAR-STRIKE-TYPE`
- TICKER: Stock symbol
- MONTH: Three-letter month code (JAN, FEB, etc.)
- YEAR: Four-digit year
- STRIKE: Strike price
- TYPE: C for Call, P for Put