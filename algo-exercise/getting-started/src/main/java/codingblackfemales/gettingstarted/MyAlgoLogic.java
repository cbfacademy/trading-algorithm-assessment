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

    //create order if the price is below x value
    //cancel order if the price goes above x threshold
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

        // get current best ask price safely
        final AskLevel nearTouch = state.getAskAt(0);  // best price on the ask side
        if (nearTouch == null) {
            logger.warn("[MYALGO] The best ask price is null, no action will be taken.");
            return NoAction.NoAction;
        }

        long bestAskPrice = nearTouch.price;
        logger.info("[MYALGO] The best ask price is: £" + bestAskPrice);

        // get active orders
        var activeOrders = state.getActiveChildOrders();

        // if less than 3 active orders exist, create more orders if the best ask price differs from the current target price (to avoid creating orders at the same price)
        if (activeOrders.size() < 3 && bestAskPrice != currentTargetPrice) {
            // check if there's still a remaining quantity available at the target price
            if (state.getAskLevels() > 0 && state.getAskAt(0).price == currentTargetPrice) {
                // Avoid creating a new order if there's already a remaining quantity at the same price
                logger.info("[MYALGO] Order exists at the current target price: £" + currentTargetPrice + ". Skipping new order creation.");
            } else {
                currentTargetPrice = bestAskPrice; // Update current target price to best ask price
                logger.info("[MYALGO] Creating new order @ new price: £" + bestAskPrice);
                return new CreateChildOrder(Side.BUY, 80, bestAskPrice);
            }
        }
        // If active orders exist,evaluate whether it makes sense to cancel them depending on how the market has changed
        if (!activeOrders.isEmpty()) {
            var firstActiveOrder = activeOrders.stream().findFirst().get();
            // If ask price drops below target price, cancel the existing order
            if (bestAskPrice < currentTargetPrice) {
                logger.info("[MYALGO] Market price has dropped below target. Cancelling order @ £" + currentTargetPrice);
                return new CancelChildOrder(firstActiveOrder);
            } else {
                logger.info("[MYALGO] Best ask price has not dropped below target. No cancellation.");
            }
        }
            return NoAction.NoAction;
        }
    }