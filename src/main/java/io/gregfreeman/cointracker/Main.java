package io.gregfreeman.cointracker;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import io.gregfreeman.cointracker.coindata.Coin;
import io.gregfreeman.cointracker.coindata.CoinDataService;
import io.gregfreeman.cointracker.config.Config;
import io.gregfreeman.cointracker.config.ConfigParser;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Config file path is a required argument");
            System.exit(1);
        }

        Config config = null;

        try {
            config = ConfigParser.getConfig(args[0]);
        } catch (Exception e) {
            System.out.println("Config file could not be read");
            System.exit(1);
        }

        List<Coin> coins = null;

        try {
            coins = CoinDataService.getCoinData("cad", 150);
        } catch (Exception e) {
            System.out.println("Coin data is unavailable");
            System.exit(1);
        }

        coins = CoinDataService.filterCoins(coins, config.holdings);

        float totalCAD = 0;
        float totalUSD = 0;
        float totalBTC = 0;
        float totalETH = 0;
        float ETHUSDPrice = 0;

        for (Coin coin : coins) {
            float numberOfCoins = config.holdings.getOrDefault(coin.symbol, 0f);

            totalCAD += numberOfCoins * coin.priceCAD;
            totalUSD += numberOfCoins * coin.priceUSD;
            totalBTC += numberOfCoins * coin.priceBTC;

            if (coin.symbol.equals("ETH")) {
                ETHUSDPrice = coin.priceUSD;
            }
        }

        if (ETHUSDPrice > 0) {
            totalETH = totalUSD / ETHUSDPrice;
        }

        List<String[]> rows = new ArrayList<String[]>();

        for (Coin coin : coins) {
            float numberOfCoins = config.holdings.getOrDefault(coin.symbol, 0f);

            float priceCAD = numberOfCoins * coin.priceCAD;
            float priceUSD = numberOfCoins * coin.priceUSD;

            float percentage = 0;

            if (totalUSD > 0) {
                percentage = priceUSD / totalUSD * 100;
            }

            float priceETH = 0;
            float coinPriceETH = 0;

            if (ETHUSDPrice > 0) {
                priceETH = priceUSD / ETHUSDPrice;
                coinPriceETH = coin.priceUSD / ETHUSDPrice;
            }

            rows.add(new String[]{
                    coin.symbol,
                    String.format("%.2f%%", percentage),
                    String.format("$%.4f", priceCAD),
                    String.format("$%.4f", coin.priceCAD),
                    String.format("$%.4f", priceUSD),
                    String.format("$%.4f", coin.priceUSD),
                    String.format("%.4f", priceETH),
                    String.format("%.8f", coinPriceETH)
            });
        }

        CWC_LongestLine cwc = new CWC_LongestLine();
        AsciiTable summaryTable = new AsciiTable();
        summaryTable.addRule();
        summaryTable.addRow("Return", "CAD", "USD", "BTC", "ETH");
        summaryTable.addRule();
        summaryTable.addRow(
                String.format("%.2f%%", CoinDataService.percentDiff(config.investmentAmount, totalCAD)),
                String.format("$%.4f", totalCAD),
                String.format("$%.4f", totalUSD),
                String.format("%.4f", totalBTC),
                String.format("%.4f", totalETH)
        );
        summaryTable.addRule();

        summaryTable.setPaddingLeft(1);
        summaryTable.setPaddingRight(1);
        summaryTable.getRenderer().setCWC(cwc);
        System.out.println(summaryTable.render());

        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("Name", "Alloc", "CAD", "Price (CAD)", "USD", "Price (USD)", "ETH", "Price (ETH)");
        table.addRule();
        for (String[] row : rows) {
            table.addRow(row);
        }
        table.addRule();

        table.setPaddingLeft(1);
        table.setPaddingRight(1);
        table.getRenderer().setCWC(cwc);
        System.out.println(table.render());
    }
}