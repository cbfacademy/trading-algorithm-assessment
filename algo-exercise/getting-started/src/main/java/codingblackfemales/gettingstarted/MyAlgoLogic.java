package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    private long currentTargetPrice = 100; // set initial target price to 100
    private static final long SPREAD_THRESHOLD = 5; // spread threshold
    private static final long PRICE_THRESHOLD = 3; // price movement threshold

    @Override
    public Action evaluate(SimpleAlgoState state) {

        // safely handle the order book state logging
        var orderBookAsString = Util.orderBookToString(state);
        if (orderBookAsString != null) {
            logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);
        } else {
            logger.warn("[MYALGO] Order book data is missing or incomplete.");
        }

        // make sure we have an exit condition if there are too many child orders
        if (state.getChildOrders().size() > 20) {
            return NoAction.NoAction;
        }

        // get current best ask and bid price safely
        final AskLevel bestAskLevel = state.getAskAt(0);
        final long bestBidPrice = state.getBidAt(0) != null ? state.getBidAt(0).price : 0;
        if (bestAskLevel == null || bestBidPrice == 0) {
            logger.warn("[MYALGO] Best ask or bid price is null, no action will be taken.");
            return NoAction.NoAction;
        }

        long bestAskPrice = bestAskLevel.price;
        logger.info("[MYALGO] The best ask price is: £" + bestAskPrice);
        logger.info("[MYALGO] The best bid price is: £" + bestBidPrice);

        // calculate spread and check if it's within the acceptable range
        long spread = bestAskPrice - bestBidPrice;
        if (spread > SPREAD_THRESHOLD) {
            logger.info("[MYALGO] Spread is too wide: £" + spread + ". No action taken.");
            return NoAction.NoAction;
        }

        // get active orders
        var activeOrders = state.getActiveChildOrders();
        logger.info("[MYALGO] Active child orders: " + activeOrders.size());

        // if less than 6 active orders exist, create more orders if the best ask price differs from the current target price
        if (activeOrders.size() < 6 && bestAskPrice != currentTargetPrice) {
            currentTargetPrice = bestAskPrice; // Update current target price to best ask price
            logger.info("[MYALGO] Creating new order @ new price: £" + bestAskPrice);
            return new CreateChildOrder(Side.BUY, 80, bestAskPrice);
        }

        // cancel orders if market has moved unfavorably based on the threshold
        if (!activeOrders.isEmpty()) {
            for (var activeOrder : activeOrders) {
                long orderPrice = activeOrder.getPrice();

                // cancel the order if the price has moved by more than the PRICE_THRESHOLD
                if (Math.abs(bestBidPrice - orderPrice) > PRICE_THRESHOLD) {
                    logger.info("[MYALGO] Canceling order with price: £" + orderPrice + " because the price moved by more than the threshold.");
                    return new CancelChildOrder(activeOrder);
                }
            }
        }

        return NoAction.NoAction;
    }
}
