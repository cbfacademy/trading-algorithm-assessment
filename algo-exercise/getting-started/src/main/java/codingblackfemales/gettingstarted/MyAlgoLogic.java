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

/*This Algo is a market making strategy that
Monitors the bid-ask spread
Places buy and sell orders to profits from the spread when it's favorable
Cancel orders that are not aligned with the best market price to ensure it remains competitive and maintains orders to close to the optimal prices
 *
 */


public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    private static final String ANSI_RED = "\033[31m";  // Red color for cancellations
    private static final String ANSI_GREEN = "\033[32m";  // Green color for successful actions
    private static final String ANSI_RESET = "\033[0m";  // Reset to default color
    private static final String ANSI_BLUE = "\u001B[34m";


    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info(ANSI_BLUE + "[MYALGO] The state of the order book is:\n" + orderBookAsString + ANSI_RESET);
        logger.info("########### CURRENT ORDERS: " + state.getActiveChildOrders().toString());

        var totalOrderCount = state.getChildOrders().size();

        // make sure we have an exit condition...
        if (totalOrderCount > 10) {
            return NoAction.NoAction;
        }

        final BidLevel bidLevel = state.getBidAt(0);
        final long bestBidPrice = bidLevel.price;
        final long bidQuantity = bidLevel.quantity;

        final AskLevel askLevel = state.getAskAt(0);
        final long bestAskPrice = askLevel.price;
        final long askQuantity = askLevel.quantity;
        final long askPriceThreshold = 75;
        final long spreadThreshold = -8; // use spreadthreshold to make decision

        // calculate the spread
        final long spread = bestAskPrice - bestBidPrice;
        logger.info(String.format(ANSI_GREEN + "[MYALGO] Spread (AsK - Bid): " + spread + ANSI_RESET));

        // Don't place or cancel orders if spread is too small
        if (spread < spreadThreshold) {
            logger.info(String.format(ANSI_GREEN + "MYALGO Spread is too small " + spread + " No Action taken"));
            return NoAction.NoAction;
        }

        // separate child orders into BUY AND SELL
        long buyOrdersCount = state.getChildOrders().stream().filter(ChildOrder -> ChildOrder.getSide() == Side.BUY).count();
        long sellOrdersCount = state.getChildOrders().stream().filter(ChildOrder -> ChildOrder.getSide() == Side.SELL).count();

        // create new BUY order if fewer than 5 BUY orders exist
        if (buyOrdersCount < 5) {
            return createBuyOrder(state, buyOrdersCount, bidQuantity, bestBidPrice);
        }

        // create new SELL order if fewer than  5 SELL orders exist
        if (sellOrdersCount < 5) {
            return createSellOrder(state, sellOrdersCount, askQuantity, bestAskPrice);
        }

       // cancel orders that don't match the best price
        return cancelOrders(state, bestBidPrice, askPriceThreshold);
    }

    public Action createBuyOrder (SimpleAlgoState state, long buyOrdersCount, long bidQuantity, long bestBidPrice){

        // ALL IN NEW METHOD CALLED createBuyOrder
        logger.info(String.format(ANSI_GREEN + "[MYALGO] Have " + state.getChildOrders().size() + " orders. ADD NEW BUY ORDER " + bidQuantity + " @ " + bestBidPrice + ANSI_RESET));
        // int initialOrderSize = state.getChildOrders().size();// capture current size of order before creating new one
        logger.info(state.getActiveChildOrders().toString());
        logger.info("BUY ORDER COUNT: "  + buyOrdersCount );
        return new CreateChildOrder(Side.BUY, bidQuantity, bestBidPrice); // Create a new BUY order
    }

    public Action createSellOrder (SimpleAlgoState state, long sellOrdersCount, long askQuantity, long bestAskPrice){
        // ALL IN NEW METHOD CALLED createBuyOrder
        logger.info(String.format(ANSI_GREEN + "[MYALGO] Have " + state.getChildOrders().size() + " orders. ADD NEW ASK ORDER " + askQuantity + " @ " + bestAskPrice + ANSI_RESET));
        //int initialOrderSize = state.getChildOrders().size();// capture current size of order before creating new one
        logger.info("SELL ORDER COUNT: "  + sellOrdersCount);
        return new CreateChildOrder(Side.SELL, askQuantity, bestAskPrice); // Create a new child order
    }

    public Action cancelOrders(SimpleAlgoState state,long bestBidPrice, long askPriceThreshold){
        //Cancel the First BUY order with a price less than the BestBid or not matching the Bestbid.

        for (ChildOrder order : state.getActiveChildOrders()) {
            logger.info("Order ID: #" + order.getOrderId() + ", Side: " + order.getSide() + ", Price: " + order.getPrice() + ", Quantity " + order.getQuantity());

            boolean orderIsBuy = order.getSide() == Side.BUY;
            boolean orderNotBestBidPrice = order.getPrice() != bestBidPrice;
            boolean orderLessThanBestBid = order.getPrice() < bestBidPrice;

            if (orderIsBuy && (orderNotBestBidPrice || orderLessThanBestBid)) {
                // MOVE TO NEW METHOD cancelBuyOrder(state, order, bestBidPrice)
                logger.info(String.format(ANSI_RED + "[MYALGO] Cancel BUY order #%d with price %d and quantity %d. The current best price is %d." + ANSI_RESET, order.getOrderId(), order.getPrice(), order.getQuantity(), bestBidPrice));
                logger.info(state.getActiveChildOrders().toString());
                return new CancelChildOrder(order);

            }

            boolean orderIsSell = order.getSide() == Side.SELL;
            boolean orderNotThreshold = order.getPrice() != askPriceThreshold;
            boolean orderLessThanThreshold = order.getPrice() < askPriceThreshold;

            // Cancel the  First SELL order with a price less than the BestAsk or not matching the Bestask.
            if (orderIsSell && ((orderNotThreshold) || (orderLessThanThreshold))) {
                logger.info(String.format(ANSI_RED + "[MYALGO] Cancel SELL order #%d with price %d and quantity %d. The current best price is %d." + ANSI_RESET, order.getOrderId(), order.getPrice(), order.getQuantity(), askPriceThreshold));
                logger.info(state.getActiveChildOrders().toString());
                return new CancelChildOrder(order);

            }
        }
        //If no actions are taken, return NoAction
        return NoAction.NoAction;

    }


}
















