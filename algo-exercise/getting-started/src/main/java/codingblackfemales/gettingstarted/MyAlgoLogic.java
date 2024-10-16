package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.container.AlgoContainer;
import codingblackfemales.sequencer.net.Consumer;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import codingblackfemales.algo.AddCancelAlgoLogic;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    protected AlgoContainer container;
    protected SimpleAlgoState state;

    @Override
    public Action evaluate(SimpleAlgoState state) {
        final Action action;
        logger.info("[MYALGO] In Algo Logic....");

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        final BidLevel nearTouch = state.getBidAt(0);
        long quantity = nearTouch.quantity;
        long price = nearTouch.price;

        if (state.getChildOrders().size() < 3) {

            logger.info("[MyALGO] Have:" + state.getChildOrders().size()
                    + " children, want 3, joining my side of book with: " + quantity + " @ " + price);
            return action = new CreateChildOrder(Side.BUY, quantity, price);

        } else {
            logger.info("[MyALGO] Have:" + state.getChildOrders().size() + " children, want 4, done.");
            action = NoAction.NoAction;
        }
        return action;
        // cancelling child order

        /*
         * final var activeOrders = state.getChildOrders();
         * 
         * if (activeOrders.size() > 4) {
         * 
         * final var option = activeOrders.stream().findFirst();
         * 
         * if (option.isPresent()) {
         * var childOrder = option.get();
         * logger.info("[ADDCANCELALGO] Cancelling order:" + childOrder);
         * return new CancelChildOrder(childOrder);
         * } else {
         * return NoAction.NoAction;
         * }
         * }
         * 
         * return action;
         * }
         * 
         * // checking filled quantity
         * public void manageFilledOrders(SimpleAlgoState state) {
         * for (ChildOrder order : state.getChildOrders()) {
         * long filledQuantity = order.getFilledQuantity();
         * 
         * if (filledQuantity == 225) {
         * order.setState(2); // Set the state to filled
         * logger.info("Order " + order.getOrderId() +
         * " is fully filled with quantity: " + filledQuantity);
         * } else if (filledQuantity > 0 && filledQuantity < order.getQuantity()) {
         * order.setState(1); // Set the state to partially filled
         * logger.info("Order " + order.getOrderId() +
         * " is partially filled with quantity: " + filledQuantity);
         * } else {
         * order.setState(0); // Set the state to open
         * logger.info("Order " + order.getOrderId() + " is still open.");
         * }
         * }
         */
    }
}
