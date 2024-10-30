package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AbstractLevel;
import codingblackfemales.util.Util;
import messages.order.Side;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    
    private int marketDataTickCount = 0;

    // DATA ABOUT THE CURRENT MARKET TICK

    // variables to store data from the current tick - BUY SIDE
    private AbstractLevel bestBidOrderInCurrentTick;
    private long bestBidPriceInCurrentTick;
    private long bestBidQuantityInCurrentTick;

    private long totalQuantityOfBidOrdersInCurrentTick;

    // lists to store data from (up to) top 10 price levels on BUY side in the current tick
    private List<AbstractLevel> topBidOrdersInCurrentTick = new ArrayList<>();
    private List<Long> pricesOfTopBidOrdersInCurrentTick = new ArrayList<>(); 
    private List<Long> quantitiesOfTopBidOrdersInCurrentTick = new ArrayList<>(); 


    // getters to obtain data from current tick - BUY SIDE
    public AbstractLevel getBestBidOrderInCurrentTick() {
        return bestBidOrderInCurrentTick;
    }

    public long getBestBidPriceInCurrentTick() {
        return bestBidPriceInCurrentTick;
    }
    
    public long getBestBidQuantityInCurrentTick() {
        return bestBidQuantityInCurrentTick;
    }


    public List<AbstractLevel> getTopBidOrdersInCurrentTick() {
        return topBidOrdersInCurrentTick;
    }

    public List<Long> getPricesOfTopBidOrdersInCurrentTick() { 
        return pricesOfTopBidOrdersInCurrentTick;
    }

    public List<Long> getQuantitiesOfTopBidOrdersInCurrentTick() { 
        return quantitiesOfTopBidOrdersInCurrentTick;
    }

    private long setTotalQuantityOfBidOrdersInCurrentTick() {
        return totalQuantityOfBidOrdersInCurrentTick = sumOfAllInAListOfLongs(getQuantitiesOfTopBidOrdersInCurrentTick());
    }

    public long getTotalQuantityOfBidOrdersInCurrentTick() { // to compare with total ask quantity to see if there is a buy or sell pressure
        return totalQuantityOfBidOrdersInCurrentTick;
    }


    // variables to store data from the current tick - SELL SIDE
    private AbstractLevel bestAskOrderInCurrentTick;
    private long bestAskPriceInCurrentTick;
    private long bestAskQuantityInCurrentTick;

    private long totalQuantityOfAskOrdersInCurrentTick; // from up to top 10 price levels

    // lists to store data from (up to) top 10 price levels on SELL side in the current tick
    private List<AbstractLevel> topAskOrdersInCurrentTick = new ArrayList<>(); 
    private List<Long> pricesOfTopAskOrdersInCurrentTick = new ArrayList<>(); 
    private List<Long> quantitiesOfTopAskOrdersInCurrentTick = new ArrayList<>();

    // getters to obtain data from current tick - SELL SIDE
    public AbstractLevel getBestAskOrderInCurrentTick() { 
        return bestAskOrderInCurrentTick;
    }
    
    public long getBestAskPriceInCurrentTick() {
        return bestAskPriceInCurrentTick;
    }

    public long getBestAskQuantityInCurrentTick() {
        return bestAskQuantityInCurrentTick;
    }

    public List<AbstractLevel> getTopAskOrdersInCurrentTick() {
        return topAskOrdersInCurrentTick;
    }

    public List<Long> getPricesOfTopAskOrdersInCurrentTick() { // top 10
        return pricesOfTopAskOrdersInCurrentTick;
    }

    public List<Long> getQuantitiesOfTopAskOrdersInCurrentTick() { // top 10
        return quantitiesOfTopAskOrdersInCurrentTick;
    }

    private long setTotalQuantityOfAskOrdersInCurrentTick() { // top 10
        return totalQuantityOfAskOrdersInCurrentTick = sumOfAllInAListOfLongs(getQuantitiesOfTopAskOrdersInCurrentTick());
    }

    public long getTotalQuantityOfAskOrdersInCurrentTick() { // top 10
        return totalQuantityOfAskOrdersInCurrentTick;
    }


    // variables to store data from the current tick - THE SPREAD AND MIDPRICE
    private long theSpreadInCurrentTick;
    private double midPriceInCurrentTick;
    private double relativeSpreadInCurrentTick;

    
    public long getTheSpreadInCurrentTick() {
        return theSpreadInCurrentTick;
    }

    public double getMidPriceInCurrentTick() {
        return midPriceInCurrentTick;
    }

    public double getRelativeSpreadInCurrentTick() {
        return relativeSpreadInCurrentTick;
    }

    // booleans for analysing the spread
    private boolean tightSpread = false;
    private boolean regularSpread = false;
    private boolean wideSpread = false;
 
    // variable to cap items of data to analyse
    int MAX_ITEMS_OF_DATA = 10;


    // DATA GATHERING METHODS

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

    // methods to populate lists of orders on both sides
    private void addToAListOfOrders(List<AbstractLevel> listOfOrders, AbstractLevel order) {
        listOfOrders.add(order);
    }


    
    // Variables to set and get child order quantities and prices based on analysis

    private long childBidOrderQuantity;


    private void setChildBidOrderQuantity() {
        childBidOrderQuantity = 100; // initial amount, then after trades execute, set to 10% of all filled orders for POV limit
    }

    public long getChildBidOrderQuantity() {
        return childBidOrderQuantity;
    }


    // DATA ABOUT MY ALGO'S CHILD ORDERS

    // list of all child orders including active, inactive, filled and cancelled
    private List<ChildOrder> allChildOrdersList = new ArrayList<>();
    public List<ChildOrder> getAllChildOrdersList() {
        return allChildOrdersList;
    }

    // BUY SIDE 
    // filtered lists of child orders - BUY SIDE
    // ACTIVE CHILD BID ORDERS
    private List<ChildOrder> activeChildBidOrdersList = new ArrayList();
    private List<String> activeChildBidOrdersToStringList= new ArrayList<>(); //  for logging
    private boolean haveActiveBidOrders = false;
    private ChildOrder activeChildBidOrderWithLowestPrice = null;
    private ChildOrder activeChildBidOrderWithHighestPrice = null;

    public List<ChildOrder> getActiveChildBidOrdersList() {
        return activeChildBidOrdersList;
    }

    public List<String> getActiveChildBidOrdersToStringList() { // for logging
        return activeChildBidOrdersToStringList;
    }

    public boolean getHaveActiveBidOrders() {
        return haveActiveBidOrders;
    }

    public ChildOrder getActiveChildBidOrderWithLowestPrice() {
        return activeChildBidOrderWithLowestPrice;
    }


    public ChildOrder getActiveChildBidOrderWithHighestPrice() {
        return activeChildBidOrderWithHighestPrice;
    }


    // FILLED CHILD BID ORDERS
    // HashSet to prevent duplication in list of filled and part filled orders list
    private Set<ChildOrder> bidOrdersMarkedAsFilledOrPartFilled = new HashSet<>();
    private List<ChildOrder> filledAndPartFilledChildBidOrdersList = new ArrayList();
    private List<String> filledAndPartFilledChildBidOrdersListToString = new ArrayList(); // for logging
    private boolean haveFilledBidOrders = false;
    private long totalFilledBidQuantity;

    private long totalExpenditure;
    private long averageEntryPrice;
    private long numOfSharesOwned;
    private long stopLoss; 


    public List<ChildOrder> getFilledAndPartFilledChildBidOrdersList() { 
        return filledAndPartFilledChildBidOrdersList;
    }

    public List<String> getFilledAndPartFilledChildBidOrdersListToString() {
        return filledAndPartFilledChildBidOrdersListToString;
    }
    
    private void setTotalFilledBidQuantity() {
        totalFilledBidQuantity = getFilledAndPartFilledChildBidOrdersList().stream()
        .mapToLong(ChildOrder::getFilledQuantity)
        .sum();
    }

    public long getTotalFilledBidQuantity() {
        return totalFilledBidQuantity;
    }

    private void setTotalExpenditure() {
        totalExpenditure = getFilledAndPartFilledChildBidOrdersList().stream()
            .mapToLong(order -> (order.getFilledQuantity() * order.getPrice()))
            .sum();
    }
    
    
    public long getTotalExpenditure() {
        return totalExpenditure;
    }

    private void setAverageEntryPrice() {  
        averageEntryPrice = (long) Math.ceil(getFilledAndPartFilledChildBidOrdersList().stream()
        .mapToLong(order -> order.getFilledQuantity() * order.getPrice())
        .sum() / getTotalFilledBidQuantity());
    }

    public long getAverageEntryPrice() {
        return averageEntryPrice;
    }

    private void setStopLoss() {
        stopLoss = (long) Math.ceil(getAverageEntryPrice() * 0.99);
    }

    public double getStopLoss() { // top 10 // TODO - TEST THIS METHOD
        return stopLoss;
    }

    // SELL SIDE 
    // filtered lists of child orders - SELL SIDE
    // ACTIVE CHILD ASK ORDERS
    private List<ChildOrder> activeChildAskOrdersList = new ArrayList<>();
    List<String> activeChildAskOrdersListToString = new ArrayList<>(); // for logging
    private boolean haveActiveAskOrders = false;
    private ChildOrder activeChildAskOrderWithHighestPrice = null;
    private ChildOrder activeChildAskOrderWithLowestPrice = null;

    public List<ChildOrder> getActiveChildAskOrdersList() {
        return activeChildAskOrdersList;
    }

    public List<String> getActiveChildAskOrdersListToString() {
        return activeChildAskOrdersListToString;
    }

    public boolean getHaveActiveAskOrders() {
        return haveActiveAskOrders;
    }

    private String activeChildAskOrderWithHighestPriceToString = ""; // for logging

    public ChildOrder getActiveChildAskOrderWithHighestPrice() {
        return activeChildAskOrderWithHighestPrice;
    }

    public ChildOrder getActiveChildAskOrderWithLowestPrice() {
        return activeChildAskOrderWithLowestPrice;
    }


    private long targetChildAskOrderPrice;

    private void setTargetChildAskOrderPrice() {
        targetChildAskOrderPrice = (long) Math.ceil(getAverageEntryPrice() * 1.03);
    }

    public long getTargetChildAskOrderPrice() {
        return targetChildAskOrderPrice;
    }

    // FILLED CHILD ASK ORDERS
    // HashSet to prevent duplication in list of filled and part filled orders list
    private Set<ChildOrder> askOrdersMarkedAsFilledOrPartFilled = new HashSet<>();
    private List<ChildOrder> filledAndPartFilledChildAskOrdersList = new ArrayList<>();
    List<String> filledAndPartFilledChildAskOrdersListToString = new ArrayList<>(); // for logging
    private long totalFilledAskQuantity;
    private boolean haveFilledAskOrders = false;
    private long totalRevenue;

    public boolean getHaveFilledAskOrders(){
        return haveFilledAskOrders;
    }

    public List<ChildOrder> getFilledAndPartFilledChildAskOrdersList() { // TODO - unit test
        return filledAndPartFilledChildAskOrdersList;
    }

    private void setTotalFilledAskQuantity() {
        totalFilledAskQuantity = getFilledAndPartFilledChildAskOrdersList().stream()
        .mapToLong(ChildOrder::getFilledQuantity)
        .sum();
    }

    public long getTotalFilledAskQuantity() { // TODO - TEST THIS METHOD
        return totalFilledAskQuantity;
    }

    private void setNumOfSharesOwned() {
        numOfSharesOwned = getTotalFilledBidQuantity() - getTotalFilledAskQuantity();
    }

    public long getNumOfSharesOwned() {  // TODO - TEST THIS METHOD
        return numOfSharesOwned;
    }

    // boolean for if we own shares
    private boolean haveShares = false;

    public boolean getHaveShares(){
        return haveShares;
    }

    private void setTotalRevenue() {
        totalRevenue = getFilledAndPartFilledChildAskOrdersList().stream()
            .mapToLong(order -> (order.getFilledQuantity() * order.getPrice()))
            .sum();
    }
    
    public long getTotalRevenue() { //TODO test this method
        return totalRevenue;
    }
    
    private long totalProfitOrLoss;

    private void setTotalProfitOrLoss() {
        totalProfitOrLoss = getTotalRevenue() - getTotalExpenditure();
    }
    
    public long getTotalProfitOrLoss() { // top 10 // TODO - TEST THIS METHOD
        return totalProfitOrLoss;
    }

    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] THIS IS TICK NUMBER: " + marketDataTickCount + "\n");
        marketDataTickCount += 1;

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        // UPDATE DATA ABOUT CURRENT MARKET DATA TICK - BUY SIDE
        bestBidOrderInCurrentTick = state.getBidAt(0);
        bestBidPriceInCurrentTick = getBestBidOrderInCurrentTick().getPrice();
        bestBidQuantityInCurrentTick = getBestBidOrderInCurrentTick().getQuantity();


        // Loop to populate lists of data about the top bid orders in the current tick
        int maxBidOrders = Math.min(state.getBidLevels(), MAX_ITEMS_OF_DATA); // up to a max of 10 bid orders
        getTopBidOrdersInCurrentTick().clear();
        getPricesOfTopBidOrdersInCurrentTick().clear();
        getQuantitiesOfTopBidOrdersInCurrentTick().clear();
        for (int i = 0; i < maxBidOrders; i++) {
            AbstractLevel bidOrder = state.getBidAt(i);
            addToAListOfOrders(getTopBidOrdersInCurrentTick(), bidOrder);
            addToAListOfLongs(getPricesOfTopBidOrdersInCurrentTick(), bidOrder.price);
            addToAListOfLongs(getQuantitiesOfTopBidOrdersInCurrentTick(), bidOrder.quantity);
        }

        setTotalQuantityOfBidOrdersInCurrentTick();
    
        // UPDATE DATA ABOUT CURRENT MARKET DATA TICK - SELL SIDE
        bestAskOrderInCurrentTick = state.getAskAt(0);
        bestAskPriceInCurrentTick = getBestAskOrderInCurrentTick().getPrice();
        bestAskQuantityInCurrentTick = getBestAskOrderInCurrentTick().getQuantity();

        // Loop to populate lists of data about the top ask orders in the current tick
        int maxAskOrders = Math.min(state.getAskLevels(), 10); // up to a max of 10 ask orders
        getTopAskOrdersInCurrentTick().clear();
        getPricesOfTopAskOrdersInCurrentTick().clear();
        getQuantitiesOfTopAskOrdersInCurrentTick().clear();
        for (int i = 0; i < maxAskOrders; i++) {
            AbstractLevel askOrder = state.getAskAt(i);
            addToAListOfOrders(getTopAskOrdersInCurrentTick(), askOrder);
            addToAListOfLongs(getPricesOfTopAskOrdersInCurrentTick(), askOrder.price);
            addToAListOfLongs(getQuantitiesOfTopAskOrdersInCurrentTick(), askOrder.quantity);
        }

        setTotalQuantityOfAskOrdersInCurrentTick();
        
        // UPDATE DATA ABOUT CURRENT MARKET DATA TICK - SPREAD AND MID PRICE
        theSpreadInCurrentTick = getBestAskPriceInCurrentTick() - getBestBidPriceInCurrentTick();
        midPriceInCurrentTick = (getBestAskPriceInCurrentTick() + getBestBidPriceInCurrentTick()) / 2;
        relativeSpreadInCurrentTick = Math.round((theSpreadInCurrentTick / midPriceInCurrentTick * 100) * 100 / 100); // rounded to 2dp
    
        // ANALYSING THE SPREAD
        if (getRelativeSpreadInCurrentTick() < 2) {
            tightSpread = true;
        } else if (getRelativeSpreadInCurrentTick() >= 2 && getRelativeSpreadInCurrentTick() <= 3) {
                regularSpread = true;
        } else {
            wideSpread = true;
        };
    

        // UPDATE DATA ABOUT MY ALGO'S CHILD ORDERS

        // update list of all child orders
        allChildOrdersList = state.getChildOrders();

        // UPDATE DATA ABOUT MY ALGO'S CHILD ORDERS - BUY SIDE

        // Update list of active child bid orders
        activeChildBidOrdersToStringList.clear();  // for logging
        activeChildBidOrdersList = state.getActiveChildOrders().stream()
            .filter(order -> order.getSide() == Side.BUY)
            .peek(order -> activeChildBidOrdersToStringList
            .add("ACTIVE CHILD BID Id:" + order.getOrderId() + " [" + order.getQuantity() + "@" + order.getPrice() + "]"))
            .collect(Collectors.toList());

        // if have active child BID orders, update the bids with the lowest and highest price
        if (!activeChildBidOrdersList.isEmpty()) {
            haveActiveBidOrders = true;
            activeChildBidOrderWithLowestPrice = activeChildBidOrdersList.stream()
                .min((order1, order2) -> Long.compare(order1.getPrice(), order2.getPrice()))
                .orElse(null);  // handle the case when min() returns an empty Optional
            activeChildBidOrderWithHighestPrice = activeChildBidOrdersList.stream()
                .max((order1, order2) -> Long.compare(order1.getPrice(), order2.getPrice()))
                .orElse(null);  // handle the case when min() returns an empty Optional
            }

        // Update list of filled child BID orders
        filledAndPartFilledChildBidOrdersList = state.getChildOrders().stream()
            .filter(order -> order.getSide() == Side.BUY && order.getFilledQuantity() > 0)
            .filter(order -> !bidOrdersMarkedAsFilledOrPartFilled.contains(order))  // Only add if not processed
            .peek(order -> bidOrdersMarkedAsFilledOrPartFilled.add(order))  // Mark as processed
            .peek(order-> filledAndPartFilledChildBidOrdersListToString // for logging
            .add("FILL/PARTFILL BID Id:" + order.getOrderId() + " [" + order.getQuantity() + "@" + order.getPrice() + "] filledQuantity: " + order.getFilledQuantity())) // TODO DELETE LATER URING DEVELOPMENT FOR BACK TESTS
            .collect(Collectors.toList());

        // if there are filled child BID Orders
        if (!filledAndPartFilledChildBidOrdersList.isEmpty()) { 
            haveFilledBidOrders = true;
            setTotalFilledBidQuantity(); // update total filled bid quantity 
            setTotalExpenditure();
            setAverageEntryPrice();
            setTargetChildAskOrderPrice();
            setStopLoss();
        }

        // UPDATE DATA ABOUT MY ALGO'S CHILD ORDERS - SELL SIDE

        // Update list of active child ASK orders
        activeChildAskOrdersListToString.clear();  // TODO delete later - only for logging now
        activeChildAskOrdersList = state.getActiveChildOrders().stream()
            .filter(order -> order.getSide() == Side.SELL)
            .peek(order -> activeChildAskOrdersListToString
            .add("ACTIVE CHILD ASK Id:" + order.getOrderId() + "[" + order.getQuantity() + "@" + order.getPrice() + "]"))
            .collect(Collectors.toList());

        // if have active child ASK orders, update the ask with the highest and lowest price
        if (!activeChildAskOrdersList.isEmpty()) {
            haveActiveAskOrders = true;
            activeChildAskOrderWithHighestPrice = activeChildAskOrdersList.stream()
                .max((order1, order2) -> Long.compare(order1.getPrice(), order2.getPrice()))
                .orElse(null);  // handle the case when max() returns an empty Optional        
            activeChildAskOrderWithLowestPrice = activeChildAskOrdersList.stream()
                .min((order1, order2) -> Long.compare(order1.getPrice(), order2.getPrice()))
                .orElse(null);  // handle the case when max() returns an empty Optional
            }

        // Update list of filled child ASK orders
        filledAndPartFilledChildAskOrdersList = state.getChildOrders().stream()
            .filter(order -> order.getSide() == Side.SELL && order.getFilledQuantity() > 0)
            .filter(order -> !askOrdersMarkedAsFilledOrPartFilled.contains(order))  // Only add if not processed
            .peek(order -> askOrdersMarkedAsFilledOrPartFilled.add(order))  // Mark as processed
            .peek(order-> filledAndPartFilledChildAskOrdersListToString // TODO DELETE LATER ONLY FOR OUTPUT DURING DEVELOPMENT FOR BACK TESTS
            .add("FILL/PARTFILL ASK Id:" + order.getOrderId() + " [" + order.getQuantity() + "@" + order.getPrice() + "] filledQuantity: " + order.getFilledQuantity())) // TODO DELETE LATER URING DEVELOPMENT FOR BACK TESTS
            .collect(Collectors.toList());

        // if there are filled ASK Orders
        if (!filledAndPartFilledChildAskOrdersList.isEmpty()) { 
            haveFilledAskOrders = true;
            setTotalFilledAskQuantity();
            setTotalRevenue();
        }

        setNumOfSharesOwned();
        setChildBidOrderQuantity();
        setTotalProfitOrLoss();

        if (getNumOfSharesOwned() > 0) {
            haveShares = true;
        }

     
        // logger.info("getChildBidOrderQuantity() is: " + getChildBidOrderQuantity());
        logger.info("getActiveChildBidOrdersToStringList() is: " + getActiveChildBidOrdersToStringList());
        logger.info("getTotalFilledBidQuantity() is: " + getTotalFilledBidQuantity());
        logger.info("getTotalExpenditure() is: " + getTotalExpenditure());
        logger.info("getAverageEntryPrice() is: " + getAverageEntryPrice());
        logger.info("getTargetChildAskOrderPrice() is: " + getTargetChildAskOrderPrice());
        logger.info("getStopLoss() is: " + getStopLoss());
        logger.info("getActiveChildAskOrdersListToString() is: " + getActiveChildAskOrdersListToString());
        logger.info("getTotalFilledAskQuantity() is: " + getTotalFilledAskQuantity());
        logger.info("getTotalRevenue() is: " + getTotalRevenue());
        logger.info("getNumOfSharesOwned() is: " + getNumOfSharesOwned());
        logger.info("getTotalProfitOrLoss() is: " + getTotalProfitOrLoss());

        // CREATE / CANCEL / BID / SELL DECISION LOGIC


        // EXIT CONDITION

        if (allChildOrdersList.size() > 8) {
            logger.info("(allChildOrdersList.size() > 8) condiition met, taking no action");
            return NoAction.NoAction;
        }

        // CANCELLING ORDERS

        // If have 0 shares left and have active ask orders, cancel all sell orders
        if ((getHaveShares() == false) && getHaveActiveAskOrders()) {
            logger.info(" ((getHaveShares() == false) && getHaveActiveAskOrders()) condition met, cancelling all sell orders");
            return new CancelChildOrder(getActiveChildAskOrdersList().get(0));
        }
        

        // If a child ask order becomes uncompetitive, cancel it
        if (getHaveActiveAskOrders() && (getActiveChildAskOrderWithHighestPrice().getPrice() >= (getBestAskPriceInCurrentTick() + 7))) {
            logger.info("(getHaveActiveAskOrders() && (getActiveChildAskOrderWithHighestPrice().getPrice() >= (getBestAskPriceInCurrentTick() + 7))) condition met, cancelling least competitive ask order");
            return new CancelChildOrder(getActiveChildAskOrderWithHighestPrice());
        }

        // If a child buy order becomes uncompetitive, cancel it
        if (getHaveActiveBidOrders() && (getActiveChildBidOrderWithLowestPrice().getPrice() <= (getBestBidPriceInCurrentTick() - 7))) {
            logger.info("(getHaveActiveBidOrders() && (getActiveChildBidOrderWithLowestPrice().getPrice() <= (getBestBidPriceInCurrentTick() - 7))) met, cancelling least competitive bid order");
            return new CancelChildOrder(getActiveChildBidOrderWithLowestPrice());
        }
        
        // PLACING ASK ORDERS
        

        if (getHaveShares()) {
                    
            // when currently have 0 active ask orders
            if (getHaveActiveAskOrders() == false) {

                // place a passive ask order at profit target price for full amount of shares owned
                logger.info("Currently own shares and have 0 active ask orders, placing an ask order at target profit price");
                return new CreateChildOrder(Side.SELL, getNumOfSharesOwned(), getTargetChildAskOrderPrice());
            }
    
            // when currently have 1 active ask order
            if (getActiveChildAskOrdersList().size() == 1) {

                if (regularSpread) {
                    // place a passive ask order above best ask order price
                    logger.info("Currently own shares, have 1 active ask order and spread is regular, placing an ask order above best ask");
                    return new CreateChildOrder(Side.SELL, getNumOfSharesOwned(), (getBestAskPriceInCurrentTick() + 1));
                }

                // If spread is wide, place a child ask order joining best ask price on order book
                if (wideSpread) {
                    logger.info("Currently own shares, have 1 active ask order and spread is wide, placing an ask order to join best ask");
                    return new CreateChildOrder(Side.SELL, (long)(getNumOfSharesOwned()), (getBestAskPriceInCurrentTick()));
                }
                // If spread is narrow, place child ask order to pay the spread to execute immediately
                if (tightSpread) {
                    logger.info("Currently own shares, have 1 active ask order and spread is narrow. Placing an at-market ask order to execute immediately");
                    return new CreateChildOrder(Side.SELL, (long)(getNumOfSharesOwned()), getBestBidPriceInCurrentTick()); 
                }
            }        
        
        }


        // PLACING BID ORDERS 
        
        // when currently have 0 active bid orders
        if (getHaveActiveBidOrders() == false) {
            // place a passive bid order below current best bid in the hope of getting a bargain
            logger.info("Currently have 0 active bid orders, placing a bid order priced 1 tick below current best bid");
            return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), (getBestBidPriceInCurrentTick() - 1));
        }

        // when currently have 1 active bid order
        if (getActiveChildBidOrdersList().size() == 1) {

            
                // if spread is wide, place a passive child order bid priced 1 tick above best bid to narrow the spread and (hopefully!) prompt trading
                if (wideSpread) {
                    logger.info("Currently have 1 active bid order and spread is wide, placing a bid order above current best bid");
                    return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), (getBestBidPriceInCurrentTick() + 1));
                }
                // if spread is regular, place a passive child bid order joining best bid price
                if (regularSpread) {
                    logger.info("Currently have 1 active bid order and spread is regular, placing a bid order joining current best bid");
                    return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), (getBestBidPriceInCurrentTick()));
                }
                // if spread is tight, place an at-market child bid order paying the spread to buy immediately
                if (tightSpread) {
                    logger.info("Currently have 1 active bid order and spread is tight, paying the spread to place an at-market bid order to execute immediately");
                    return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), (getBestAskPriceInCurrentTick()));
                }  

            }
        logger.info("No buy or sell conditions met, no Action, hold position");
        return NoAction.NoAction;
    }
}     

