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
    private boolean orderJustCancelled = false;  //  track if order was just canceled



    //create order if the price is below x value
    //cancel order if the price goes above x threshold
    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);
        final long initialTargetPrice = 100; // set initial target price to 100
        long currentTargetPrice = initialTargetPrice;

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        // get current best ask price
        final AskLevel nearTouch = state.getAskAt(0);  // best price on the ask side
        long bestAskPrice = nearTouch.price;

        logger.info("[MYALGO] The best ask price is: £" + bestAskPrice);

        //check if an active order at the target price exists
        var activeOrders = state.getActiveChildOrders();

        if (!activeOrders.isEmpty()) {
            var firstActiveOrder = activeOrders.stream().findFirst().get();

            // if ask price drops below target price, cancel old order
            if (bestAskPrice < currentTargetPrice && !orderJustCancelled) {
                logger.info("[ADDCANCELALGO] Market price has dropped below target. Cancelling order @ £" + currentTargetPrice);
                orderJustCancelled = true;  // set true to prevent re-creation of the order
                return new CancelChildOrder(firstActiveOrder);
            }
        }
        // if no active order exist or previous one is cancelled, create new order
        if (activeOrders.isEmpty() && !orderJustCancelled) {
            if (bestAskPrice < currentTargetPrice) {
                currentTargetPrice = bestAskPrice;
                logger.info("[MYALGO] Creating new order @ new price: £" + currentTargetPrice);
                // create a new child order at the better price
                return new CreateChildOrder(Side.BUY, 80, currentTargetPrice);
            } else {
                // keep inital targetprice
                logger.info("[MYALGO] Creating new order @ original target price: £" + initialTargetPrice);
                return new CreateChildOrder(Side.BUY, 80, initialTargetPrice);
            }
        }

        // reset bool if no cancellations or creations happened
        if (!activeOrders.isEmpty()) {
            orderJustCancelled = false;
        }

        return NoAction.NoAction;
    }
}
