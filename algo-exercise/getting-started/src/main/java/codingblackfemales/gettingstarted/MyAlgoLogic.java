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

import java.util.Iterator;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    // Thresholds for buy and sell actions
    private static long buyThreshold = 90; // Minimum acceptable bid price for a buy order
    private static  long sellThreshold = 115; // Minimum acceptable ask price for a sell order
    private static  long spreadThreshold = -3; // Minimum spread threshold for action



    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

//shows current state of order book and current state of active orders
        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);
        logger.info("Active Orders:" + state.getActiveChildOrders().toString());

        var totalOrderCount = state.getChildOrders().size();

        //make sure we have an exit condition...
        if (totalOrderCount > 10) {
            return NoAction.NoAction;
        }



        final BidLevel bidlevel = state.getBidAt(0);
        final long bestBidPrice = bidlevel.price;
        final long bidQuantity = bidlevel.quantity;



        final AskLevel asklevel = state.getAskAt(0);
        final long bestAskPrice = asklevel.price;
        final long askQuantity = asklevel.quantity;


        //calculate the spread
        final long spread = bestAskPrice - bestBidPrice;
        logger.info("[MYALGO] Spread between ask and bid " + spread);

        //calculate the midPrice
        final long midPrice =(bestBidPrice + bestBidPrice)/2;
        logger.info("[MYALGO] Mid-price calculated:" + midPrice);

        //Calculate the spread as a percentage of the mid-price
        final double spreadPercentage = (double) spread / midPrice * 100;
        logger.info("[MYALGO] Spread percentage: " + spreadPercentage + "%");

        // Check for matching orders
        matchOrders(state, bestBidPrice, bestAskPrice);



        // not to create or cancel orders because spread is small
        if (spread < spreadThreshold) {
            logger.info("[MYALGO] The spread is small " + spread + " No Action");
            return NoAction.NoAction;

        }


//count the buy and sell orders
        long buyOrdersCount = state.getChildOrders().stream().filter(ChildOrder -> ChildOrder.getSide() == Side.BUY).count();
        long sellOrdersCount = state.getChildOrders().stream().filter(ChildOrder -> ChildOrder.getSide() == Side.SELL).count();



        //create new buy orders if there are less than 5 orders
        if (buyOrdersCount < 5 ) {
            return createBuyOrder(state, buyOrdersCount, bidQuantity, bestBidPrice);
        }

        //create new sell orders if there are less than 5 order
        if (sellOrdersCount < 5 ) {
            return createSellOrder(state, sellOrdersCount, askQuantity, bestAskPrice);
        }

        //cancel orders that don't match the best price

        return cancelOrders(state, bestBidPrice, bestAskPrice);
    }

    private void matchOrders(SimpleAlgoState state, long bestBidPrice, long bestAskPrice) {
        Iterator<ChildOrder> iterator = state.getActiveChildOrders().iterator();

        while (iterator.hasNext()) {
            ChildOrder order = iterator.next();
            if (order.getSide() == Side.BUY && order.getPrice() >= bestAskPrice) {
                logger.info("[MYALGO] Matched BUY order: Order ID: #" + order.getOrderId() +", Side: " + order.getSide() + ", Price: " + order.getPrice() + ", Quantity: " + order.getQuantity());
                iterator.remove(); // Remove matched order
            } else if (order.getSide() == Side.SELL && order.getPrice() <= bestBidPrice) {
                logger.info("[MYALGO] Matched SELL order: Order ID: #" + order.getOrderId() +", Side: " + order.getSide() + ", Price: " + order.getPrice() + ", Quantity: " + order.getQuantity());
                iterator.remove(); // Remove matched order
            }
        }
    }





    //create method to create new buy order
    public Action createBuyOrder(SimpleAlgoState state, long buyOrdersCount, long bidQuantity, long bestBidPrice) {
        logger.info("[MYALGO] Creating new buy order: " + state.getChildOrders().size() + " orders and add to new buy order " + bidQuantity + " @ " + bestBidPrice);
        logger.info(state.getActiveChildOrders().toString());
        logger.info("Buy order count is:" + buyOrdersCount);
        //creates a new child order
        return new CreateChildOrder(Side.BUY, bidQuantity, bestBidPrice);

    }

    // create a method to create a new sell order
    public Action createSellOrder(SimpleAlgoState state, long sellOrdersCount, long askQuantity, long bestAskPrice) {
        logger.info("[MYALGO] Creating new sell order:" + state.getChildOrders().size() + " orders and add to new sell order " + askQuantity + " @ " + bestAskPrice);
        logger.info("Sell order count is:" + sellOrdersCount);
        //creates a new child order
        return new CreateChildOrder(Side.SELL, askQuantity, bestAskPrice);
    }

    //create a method to cancel orders that don't match best price or fall below thresholds
    public Action cancelOrders(SimpleAlgoState state, long bestBidPrice, long bestAskPrice){
        for (ChildOrder order : state.getActiveChildOrders()){
            logger.info("Order ID: #" + order.getOrderId() + ", Side: " + order.getSide() + ", Price: " + order.getPrice() + ", Quantity: " + order.getQuantity());

            boolean buyOrder = order.getSide() ==Side.BUY;
            boolean notBuyThreshold = order.getPrice() != buyThreshold;
            boolean lessThanBuyThreshold = order.getPrice() < buyThreshold;

            // Cancel buy orders not matching the best price or below the buy threshold
            if  (buyOrder && (notBuyThreshold || lessThanBuyThreshold)) {
                logger.info(String.format("The buy Threshold is %d", + buyThreshold ));
                logger.info(String.format("[MYALGO] Cancel BUY order %d with price %d and quantity %d. The current best bid price is %d." ,+ order.getOrderId(), + order.getPrice(), + order.getQuantity(), + bestBidPrice));
                return new CancelChildOrder(order);
            }
            boolean sellTheOrder = order.getSide() ==Side.SELL;
            boolean notSellThreshold = order.getPrice() != sellThreshold;
            boolean lessThanSellThreshold = order.getPrice() < sellThreshold;


            // Cancel sell orders not matching the best price or below the sell threshold
            if (sellTheOrder && (notSellThreshold || lessThanSellThreshold)) {
                logger.info(String.format("The sell Threshold is %d", + sellThreshold ));
                logger.info(String.format("[MYALGO] Cancel SELL order %d with price %d and quantity %d. The current best ask price is %d." ,+ order.getOrderId(), + order.getPrice(), + order.getQuantity(), + bestAskPrice));
                return new CancelChildOrder(order);
            }
        }

        return NoAction.NoAction;
    }
}




