package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.OrderState;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AbstractLevel;
import codingblackfemales.util.Util;
import messages.order.Side;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    
    private int evaluateMethodCallCount = 0;
    private int bidPriceDifferentiator = -3;


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

    public long getTotalQuantityOfBidOrdersInCurrentTick() { 
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

    // booleans for analysing the amount of sellers vs buyers
    private boolean sellPressure = false;
    private boolean buyPressure = false;
    private boolean marketEquilibirum = false;
     
    private String supplyAndDemandStatus = "";

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

    // ANALYSING THE SPREAD

    // enums for analysing the spread
    public enum SpreadType {
        TIGHT,
        REGULAR,
        WIDE
    }

    // variable to store the enum value 
    private SpreadType spreadType;

     // Method to analyse and set SpreadType
    private void setSpreadType() {
        double relativeSpread = getRelativeSpreadInCurrentTick();

        if (relativeSpread < 2) {
            spreadType = SpreadType.TIGHT;
        } else if (relativeSpread <= 3) {
            spreadType = SpreadType.REGULAR;
        } else {
            spreadType = SpreadType.WIDE;
        }
    }

    public SpreadType getSpreadType() {
        return spreadType;
    }

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
        // initial fixed amount of 100, then after trades execute, set to 10% of all filled orders for a POV limit
        // should be 10% of all filled orders, not just child orders but only have child orders to work with
        // if (getTotalFilledQuantityOfAllBidAndAskOrders() > 0) {
        //     childBidOrderQuantity = (long) (getTotalFilledQuantityOfAllBidAndAskOrders() * 0.1);
        // } else {
            childBidOrderQuantity = 100; 
        // }
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
    private List<String> activeChildBidOrdersListToString = new ArrayList<>(); //  for logging
    private boolean haveActiveBidOrders = false;
    private ChildOrder activeChildBidOrderWithLowestPrice = null;
    private ChildOrder activeChildBidOrderWithHighestPrice = null;

    public List<ChildOrder> getActiveChildBidOrdersList() {
        return activeChildBidOrdersList;
    }

    public List<String> getActiveChildBidOrdersListToString() { // for logging
        return activeChildBidOrdersListToString;
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
    private long trailingStopLoss; 

    public boolean getHaveFilledBidOrders(){
        return haveFilledBidOrders;
    }

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

    private void setTrailingStopLoss() {
        trailingStopLoss = (long) Math.ceil(getAverageEntryPrice() * 0.98);
    }

    public long getTrailingStopLoss() { 
        return trailingStopLoss;
    }

    // SELL SIDE 
    // filtered lists of child orders - SELL SIDE
    // ACTIVE CHILD ASK ORDERS
    private List<ChildOrder> activeChildAskOrdersList = new ArrayList<>();
    List<String> activeChildAskOrdersListToString = new ArrayList<>(); // for logging
    private boolean haveActiveAskOrders = false;
    private ChildOrder activeChildAskOrderWithHighestPrice = null;
    private ChildOrder activeChildAskOrderWithLowestPrice = null;

    private long targetChildAskOrderPrice;


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
    private long totalProfitOrLoss;
    private long VWAP;
    private boolean haveShares = false;

    public boolean getHaveFilledAskOrders(){
        return haveFilledAskOrders;
    }

    public List<ChildOrder> getFilledAndPartFilledChildAskOrdersList() { // TODO - unit test
        return filledAndPartFilledChildAskOrdersList;
    }

    public List<String> getFilledAndPartFilledChildAskOrdersListToString() { // TODO - unit test
        return filledAndPartFilledChildAskOrdersListToString;
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

    public long getNumOfSharesOwned() {  
        return numOfSharesOwned;
    }  

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

    private void setTotalProfitOrLoss() {
        totalProfitOrLoss = getTotalRevenue() - getTotalExpenditure();
    }
    
    public long getTotalProfitOrLoss() { 
        return totalProfitOrLoss;
    }

    public long getTotalFilledQuantityOfAllBidAndAskOrders() {  // TODO - TEST THIS METHOD  
        return getTotalFilledBidQuantity() + getTotalFilledAskQuantity();
    }


    private void setVWAP() {        
        VWAP = getAllChildOrdersList().stream()
            .filter(order -> order.getFilledQuantity() > 0)
            .mapToLong(order -> order.getFilledQuantity() * order.getPrice())
            .sum() / getTotalFilledQuantityOfAllBidAndAskOrders();
    }
    
    public long getVWAP() { // TODO - TEST THIS METHOD
        return VWAP;
    }

    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] THIS IS EVALUATE METHOD CALL NUMBER: " + evaluateMethodCallCount + "\n");
        evaluateMethodCallCount += 1;

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
        relativeSpreadInCurrentTick = Math.round((getTheSpreadInCurrentTick() / getMidPriceInCurrentTick() * 100) * 100 / 100); // rounded to 2dp
    
        
        setSpreadType();


        // ANALYSING SUPPLY AND DEMAND - TODO TEST THIS
        if (getTotalQuantityOfAskOrdersInCurrentTick() > (long)(getTotalQuantityOfBidOrdersInCurrentTick() * 1.5)) {
            sellPressure = true;
            supplyAndDemandStatus = "There are more sellers than buyers.";
        } else if (getTotalQuantityOfBidOrdersInCurrentTick() > (long)(getTotalQuantityOfAskOrdersInCurrentTick() * 1.5)) {
            buyPressure = true;
            supplyAndDemandStatus = "There are more buyers than sellers.";
        } else {
            marketEquilibirum = true;
            supplyAndDemandStatus = "Buyer and seller numbers are equal or relatively close.";

        };
    

        // UPDATE DATA ABOUT MY ALGO'S CHILD ORDERS

        // update list of all child orders
        allChildOrdersList = state.getChildOrders();

        // UPDATE DATA ABOUT MY ALGO'S CHILD ORDERS - BUY SIDE

        // Update list of active child bid orders
        getActiveChildBidOrdersListToString().clear(); // for logging
        getActiveChildBidOrdersList().clear();
        activeChildBidOrdersList = state.getActiveChildOrders().stream()
            .filter(order -> order.getSide() == Side.BUY)
            .filter(order -> order.getState()  == OrderState.PENDING)
            .peek(order -> activeChildBidOrdersListToString
            .add("ACTIVE CHILD BID Id:" + order.getOrderId() + " [" + order.getQuantity() + "@" + order.getPrice() + "]"))
            .collect(Collectors.toList());

        // if have active child BID orders, update the bids with the lowest and highest price
        if (!getActiveChildBidOrdersList().isEmpty()) {
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
            .add("FILLED/PARTFILLED BID Id:" + order.getOrderId() + " [" + order.getQuantity() + "@" + order.getPrice() + "] filledQuantity: " + order.getFilledQuantity())) // TODO DELETE LATER URING DEVELOPMENT FOR BACK TESTS
            .collect(Collectors.toList());

        // if there are filled child BID Orders
        if (!getFilledAndPartFilledChildBidOrdersList().isEmpty()) { 
            haveFilledBidOrders = true;
            setTotalFilledBidQuantity(); // update total filled bid quantity 
            setTotalExpenditure();
            setAverageEntryPrice();
            setTargetChildAskOrderPrice();
            setTrailingStopLoss();
        }

        // UPDATE DATA ABOUT MY ALGO'S CHILD ORDERS - SELL SIDE

        // Update list of active child ASK orders
        getActiveChildAskOrdersListToString().clear(); // for logging 
        getActiveChildAskOrdersList().clear();
        activeChildAskOrdersList = state.getActiveChildOrders().stream()
            .filter(order -> order.getSide() == Side.SELL)
            .filter(order -> order.getState()  == OrderState.PENDING)
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
            .peek(order-> filledAndPartFilledChildAskOrdersListToString // for loggging
            .add("FILLED/PARTFILLED ASK Id:" + order.getOrderId() + " [" + order.getQuantity() + "@" + order.getPrice() + "] filledQuantity: " + order.getFilledQuantity())) // forlogging
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

        if (getTotalFilledQuantityOfAllBidAndAskOrders() > 0) {
            setVWAP();
        }
        

        if (getNumOfSharesOwned() > 0) {
            haveShares = true;
        }

        logger.info("getChildBidOrderQuantity() is: " + getChildBidOrderQuantity());
        logger.info("getActiveChildBidOrdersToStringList() is: " + getActiveChildBidOrdersListToString());
        logger.info("getFilledAndPartFilledChildBidOrdersListToString() is: " + getFilledAndPartFilledChildBidOrdersListToString());
        logger.info("getTotalFilledBidQuantity() is: " + getTotalFilledBidQuantity());
        logger.info("getTotalExpenditure() is: " + getTotalExpenditure());
        logger.info("getAverageEntryPrice() is: " + getAverageEntryPrice());
        logger.info("getTargetChildAskOrderPrice() is: " + getTargetChildAskOrderPrice());
        logger.info("getTrailingStopLoss() is: " + getTrailingStopLoss());
        logger.info("getActiveChildAskOrdersListToString() is: " + getActiveChildAskOrdersListToString());
        logger.info("getFilledAndPartFilledChildAskOrdersListToString() is: " + getFilledAndPartFilledChildAskOrdersListToString());
        logger.info("getTotalFilledAskQuantity() is: " + getTotalFilledAskQuantity());
        logger.info("getTotalRevenue() is: " + getTotalRevenue());
        logger.info("getNumOfSharesOwned() is: " + getNumOfSharesOwned());
        logger.info("getTotalProfitOrLoss() is: " + getTotalProfitOrLoss());
        logger.info("getVWAP() is: " + getVWAP());


        // CREATE / CANCEL / BID / SELL DECISION LOGIC


        // EXIT CONDITION - UP TO A MAX OF 6 CHILD ORDERS

        if (getAllChildOrdersList().size() < 6) {

            logger.info("Currently own " + getNumOfSharesOwned() + " shares.");
            logger.info(supplyAndDemandStatus);
            logger.info("The relative spread is: " + getRelativeSpreadInCurrentTick());
            logger.info("SpreadType is: " + spreadType);



            // CANCELLING ORDERS

            // If have 0 shares left and have active ask orders, cancel all sell orders
            if ((getHaveShares() == false) && getHaveActiveAskOrders()) {
                logger.info("Have 0 shares but have active ask orders, cancelling all sell orders");
                return new CancelChildOrder(getActiveChildAskOrdersList().get(0));
            }

            // If a child ask order becomes uncompetitive, cancel it
            if (getHaveActiveAskOrders() && (getActiveChildAskOrderWithHighestPrice().getPrice() >= (getBestAskPriceInCurrentTick() + 5))) {
                logger.info("An active child ask order has become too uncompetitive, cancelling it");
                return new CancelChildOrder(getActiveChildAskOrderWithHighestPrice());
            }

            // If a child buy order becomes uncompetitive, cancel it
            if (getHaveActiveBidOrders() && (getActiveChildBidOrderWithLowestPrice().getPrice() <= (getBestBidPriceInCurrentTick() - 5))) {
                logger.info("An active child bid order has become too uncompetitive, cancelling it");
                return new CancelChildOrder(getActiveChildBidOrderWithLowestPrice());
            }


            // PLACING ASK ORDERS

            if (getHaveShares()) {

                // if VWAP hits stop loss price, sell everything
                if (getVWAP() <= getTrailingStopLoss()) {
                    if (getNumOfSharesOwned() > 0) {
                    logger.info("VWAP has hit trailing stop loss price, selling all shares for best price possible");
                    return new CreateChildOrder(Side.SELL, getNumOfSharesOwned(), getBestBidPriceInCurrentTick());
                    }   
                }

                // first ask order
                if (getHaveActiveAskOrders() == false ) {
                    logger.info("Placing an ask order at target profit price");
                    return new CreateChildOrder(Side.SELL, getNumOfSharesOwned(), getTargetChildAskOrderPrice());
                }
                
                // second ask order
                if ((getActiveChildAskOrdersList().size() >= 1) && (getActiveChildAskOrdersList().size() < 2)) {
                    logger.info("placing a passive ask order above current highest ask order");
                    return new CreateChildOrder(Side.SELL, getNumOfSharesOwned(), (getActiveChildAskOrderWithHighestPrice().getPrice() + 1));
                }
                
                // third ask order
                if ((getActiveChildAskOrdersList().size() >= 2) && (getActiveChildAskOrdersList().size() < 3) 
                && (getTargetChildAskOrderPrice() >= getBestAskPriceInCurrentTick())) {


                    if (((getSpreadType() == SpreadType.TIGHT) && sellPressure) || ((getSpreadType() == SpreadType.TIGHT) && marketEquilibirum)) {
                        logger.info("Placing an at-market ask order to pay the spread and execute immediately");
                            return new CreateChildOrder(Side.SELL, getNumOfSharesOwned(), getBestBidPriceInCurrentTick());
                    }

                    if (((getSpreadType() == SpreadType.WIDE) && sellPressure) || ((getSpreadType() == SpreadType.WIDE) && marketEquilibirum) || ((getSpreadType() == SpreadType.REGULAR) && sellPressure)) {
                        logger.info("Placing an ask order 1 tick size below current best ask price, narrowing the spread.");
                            return new CreateChildOrder(Side.SELL, getNumOfSharesOwned(), (getBestAskPriceInCurrentTick() - 1));
                    }

                    if (((getSpreadType() == SpreadType.REGULAR) && marketEquilibirum) || ((getSpreadType() == SpreadType.WIDE) && buyPressure)) {
                        logger.info("Placing an ask order to join current best ask price");
                        return new CreateChildOrder(Side.SELL, getNumOfSharesOwned(), getBestAskPriceInCurrentTick());
                    }

                    if ((getSpreadType() == SpreadType.TIGHT) && buyPressure) {
                        logger.info("Placing an ask order 1 tick size above current best ask price");
                            return new CreateChildOrder(Side.SELL, getNumOfSharesOwned(), (getBestAskPriceInCurrentTick() + 1));
                    }
                }

                

            }
        
            // PLACING BID ORDERS 
                
            // place 3 bid orders
            if (getActiveChildBidOrdersList().size() < 3) { 

                logger.info("Currently have " + getActiveChildBidOrdersList().size() + " active bid orders");

                bidPriceDifferentiator += 1;
                if (((getSpreadType() == SpreadType.TIGHT) && buyPressure) || ((getSpreadType() == SpreadType.TIGHT) && marketEquilibirum) ){
                    logger.info(" Placing 3 bid orders, the highest of which is an at-market bid order paying the spread to execute immediately.");
                    return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), (getBestAskPriceInCurrentTick() + bidPriceDifferentiator));
                
                } else if (((getSpreadType() == SpreadType.WIDE) && buyPressure) || ((getSpreadType() == SpreadType.WIDE) && marketEquilibirum) || ((getSpreadType() == SpreadType.REGULAR) && buyPressure) ){
                    logger.info(" Placing 3 bid orders, the highest of which will be 1 tick above current best bid price, narrowing the spread.");
                    return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), (getBestBidPriceInCurrentTick() + 1 + bidPriceDifferentiator));

                } else if (((getSpreadType() == SpreadType.WIDE) && sellPressure) || ((getSpreadType() == SpreadType.REGULAR) && marketEquilibirum) || ((getSpreadType() == SpreadType.REGULAR) && sellPressure) ){
                    logger.info(" Placing 3 bid orders, the highest of which will join the best bid price.");
                    return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), (getBestBidPriceInCurrentTick() + bidPriceDifferentiator));

                } else if ((getSpreadType() == SpreadType.TIGHT) && sellPressure) {
                    logger.info("Placing 3 orders, the highest of which will be 1 tick size less than the best bid price.");
                    return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), (getBestBidPriceInCurrentTick() - 1 + bidPriceDifferentiator));
                }
            }
            logger.info("No buy or sell conditions met, hold position until next market data tick");
            return NoAction.NoAction;
        } else {
            logger.info("Have 6 child orders in total, taking no further action");
            return NoAction.NoAction;
        } 
        
    }
}

    
        



//         
        
//         // place 3 bid orders
//         if (getActiveChildBidOrdersList().size() < 3) {
//                 int priceDifferentiator = -3;

            
//                 // if spread is wide, place a passive child order bid priced 1 tick above best bid to narrow the spread and (hopefully!) prompt trading
//                 if (wideSpread) {
//                     logger.info("Currently have " + getActiveChildBidOrdersList().size() + " active bid orders and spread is wide, placing a bid order above current best bid");
//                     return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), (getBestBidPriceInCurrentTick() +));
//                 }
//                 // if spread is regular, place a passive child bid order joining best bid price
//                 if (regularSpread) {
//                     logger.info("Currently have 1 active bid order and spread is regular, placing a bid order joining current best bid");
//                     return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), (getBestBidPriceInCurrentTick()));
//                 }
//                 // if spread is tight, place an at-market child bid order paying the spread to buy immediately
//                 if (tightSpread) {
//                     logger.info("Currently have 1 active bid order and spread is tight, paying the spread to place an at-market bid order to execute immediately");
//                     return new CreateChildOrder(Side.BUY, getChildBidOrderQuantity(), (getBestAskPriceInCurrentTick()));
//                 }  



