package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

public class MyAlgoLogic implements AlgoLogic {

        private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

        // Fixed entry price for stop-loss calculation
        private long fixedEntryPrice = 0;
        // Current best price for stop-loss comparison
        private long currentBestPrice = 0;

        private final Queue<Long> bidPricesOverTime = new LinkedList<>();
        private static final int maxPricesStored = 5;  // Number of prices to calculate SMA
        private double currentSMA = 0;

        @Override
        public Action evaluate(SimpleAlgoState state) {

            var orderBookAsString = Util.orderBookToString(state);
            logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

            final BidLevel highestBidPrice = state.getBidAt(0);
            final AskLevel highestAskPrice = state.getAskAt(0);
            long quantity = 75;
            long entryPrice = highestBidPrice.price;  // Current best price for the decision
            double stopLossPrice = fixedEntryPrice * 0.98; // Based on the initial entry price
            double profitTarget = fixedEntryPrice * 1.00033; // 0.033% profit target

            // Update current best price
            currentBestPrice = entryPrice;

            logger.info("[ADDCANCELALGO] In Algo Logic....");

            // Update queue with new prices
            if (bidPricesOverTime.size() >= maxPricesStored) {
                bidPricesOverTime.remove();  // Remove the oldest price
            }
            bidPricesOverTime.add(entryPrice); // Add the new price

            // Calculate SMA if enough data is available
            if (bidPricesOverTime.size() == maxPricesStored) {
                double sum = 0;
                for (long price : bidPricesOverTime) {
                    sum += price;
                }
                currentSMA = sum / maxPricesStored;
                logger.info("[MYALGO] Calculated SMA: " + currentSMA);
            } else {
                logger.info("[MYALGO] Not enough prices to calculate SMA.");
                return NoAction.NoAction;
            }

            var totalOrderCount = state.getChildOrders().size();
            final var activeOrders = state.getActiveChildOrders();
            logger.info("[MYALGO] Active orders count: " + activeOrders.size());

            // Exit condition: stop if total order count exceeds a certain limit or no bid levels
            if (totalOrderCount >= 20 || state.getBidLevels() == 0) {
                logger.info("[MYALGO] Exit condition met. No further actions.");
                return NoAction.NoAction;
            }

            // Stop-loss logic: cancel order if the price falls below stop-loss level
            if (!activeOrders.isEmpty()) {
                var firstOrder = activeOrders.stream().findFirst().orElse(null);
                if (firstOrder != null && currentBestPrice <= stopLossPrice) {
                    logger.info("[MYALGO] Stop-loss triggered at " + stopLossPrice + ". Cancelling order: " + firstOrder);
                    return new CancelChildOrder(firstOrder);
                }
            }

            // Buy order logic: place order if entry price is below SMA and no active orders
            if (entryPrice <= currentSMA && activeOrders.size() < 3) {
                logger.info("[MYALGO] Placing buy order for " + quantity + " @ " + entryPrice + " (Price <= SMA: " + currentSMA + ")");
                // Set the fixed entry price when the first buy is placed
                if (fixedEntryPrice == 0) {
                    fixedEntryPrice = entryPrice;
                }
                return new CreateChildOrder(Side.BUY, quantity, entryPrice);
            }

            // Sell logic: place sell order if the profit target is reached
            if (!activeOrders.isEmpty() && entryPrice > profitTarget) {
                AskLevel topAsk = state.getAskAt(0);
                logger.info("[MYALGO] Selling to take profit at " + entryPrice);
                return new CreateChildOrder(Side.SELL, topAsk.getQuantity(), topAsk.getPrice());
            }

            return NoAction.NoAction;
        }
    }
