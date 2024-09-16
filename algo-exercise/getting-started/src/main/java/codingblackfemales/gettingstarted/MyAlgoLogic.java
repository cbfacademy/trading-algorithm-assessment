package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    @Override
    public Action evaluate(SimpleAlgoState state) {

        logger.info("[MYALGO] In Algo Logic....");

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        /********
         *
         * Add your logic here....
         *
         */
        final BidLevel nearTouch = state.getBidAt(0);
        long quantity = nearTouch.quantity;
        long price = nearTouch.price;

        if (state.getChildOrders().size() < 4) {

            logger.info("[MyALGO] Have:" + state.getChildOrders().size()
                    + " children, want 4, joining my side of book with: " + quantity + " @ " + price);
            return new CreateChildOrder(Side.BUY, quantity, price);

        } else {
            logger.info("[MyALGO] Have:" + state.getChildOrders().size() + " children, want 4, done.");
            return NoAction.NoAction;
        }

    }
}
