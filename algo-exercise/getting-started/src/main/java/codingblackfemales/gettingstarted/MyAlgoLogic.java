package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AbstractLevel;
import codingblackfemales.util.Util;
import messages.order.Side;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    
    private int marketDataTickCount = 0;
    private int priceDifferentiator = 0;

    // DATA ABOUT THE CURRENT MARKET TICK

    // variables to store data from the current tick
    private AbstractLevel bestBidOrderInCurrentTick;
    private long bestBidPriceInCurrentTick;

    private long totalQuantityOfBidOrdersInCurrentTick; // from top 10 orders

    // lists to store data from multiple orders in the current tick
     private List<Long> quantitiesOfTopBidOrdersInCurrentTick = new ArrayList<>(); // from top 10 ask orders


    // getters to obtain data from current tick
    public AbstractLevel getBestBidOrderInCurrentTick() {
        return bestBidOrderInCurrentTick;
    }

    public long getBestBidPriceInCurrentTick() {
        return bestBidPriceInCurrentTick;
    }
    

    public List<Long> getQuantitiesOfTopBidOrdersInCurrentTick() { // from (up to) the top 10 bid price levels
        return quantitiesOfTopBidOrdersInCurrentTick;
    }

    private long setTotalQuantityOfBidOrdersInCurrentTick() { // from (up to) the top 10 bid price levels
        return totalQuantityOfBidOrdersInCurrentTick = sumOfAllInAListOfLongs(getQuantitiesOfTopBidOrdersInCurrentTick());
    }

    public long getTotalQuantityOfBidOrdersInCurrentTick() { // to compare with total ask quantity to see if there is a buy or sell pressure
        return totalQuantityOfBidOrdersInCurrentTick;
    }

    // variable to cap items of data to analyse
    int MAX_ITEMS_OF_DATA = 10;


    // method to populate lists of longs capped at 10 items
    private void addToAListOfLongs(List<Long> list, long num) {
        list.add(num);
        if (list.size() > MAX_ITEMS_OF_DATA) {
            list.remove(0); // remove oldest piece of data
        }
    }

    // method to calculate sum of all in a list of longs
    private long sumOfAllInAListOfLongs(List<Long> list) { 
        return list.stream().reduce(Long::sum).orElse(0L);
    }

    // DATA ABOUT MY ALGO'S CHILD ORDERS

    // BUY SIDE 
    private long childBidOrderQuantity;

    private void setChildBidOrderQuantity() {
        childBidOrderQuantity = 100; // initial amount, then after trades execute, set to 10% of all filled orders for POV limit
    }

    public long getChildBidOrderQuantity() {
        return childBidOrderQuantity;
    }


    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] THIS IS TICK NUMBER: " + marketDataTickCount + "\n");
        marketDataTickCount += 1;

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        // UPDATE DATA ABOUT CURRENT MARKET DATA TICK
        bestBidOrderInCurrentTick = state.getBidAt(0);
        bestBidPriceInCurrentTick = getBestBidOrderInCurrentTick().getPrice();

        // Loop to populate lists of data about the top bid orders in the current tick
        int maxBidOrders = Math.min(state.getBidLevels(), MAX_ITEMS_OF_DATA); // up to a max of 10 bid orders
        quantitiesOfTopBidOrdersInCurrentTick.clear();
        for (int i = 0; i < maxBidOrders; i++) {
            AbstractLevel bidOrder = state.getBidAt(i);
            addToAListOfLongs(quantitiesOfTopBidOrdersInCurrentTick, bidOrder.quantity);
        }

        setTotalQuantityOfBidOrdersInCurrentTick();
        setChildBidOrderQuantity();


        // CREATE / CANCEL / BID / SELL DECISION LOGIC

        if (state.getChildOrders().size() < 3) {
            priceDifferentiator += 1;
            return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), (getBestBidPriceInCurrentTick() - 3 + priceDifferentiator));
        } else {
            return NoAction.NoAction;
        }

    }
}
