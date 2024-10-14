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
        final AskLevel highestAskPrice = state.getAskAt(0);
        long quantity = 75;
        long entryPrice = highestBidPrice.price;
        long bestAskPrice = highestAskPrice.price;
        double stopLossPrice = entryPrice * 0.98;
        double profitTarget = entryPrice * 1.00033; // Profit target is 0.033%

        // First ensure algo updates the queue properly:
        if (bidPricesOverTime.size() >= maxPricesStored) {
            bidPricesOverTime.remove();
        } else {
            bidPricesOverTime.add(entryPrice);

        }

        // Calculate SMA for my logic
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

        final var activeOrders = state.getActiveChildOrders();

// Combined check for max orders and bid levels (exit condition)
        if (state.getChildOrders().size() > maxOrderCount || state.getBidLevels() == 0) {
            return NoAction.NoAction;
        } else {
            //cancel action for existing child order in case of bear market
            if (activeOrders.size() > 0 && entryPrice <= stopLossPrice) {
                logger.info("[MYALGO] BEARISH TREND DETECTED. Stop-loss triggered at " + stopLossPrice + ". Cancelling any existing child order.");

                final var option = activeOrders.stream().findFirst();

                if (option.isPresent()) {
                    var childOrder = option.get();
                    logger.info("[ADDCANCELALGO] Cancelling order:" + childOrder);
                    return new CancelChildOrder(childOrder);
                } else {
                    return NoAction.NoAction;
                }
            } else { //buy action
                if (entryPrice <= currentSMA && state.getChildOrders().size() < 3) {
                    logger.info("[MYALGO] Have:" + state.getChildOrders().size() + " children, buying: " + quantity + " @ " + entryPrice);
                    return new CreateChildOrder(Side.BUY, quantity, entryPrice);
                }

//sell action
                if (currentSMA >= profitTarget && state.getChildOrders().size() >= 3) {
                    logger.info("[MYALGO] Have: " + state.getChildOrders().size() + " children, selling: " + quantity + " @ " + bestAskPrice);
                    return new CreateChildOrder(Side.SELL, quantity, bestAskPrice);
                }
            }

            return NoAction.NoAction;
        }

        // Default return if no action is triggered
        //  return NoAction.NoAction;


    }

}

    /** Method for selling based on profit target
    private Action createSellOrder(long bestBidPrice, SimpleAlgoState state) {
        BidLevel level = state.getBidAt(0);

        //it then collects the price and qty at that level:
        long entryPrice = level.price;

        double profitTarget = entryPrice * 1.00033; // Profit target is 0.033%

        if (entryPrice > 0 && bestBidPrice >= profitTarget) {
            AskLevel topAsk = state.getAskAt(0);

            logger.info("[MYALGO] Profit target reached. Selling to take profit at " + bestBidPrice);
            logger.info("[MYALGO] Order book after sell order looks like this:\n" + Util.orderBookToString(state));

          //  entryPrice = 0; // Reset entry price after selling
            return new CreateChildOrder(Side.SELL, topAsk.getQuantity(), topAsk.getPrice());
        } else {
            return NoAction.NoAction;
        } */


