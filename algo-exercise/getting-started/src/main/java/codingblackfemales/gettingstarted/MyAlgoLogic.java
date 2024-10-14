package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.OrderState;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        /********
         *
         * Add your logic here....
         * check child order size < 5
         *
         *
         */
        final AskLevel ask = state.getAskAt(0);
        long bestAskQuantity = ask.quantity;
        long bestAskPrice = ask.price;

        final BidLevel bid = state.getBidAt(0);
        long bestBidQuantity = bid.quantity;
        long bestBidPrice = bid.price;


        logger.info("[MYALGO] Best Bid: Quantity = " + bid.quantity + ", Price = " + bid.price);
        logger.info("[MYALGO] Best Ask: Quantity = " + ask.quantity + ", Price = " + ask.price);


        double bidAskSpreadPercentage = bidAskSpreadPercentage(state);
        logger.info("[MYALGO] Bid-ask spread percentage is: " + String.format("%.2f", bidAskSpreadPercentage) + "%");
        final int minimumFilledOrderQuantity = 2;
        final double bidAskSpreadThreshold = 4.5;//normally less than 1% from research based on this order book set to 4.5



        if (bidAskSpreadPercentage < bidAskSpreadThreshold) {
            logger.info("[MYALGO] Bid-Ask Spread percentage is less than " + bidAskSpreadThreshold + "%. Market is liquid. Evaluating child orders...");
            childOrderLog(state);

            int filledStateSize = filledStateSize(state);


//            if (state.getChildOrders().size() < 2) {//this line works for unit test on version one
           if (filledStateSize < 2) {//this line only works for the back test
                logger.info("[MYALGO] Have: " + filledStateSize + " filled orders. Adding aggressive buy order: " + bestAskQuantity + " @ " + bestAskPrice);
                return new CreateChildOrder(Side.BUY, bestAskQuantity, bestAskPrice);
            } else {
                logger.info("[MYALGO] Achieved " + minimumFilledOrderQuantity + " Filled Order minimum. Evaluating conditions for competitive orders...");
            }


            String volumeImbalanceOutput = volumeImbalanceIndicator(state);

            long VWAP = calculateVWAP(state);


            final var buyOrders = state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.BUY)).toList();
            final var sellOrders = state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.SELL)).toList();

            logger.info("[MYALGO] VWAP calculated as: " + VWAP + ", Volume Imbalance indicating: " + volumeImbalanceOutput);
            boolean createBuyOrder = false;
            boolean createSellOrder = false;
            if (buyOrders.size() < 5 && (bestBidPrice <= VWAP && volumeImbalanceOutput.contains("NEGATIVE"))) {//create buy order if volume imbalance is negative as it indicates an imminent drop in midpoint due to increased selling pressure.
                logger.info("[MYALGO] Have " + buyOrders.size() + " buy orders. VWAP or Volume Imbalance conditions have been met. Adding buy order for " + bestBidQuantity + " @ " + bestBidPrice);
                createBuyOrder = true;
            }
            if (sellOrders.size() < 5 && (bestAskPrice >= VWAP && volumeImbalanceOutput.contains("POSITIVE"))) {//create sell order if volume imbalance is positive as it indicates an imminent increase in midpoint due to increased buying pressure.
                logger.info("[MYALGO] Have " + sellOrders.size() + " sell orders. VWAP or Volume Imbalance conditions have been met. Adding sell order for " + bestAskQuantity + " @ " + bestAskPrice);
                createSellOrder = true;
            }
            if (createBuyOrder) {
                return new CreateChildOrder(Side.BUY, bestBidQuantity, bestBidPrice);
            }
            if (createSellOrder) {
                return new CreateChildOrder(Side.SELL, bestAskQuantity, bestAskPrice);
            }
            logger.info("[MYALGO] Conditions for Buy or Sell orders have not been met. No action taken.");
            return NoAction.NoAction;

        } else {
            logger.info("[MYALGO] Bid-Ask Spread percentage is greater than " + bidAskSpreadThreshold + "%. Market is volatile. Evaluating active child orders for cancellation...");

            childOrderLog(state);
            for (ChildOrder orders : state.getActiveChildOrders()) {
                if (orders.getSide().equals(Side.BUY) && orders.getState() != OrderState.FILLED && orders.getPrice() != bestBidPrice) {
                    logger.info("[MYALGO] Cancelling non-viable buy order: ID = " + orders.getOrderId() + ", Quantity = " + orders.getQuantity() + ", Price = " + orders.getPrice());
                    return new CancelChildOrder(orders);

                }
                if (orders.getSide().equals(Side.SELL) && orders.getState() != OrderState.FILLED && orders.getPrice() != bestAskPrice) {
                    logger.info("[MYALGO] Cancelling non-viable sell order : ID = " + orders.getOrderId() + ", Quantity = " + orders.getQuantity() + ", Price = " + orders.getPrice());
                    logger.info(state.getActiveChildOrders().toString());
                    return new CancelChildOrder(orders);
                }
            }
        }
        logger.info("[MYALGO] No viable orders, no action taken.");
        return NoAction.NoAction;

    }

    protected double bidAskSpreadPercentage(SimpleAlgoState state) {
        final AskLevel ask = state.getAskAt(0);
        long bestAskPrice = ask.price;

        final BidLevel bid = state.getBidAt(0);
        long bestBidPrice = bid.price;
        return (double) (bestAskPrice - bestBidPrice) / (bestAskPrice) * 100;

    }

    protected long calculateVWAP(SimpleAlgoState state) {
        final var filledState = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED).toList();

        long quantity = 0;
        long totalPriceQuantity = 0;

        for (ChildOrder order : filledState) {
            quantity += order.getFilledQuantity();
            totalPriceQuantity += order.getQuantity() * order.getPrice();

        }
        return quantity == 0 ? 0 : totalPriceQuantity / quantity;

    }

    protected double calculateVolumeImbalance(SimpleAlgoState state) {
        final AskLevel ask = state.getAskAt(0);
        final BidLevel bid = state.getBidAt(0);

        return (double) (bid.quantity - ask.quantity) / (bid.quantity + ask.quantity);
    }


    private String analyzedVolumeImbalance(double volumeImbalance){

        String formattedVolumeImbalance = String.format("%.2f", volumeImbalance);

        if (volumeImbalance >= -1 && volumeImbalance <= -0.3) {
            return "NEGATIVE(" + formattedVolumeImbalance + "), POSSIBLE SELL PRESSURE";

        } else if (volumeImbalance > -0.3 && volumeImbalance < 0.3) {
            return "NEUTRAL(" + formattedVolumeImbalance + ")";

        } else if (volumeImbalance >= 0.3 && volumeImbalance <= 1) {
            return "POSITIVE(" + formattedVolumeImbalance + "), POSSIBLE BUY PRESSURE";

        } else {
            return "INVALID";
        }

    }
    private String volumeImbalanceIndicator(SimpleAlgoState state){
        double volumeImbalance = calculateVolumeImbalance(state);
        return analyzedVolumeImbalance(volumeImbalance);
    }


    private void childOrderLog(SimpleAlgoState state) {
        final var pendingState = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.PENDING).toList();
        final var filledState = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED).toList();
        final var cancelledState = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.CANCELLED).toList();
        final var buyOrders = state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.BUY)).toList();
        final var sellOrders = state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.SELL)).toList();
        final var filledQuantity = state.getChildOrders().stream().filter(order -> order.getFilledQuantity() > 0).count();
        logger.info("filled count " + filledQuantity);
        logger.info("[MYALGO] Child Order Log Count: Filled=" + filledState.size() + " ,Pending=" + pendingState.size() + " ,Cancelled=" + cancelledState.size()+ " ,Buy =" + buyOrders.size()+ " ,Sell=" + sellOrders.size());
    }

    private int filledStateSize(SimpleAlgoState state) {
        final var filledState = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED).toList();

        return filledState.size();
    }
}







