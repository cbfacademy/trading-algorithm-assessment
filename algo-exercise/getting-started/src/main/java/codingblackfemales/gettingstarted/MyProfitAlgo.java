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

// 8TH AND FINAL CODE - GOOD CODE
// TRADING ALGO THAT CREATES AND CANCELS AN ORDER BASED ON THE SPREAD NARROWING AND EXPANDING
// THE ALGO CAN ALSO MAKE A PROFIT BY BUYING SHARES WHEN PRICES ARE LOW AND SELLING THEM WHEN PRICES ARE HIGH
// IF THE SPREAD NARROWS TO OR BELOW THE SET THRESHOLD CREATE UP TO 5 ORDERS FOR 200 SHARES PER ORDER,
// IF THE SPREAD WIDENS TO OR ABOVE THE SET THRESHOLD, CANCEL ANY UNFILLED OR PARTIALLY FILLED ORDERS
// IF THE BID AND ASK PRICES GO ABOVE YOUR PREVIOUS BOUGHT PRICE BY A CERTAIN THRESHOLD, SELL THE SHARES FOR A PROFIT


public class MyProfitAlgo implements AlgoLogic {
    private static final Logger logger = LoggerFactory.getLogger(MyProfitAlgo.class);

    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[PROFITALGO] The state of the order book is:\n" + orderBookAsString);

        /** Algo **/

        // Get the best bid and best ask levels     
        BidLevel bestBid = state.getBidAt(0);
        AskLevel bestAsk = state.getAskAt(0);

        // Get the best bid and ask prices and their corresponding quantities
        final long bestBidPrice = bestBid.getPrice();
        final long bestAskPrice = bestAsk.getPrice();
        final long bestBidQuantity = bestBid.getQuantity();
        final long bestAskQuantity = bestAsk.getQuantity();
        final long buyQuantity = 200L;

        // Define a threshold for the spread (in price points)
        final long spread = Math.abs(bestAskPrice - bestBidPrice); // added 5/10/2024 to get absolute value of a number
        final long spreadThreshold = 3L;

        // Log the bid-ask information
        logger.info("[PROFITALGO] Best bid: " + bestBidQuantity + " @ " + bestBidPrice + " Best ask: " + bestAskQuantity + " @ " + bestAskPrice);   
        
        // Retrieve the list of active child orders
        var activeChildOrders = state.getActiveChildOrders();


        // TO CREATE CHILD ORDERS
        // If there are less than 5 active child orders, and the spread has narrowed to the defined threshold,
        // create a buy limit order at the best BID price and defined buy quantity

        // if (activeChildOrders.isEmpty()) { // created 1
        if (activeChildOrders.size() < 5) {

            if (spread <= spreadThreshold) {
                    logger.info("[PROFITALGO] BUY CONDITIONS - Spread is " + spread + " points.");
                    logger.info("BUY CONDITIONS - [PROFITALGO] has " + activeChildOrders.size() + " active child orders. " +
                                " Spread is below threshold. Creating a buy order for " + buyQuantity + " units @ " + bestBidPrice);
                return new CreateChildOrder(Side.BUY, buyQuantity, bestBidPrice);
            }
            else {
                // Log if the spread does not meet the threshold
                logger.info("[PROFITALGO] BUY CONDITIONS - Spread is " + spread + " points.");
                logger.info("[PROFITALGO] BUY CONDITIONS - Spread is above the buying threshold. No buy order created.");
            }
        }      
        else {
            // If there are already 5 active child orders, log it and take no action
            logger.info("BUY CONDITIONS - [PROFITALGO] has " + activeChildOrders.size() + " active child orders. No new orders will be created.");
        }             
        

        // CONDITIONS TO CANCEL A BUY ORDER        

        long orderId;
        Side orderSide;        
        long quantityOrdered;        
        long orderPrice;
        long orderFilledQuantity;
        long unfilledQuantity;


        // Define a price reversal threshold (in price points)            
        long priceReversalThreshold = 10L;  // was 10L          

        // amended code - now cancelling partially filled or unfilled orders
        if (!activeChildOrders.isEmpty()) {                      

            for (ChildOrder order : activeChildOrders) {
                if (order.getSide() == Side.BUY) {
                    orderId = order.getOrderId();
                    orderSide = order.getSide();
                    quantityOrdered = order.getQuantity();                    
                    orderPrice = order.getPrice();
                    orderFilledQuantity = order.getFilledQuantity();

                        // Check if the order is partially filled or not filled at all
                        if (orderFilledQuantity < quantityOrdered) {

                                // Calculate the remaining unfilled quantity
                                unfilledQuantity = quantityOrdered - orderFilledQuantity;

                                // Log the details of the unfilled or partially filled orders
                                logger.info("[PROFITALGO] CANCEL CONDITIONS - Partially filled or Non filled order found. " +
                                            " Order ID: " + orderId +
                                            " Side: " + orderSide +
                                            ", Ordered Qty: " + quantityOrdered +
                                            ", Price: " + orderPrice +                                            
                                            ", Filled Qty: " + orderFilledQuantity  +
                                            ", Unfilled Qty: " + unfilledQuantity);

                                // Cancel the unfilled part of the order if the ASK price moves up by or above the defined threshold
                                if (bestAskPrice >= (orderPrice + priceReversalThreshold)) {
                                        logger.info("[PROFITALGO] CANCEL CONDITIONS - price reversal threshold is " + priceReversalThreshold + " points.");
                                        logger.info("[PROFITALGO] CANCEL CONDITIONS - Ask price moved against buy order. " +
                                                    " Cancelling order ID: " + orderId +
                                                    ", Unfilled Qty: " + (unfilledQuantity));
                                    return new CancelChildOrder(order);
                                }
                                else {
                                    // If the ask price has not moved beyond the threshold, log that the order remains active
                                    logger.info("[PROFITALGO] CANCEL CONDITIONS  - Ask price has not moved above threshold. Buy order to remain active");
                                }
                        }
                        else {
                            // Code to log if there are no unfilled quantities
                            logger.info("[PROFITALGO] CANCEL CONDITIONS - No orders need cancelling. No action taken.");
                            }
                }                     
                else {
                    // if it was not a buy order, log this and take no action
                    logger.info("[PROFITALGO] CANCEL CONDITIONS -  This was not a buy order. No action taken.");
                }

            }

        }  

        // CONDITIONS TO SELL THE SHARES FOR A PROFIT
        // if current best bid price and best ask price is above my average bought price + 2
        // create a sell order at best ask price for previous filled quantity

        // add code to get the previous traded price from filled buy orders. calculate an average of buy price
        // there are multiple orders filled at different prices, calculate an average for the filled quantities
        // average price at which I bought the shares, then use that price to sell shares
        // get the filled quantity that I bought at (side.BUY), sum the filled quantity by buy orders,

        // cancel all the unfilled buy orders before you start selling
        // add some code to cancel all buy orders before start selling
        // for every child order, if side == BUY, cancel the child order
        // else - conditions to create a sell order if the sell conditions are met  
        
        if (!activeChildOrders.isEmpty()) {
            for (ChildOrder childOrder : activeChildOrders) {
                if (childOrder.getSide() == Side.SELL) {  
                        logger.info("[PROFITALGO] SELL CONDITIONS - A sell order exists. Take no action.");
                    return NoAction.NoAction;  
                }
            }
            logger.info("[PROFITALGO] SELL CONDITIONS - The list has no sell orders. Continue with step to sell");
        }
        
        // using Java Streams to calculate the average traded price for buy trades filtered by filled quantities for buy orders
        final long averageBoughtPrice = (long) state.getChildOrders().stream()
                                .filter(order -> order.getSide() == Side.BUY)
                                .filter(order -> order.getFilledQuantity() > 0)
                                .mapToLong(ChildOrder::getPrice) // map to the price of the buy order
                                .average()
                                .orElse(0L); // Default to 0 if there are no valid orders

        // final long previousFilledQuantity = 501;
        // get the total filled quantity filtered by filled buy orders
        final long buyOrdersFilledQuantity = state.getChildOrders().stream()
                                .filter(order -> order.getSide() == Side.BUY)
                                .filter(order -> order.getFilledQuantity() > 0)
                                .map(ChildOrder::getFilledQuantity)
                                .reduce(Long::sum)
                                .orElse(0L); 

        final long sellQuantityThreshold = 100;
        final long sellQuantity = buyOrdersFilledQuantity; ///5;

        if (!activeChildOrders.isEmpty()) { 

            if (averageBoughtPrice > 0 && buyOrdersFilledQuantity > sellQuantityThreshold) {
        
                if ((bestBidPrice > (averageBoughtPrice + 2)) && (bestAskPrice > (averageBoughtPrice + 2 ))) {
                        logger.info("[PROFITALGO] SELL CONDITIONS - bestBidPrice: "+ bestBidPrice + " |bestAskPrice " + bestAskPrice +
                                    " | previousBoughtPrice: " + averageBoughtPrice );
                        logger.info("[PROFITALGO] SELL CONDITIONS - Market has moved up from my previous buy price " +
                                    "Selling my shares for " + sellQuantity + " units @ " + bestAskPrice);
                    return new CreateChildOrder(Side.SELL, sellQuantity, bestAskPrice);
                }            

                else {

                    // If the Bid and Ask price has not moved beyond the threshold. No order created
                    logger.info(" [PROFITALGO] SELL CONDITIONS - bestBidPrice: "+ bestBidPrice + " |bestAskPrice " + bestAskPrice +
                                " | averageBoughtPrice: " + averageBoughtPrice );
                    logger.info("[PROFITALGO] SELL CONDITIONS - Price has not moved above threshold. Do not trade");
                } 

            }
            
        }
  
        // If there are no active child orders, return no action
        return NoAction.NoAction;
    
    }

}