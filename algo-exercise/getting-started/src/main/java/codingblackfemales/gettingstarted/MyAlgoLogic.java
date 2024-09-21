package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import java.util.stream.Collectors;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    private static final String ANSI_RED = "\033[31m";  // Red color for cancellations
    private static final String ANSI_GREEN = "\033[32m";  // Green color for successful actions
    private static final String ANSI_RESET = "\033[0m";  // Reset to default color
    private static final String ANSI_BLUE = "\u001B[34m";



    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info(ANSI_BLUE + "[MYALGO] The state of the order book is:\n"  + orderBookAsString + ANSI_RESET);
        logger.info("########### CURRENT ORDERS: " + state.getActiveChildOrders().toString());
        var totalOrderCount = state.getChildOrders().size();
        //make sure we have an exit condition...
        if (totalOrderCount > 10) {
            return NoAction.NoAction;
        }

        final BidLevel bidLevel = state.getBidAt(0);
        final long bidPrice = bidLevel.price; // VALUES ARE THE SAME
        final long bidQuantity = bidLevel.quantity;
       // final long bestBidPrice = state.getBidAt(0).price; // get the current Bidprice // VALUES ARE THE SAME(will remove)

        final AskLevel askLevel = state.getAskAt(0);
        final long askPrice = askLevel.price;
        final long askQuantity = askLevel.quantity;
       //final long bestAskPrice = state.getAskAt(0).price; will remove
        final long OrderAskPrice =75;

        // logger.info("[MYALGO] Current order size is :" + currentOrderSize );

        // separate child orders into BUY AND SELL
        long buyOrdersCount = state.getChildOrders().stream().filter(ChildOrder -> ChildOrder.getSide() == Side.BUY).count();
        long sellOrdersCount = state.getChildOrders().stream().filter(ChildOrder -> ChildOrder.getSide() == Side.SELL).count();

        // create new BUY order
        if (buyOrdersCount < 5) {
            logger.info(String.format(ANSI_GREEN + "[MYALGO] Have: " + buyOrdersCount + " orders. ADD New BUY ORDER " + bidQuantity + " @ " + bidPrice + ANSI_RESET));
            //  int initialOrderSize = state.getChildOrders().size();// capture current size of order before creating new one
            logger.info(state.getActiveChildOrders().toString());
            return new CreateChildOrder(Side.BUY, bidQuantity, bidPrice); // Create a new BUY order
        }

        // create new SELL order
        if (sellOrdersCount<5){//create new sell order if fewer than 5 child order exist
            logger.info(String.format(ANSI_GREEN + "[MYALGO] Have: " + sellOrdersCount + " orders. ADD New ASK ORDER " + askQuantity + " @ " + askPrice + ANSI_RESET));
            int initialOrderSize = state.getChildOrders().size();// capture current size of order before creating new one
            return new CreateChildOrder(Side.SELL, askQuantity, askPrice); // Create a new child order
        }

        // Cancel the  First  BUY order with a price less than the BestBid or not matching the Bestbid.
        for (ChildOrder order : state.getActiveChildOrders()) {
            logger.info("Order ID#####: " + order.getOrderId() + ", Side: " + order.getSide() + ", Price: " + order.getPrice());

            if (order.getSide() == Side.BUY && ((order.getPrice() != bidPrice) || (order.getPrice() < bidPrice))) {
                logger.info(String.format(ANSI_GREEN  +"[MYALGO] Cancel BUY order #%d with price %d and quantity %d. The current best price is %d." + ANSI_RESET, order.getOrderId(), order.getPrice(), order.getQuantity(), bidPrice));
                logger.info(state.getActiveChildOrders().toString());
                return new CancelChildOrder(order);

            }

            // Cancel the  First  SELL order with a price less than the BestAsk or not matching the Bestsk.

            if (order.getSide() == Side.SELL && ((order.getPrice() != OrderAskPrice) || (order.getPrice() < OrderAskPrice))) {
                logger.info(String.format(ANSI_RED + "[MYALGO] Cancel SELL order #%d with price %d and quantity %d. The current best price is %d." + ANSI_RESET, order.getOrderId(), order.getPrice(), order.getQuantity(), OrderAskPrice));
                logger.info(state.getActiveChildOrders().toString());
                return new CancelChildOrder(order);

            }
        }
        //If no actions are taken, return NoAction
        return NoAction.NoAction;
    }

}


















