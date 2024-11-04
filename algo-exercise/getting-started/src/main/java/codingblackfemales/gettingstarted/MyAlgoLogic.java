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

/*Welcome to my High Frequency Trading Algorithm. This algorithm assesses the markets liquidity or volatility  the via
 the spread to determine whether conditions are prime to buy or sell. If spread percentage is under the set target limit is
recorded the algo will utilise a High Frequency Trading indicator named Volume imbalance and a VWAP benchmark to access market
conditions to create child buy orders matching the price and volume of best bid and sell shares at best ask with a volume of 20%
of the total market volume traded. When the target spread is not met active child orders will be cancelled depending on whether they
 meet the best bid or ask.
 */



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
        var totalOrderCount = state.getChildOrders().size();

        //make sure we have an exit condition...
        if (totalOrderCount > 20) {
            return NoAction.NoAction;
        }


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

        final var filledStateOrders = state.getActiveChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED).toList().size();
        final int minimumFilledOrderQuantity = 2;
        final double targetSpreadLimit = 4.5; //preferably would be set to under 1% when executing high frequency trading strategies
        /*based on research a spread of less than 1% is preferred in real life markets when executing high frequency trading strategies.
        However, to accommodate the order book data provided I have set a target spread limit of 4.5%*/


        if (bidAskSpreadPercentage < targetSpreadLimit) {
            logger.info("[MYALGO] Bid-Ask Spread percentage is less than " + targetSpreadLimit + "%. Market is liquid. Evaluating child orders...");
            childOrderLog(state);
            profitTracker(state);


            if (filledStateOrders < minimumFilledOrderQuantity) {//Before creating competitive buy or sell child orders a VWAP benchmark calculated from two a minimum of  filled orders needs to be attained
                logger.info("[MYALGO] Have: " + filledStateOrders + " filled orders. Adding aggressive buy order: " + bestAskQuantity + " @ " + bestAskPrice);
                return new CreateChildOrder(Side.BUY, bestAskQuantity, bestAskPrice);
            } else {
                logger.info("[MYALGO] Achieved " + minimumFilledOrderQuantity + " Filled Order minimum. Evaluating conditions for competitive orders...");
            }


            String volumeImbalanceOutput = volumeImbalanceIndicator(state);//called in the same places as the vwap? should we keep as variable

            double VWAP = calculateVWAP(state);



            final var buyOrders = state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.BUY)).toList();
            final var sellOrders = state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.SELL)).toList();


            logger.info("[MYALGO] VWAP calculated as: " + String.format("%.2f", VWAP)+ ", Volume Imbalance indicating: " + volumeImbalanceOutput);
            boolean buyOrder = false;
            boolean sellOrder = false;


            if (filledBuyStateSize(state) < 5 && (bestBidPrice <= VWAP && volumeImbalanceOutput.contains("NEGATIVE"))) {
                logger.info("[MYALGO] Have " + buyOrders.size() + " buy orders. VWAP and Volume Imbalance conditions have been met. Adding buy order for " + bestBidQuantity + " @ " + bestBidPrice);
                buyOrder = true; //create buy child order if volume imbalance indicates as negative as it indicates an imminent drop in midpoint due to increased selling pressure.

            }
            if (calculateSharesRemainingToSell(state) > 0 && (bestAskPrice >= VWAP && volumeImbalanceOutput.contains("POSITIVE"))) {
                logger.info("[MYALGO] Have " + sellOrders.size() + " sell orders. VWAP and Volume Imbalance conditions have been met. Adding sell order for " + sellVolumeInline(state,20.0) + " @ " + bestAskPrice);
                sellOrder = true; //create sell child order if volume imbalance indicates as positive as it indicates an imminent increase in midpoint due to increased buying pressure.
            }
            if (buyOrder) {
                return new CreateChildOrder(Side.BUY, bestBidQuantity, bestBidPrice);
            }
            if (sellOrder) {
                return new CreateChildOrder(Side.SELL, sellVolumeInline(state,20.0), bestAskPrice);
            }
            logger.info("[MYALGO] Conditions for Buy or Sell orders have not been met. No action taken.");
            return NoAction.NoAction;

        } else {
            logger.info("[MYALGO] Bid-Ask Spread percentage is greater than " + targetSpreadLimit + "%. Market is volatile. Evaluating active child orders for cancellation...");

            childOrderLog(state);
            profitTracker(state);
            for (ChildOrder orders : state.getActiveChildOrders()) {//will cancel the first non-viable active child older if not meeting best bid and ask due to no longer being competitive
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
        logger.info("[MYALGO] No non-viable orders, no action taken.");
        return NoAction.NoAction;

    }

    protected double bidAskSpreadPercentage(SimpleAlgoState state) {
        final AskLevel ask = state.getAskAt(0);
        long bestAskPrice = ask.price;

        final BidLevel bid = state.getBidAt(0);
        long bestBidPrice = bid.price;
        return (double) (bestAskPrice - bestBidPrice) / (bestAskPrice) * 100;

    }

    protected double calculateVWAP(SimpleAlgoState state) {


        double totalQuantity = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED).mapToDouble(ChildOrder::getFilledQuantity).sum();
        double totalPriceQuantity =  state.getChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED).mapToDouble(order -> order.getFilledQuantity() * order.getPrice()).sum();


        return totalQuantity == 0 ? 0 : totalPriceQuantity / totalQuantity;

    }

    protected double calculateVolumeImbalance(SimpleAlgoState state) {
        final AskLevel ask = state.getAskAt(0);
        final BidLevel bid = state.getBidAt(0);

        return (double) (bid.quantity - ask.quantity) / (bid.quantity + ask.quantity);
    }


    private String analyzedVolumeImbalance(double volumeImbalance) {

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

    private String volumeImbalanceIndicator(SimpleAlgoState state) {
        return analyzedVolumeImbalance(calculateVolumeImbalance(state));
    }


    private void childOrderLog(SimpleAlgoState state) {
        final var pendingBuyState = state.getActiveChildOrders().stream().filter(order -> order.getState()==OrderState.PENDING && order.getSide()==Side.BUY).toList();
        final var pendingSellState = state.getActiveChildOrders().stream().filter(order -> order.getState()==OrderState.PENDING && order.getSide()==Side.SELL).toList();
        final var filledSellState =state.getActiveChildOrders().stream().filter(order -> order.getState()==OrderState.FILLED && order.getSide()==Side.SELL).toList();
        final var cancelledState = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.CANCELLED).toList();
        logger.info("[MYALGO] Child Order Log Count: Filled Buy=" + filledBuyStateSize(state)+ ", Filled Sell=" + filledSellState.size()+", Pending Buy=" + pendingBuyState.size() + ", Pending Sell=" + pendingSellState.size() +", Cancelled=" + cancelledState.size());
    }

    private int filledBuyStateSize(SimpleAlgoState state) {
        return state.getActiveChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED && order.getSide() == Side.BUY).toList().size();

    }

    private long calculateTotalExecutedVolume(SimpleAlgoState state) {
        return state.getActiveChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED).mapToLong(ChildOrder::getFilledQuantity).sum();

    }

    private long calculateSharesRemainingToSell(SimpleAlgoState state) {
        long filledBuyVolume = state.getActiveChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED && order.getSide() == Side.BUY).mapToLong(ChildOrder::getFilledQuantity).sum();
        long filledSellVolume = state.getActiveChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED && order.getSide() == Side.SELL).mapToLong(ChildOrder::getFilledQuantity).sum();
        return filledSellVolume == 0 ? filledBuyVolume : filledBuyVolume - filledSellVolume;//unnecessary possibly?

    }

    protected long sellVolumeInline(SimpleAlgoState state, double participationRate) {
        double actualParticipationRate= participationRate /(100-participationRate);//adjusting participation rate to account for own trading
        long volumeInlineSellQuantity = (long) (calculateTotalExecutedVolume(state) * actualParticipationRate);
        return Math.min(volumeInlineSellQuantity, calculateSharesRemainingToSell(state));
// in the case where there are not enough shares to trade at 20% inline of the market volume the quantity of the remaining shares will be returned
    }

    private void profitTracker(SimpleAlgoState state) {
        long filledBuyVolume = state.getActiveChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED && order.getSide() == Side.BUY).mapToLong(ChildOrder::getFilledQuantity).sum();
        long filledSellVolume = state.getActiveChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED && order.getSide() == Side.SELL).mapToLong(ChildOrder::getFilledQuantity).sum();
        double totalBuyPriceQuantity =  state.getActiveChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED && order.getSide() == Side.BUY).mapToDouble(order -> order.getFilledQuantity() * order.getPrice()).sum();
        double totalSellPriceQuantity = state.getActiveChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED && order.getSide() == Side.SELL).mapToDouble(order -> order.getFilledQuantity() * order.getPrice()).sum();
        double profit = totalSellPriceQuantity - totalBuyPriceQuantity;
        logger.info("[MYALGO] Revenue Tracker: Spent=£" + totalBuyPriceQuantity + ", Sold=£" + totalSellPriceQuantity +", Profit=£" + profit + ", Shares Acquired=" +filledBuyVolume +  ", Shares Sold=" + filledSellVolume +  ", Shares Remaining=" +  calculateSharesRemainingToSell(state));
    }
}









