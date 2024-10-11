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

        var orderBookAsString = Util.orderBookToString(state);
        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        //make sure we have an exit condition...
        if (state.getChildOrders().size() > 20) {
            return NoAction.NoAction;
        }

        // get current best ask price
        final AskLevel nearTouch = state.getAskAt(0);  // best price on the ask side
        long bestAskPrice = nearTouch.price;

        logger.info("[MYALGO] The best ask price is: £" + bestAskPrice);
        //check active orderS
        var activeOrders = state.getActiveChildOrders();

        // if less than 3 active orders exist, create more orders if the best ask price differs from the current target price (to avoid creating orders at the same price)
        if (activeOrders.size() < 3  && bestAskPrice != currentTargetPrice) {
            currentTargetPrice = bestAskPrice; // Update current target price to best ask price
            logger.info("[MYALGO] Creating new order @ new price: £" + bestAskPrice);
                return new CreateChildOrder(Side.BUY, 80, bestAskPrice);
            }

        // If active orders exist,evaluate whether it makes sense to cancel them depending on how the market has changed
            if (!activeOrders.isEmpty()) {
                var firstActiveOrder = activeOrders.stream().findFirst().get();
                // If ask price drops below target price, cancel the existing order
                if (bestAskPrice < currentTargetPrice) {
                    logger.info("[ADDCANCELALGO] Market price has dropped below target. Cancelling order @ £" + currentTargetPrice);
                    return new CancelChildOrder(firstActiveOrder);
                }
            }

            return NoAction.NoAction;
        }
    }