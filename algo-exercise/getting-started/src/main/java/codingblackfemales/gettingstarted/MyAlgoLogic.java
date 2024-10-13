package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);


    //creating a queue to store the latest 5 bid prices to calculate the SMA:
    private Queue<Long> bidPricesOverTime = new LinkedList<>();

    private static final int maxPricesStored = 5;
    private double currentSMA = 0;
    private double maxOrderCount = 20;


    @Override
    public Action evaluate(SimpleAlgoState state) {
        var orderBookAsString = Util.orderBookToString(state);
        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        final BidLevel highestBidPrice = state.getBidAt(0);
        long quantity = 75;
        long entryPrice = highestBidPrice.price;
        double stopLossPrice = entryPrice * 0.98;

        // First ensure algo updates the queue properly:
        if (bidPricesOverTime.size() >= maxPricesStored) {
            bidPricesOverTime.remove();
        }
        // Then always add the latest price
        bidPricesOverTime.add(entryPrice);

// Combined check for max orders and bid levels
        if (state.getChildOrders().size() > maxOrderCount || state.getBidLevels() == 0) {
            return NoAction.NoAction;
        }

        final var activeOrders = state.getActiveChildOrders();
        if (activeOrders.size() > 0  && entryPrice <= stopLossPrice) {
            logger.info("[MYALGO] BEARISH TREND DETECTED. Stop-loss triggered at " + stopLossPrice + ". Cancelling any existing buy order.");

            final var option = activeOrders.stream().findFirst();

            if (option.isPresent()) {
                var childOrder = option.get();
                logger.info("[ADDCANCELALGO] Cancelling order:" + childOrder);
                return new CancelChildOrder(childOrder);
            }

            /**    final var activeOrders = state.getActiveChildOrders();

             // First, evaluate the stop-loss logic regardless of buy logic
             if (entryPrice <= stopLossPrice) {
             logger.info("[MYALGO] BEARISH TREND DETECTED. Stop-loss triggered at " + stopLossPrice + ". Cancelling any existing buy order.");
             if (!activeOrders.isEmpty()) {
             var childOrder = activeOrders.stream().findFirst();
             if (childOrder.isPresent()) {
             return new CancelChildOrder(childOrder.get());
             }
             }
             } */

            // Then proceed with the buy logic independently
            if (activeOrders.size() < 3) {
                // Calculate SMA only if necessary for buy logic
                if (bidPricesOverTime.size() == maxPricesStored) {
                    double sum = 0;
                    for (long price : bidPricesOverTime) {
                        sum += price;
                    }
                    currentSMA = sum / maxPricesStored;
                    logger.info("[MYALGO] Calculated SMA for Algo Logic: " + currentSMA);
                } else {
                    logger.info("[MYALGO] Not enough prices collected to calculate SMA. No further action taken.");
                    return new NoAction();
                }

                // Check if we should place a new buy order
                if (entryPrice <= currentSMA && state.getChildOrders().size() < 3) {
                    logger.info("[MYALGO] Have:" + state.getChildOrders().size() + " children, joining passive side of book with: " + quantity + " @ " + entryPrice);
                    return new CreateChildOrder(Side.BUY, quantity, entryPrice);
                }
            }

            // Default return if no action is triggered
            return NoAction.NoAction;
        }
        return NoAction.NoAction;
    }
}