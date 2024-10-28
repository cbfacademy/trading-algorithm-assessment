package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.OrderState;

import org.junit.Test;

import messages.order.Side;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This test plugs together all of the infrastructure, including the order book (which you can trade against)
 * and the market data feed.
 *
 * If your algo adds orders to the book, they will reflect in your market data coming back from the order book.
 *
 * If you cross the srpead (i.e. you BUY an order with a price which is == or > askPrice()) you will match, and receive
 * a fill back into your order from the order book (visible from the algo in the childOrders of the state object.
 *
 * If you cancel the order your child order will show the order status as cancelled in the childOrders of the state object.
 *
 */
public class MyAlgoBackTest extends AbstractAlgoBackTest {

    @Override
    public AlgoLogic createAlgoLogic() {
        return new MyAlgoLogic();
    }

    double delta = 0.0001;

    @Test
    public void testGetsBestBidOrderInCurrentTick() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals("Best bid price should be 98", 98, myAlgoLogic.getBestBidPriceInCurrentTick());
        assertEquals("Best bid quantity should be 200", 200, myAlgoLogic.getBestBidQuantityInCurrentTick()); // 100 from original orderbook + 100 placed by MyAlgoLogic
    }
    
    @Test
    public void testGetsTopBidsOrdersInCurrentTick() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);

        assertEquals("[BID[200@98], BID[100@97], BID[100@96], BID[200@95], BID[300@91]]", myAlgoLogic.getTopBidOrdersInCurrentTick().toString()); //
        assertArrayEquals("Best bid prices should be 98, 97, 96, 95, 91", new Long[]{(long) 98, (long) 97, (long) 96, (long) 95, (long) 91}, myAlgoLogic.getPricesOfTopBidOrdersInCurrentTick().toArray(Long[]::new));
        assertArrayEquals("Best bid quantities should be 200, 100, 100, 200, 300", new Long[]{(long) 200, (long) 100, (long) 100, (long) 200, (long) 300}, myAlgoLogic.getQuantitiesOfTopBidOrdersInCurrentTick().toArray(Long[]::new));
        assertEquals("Total quantity of best bids should be 900", 900, myAlgoLogic.getTotalQuantityOfBidOrdersInCurrentTick()); // 600 from original orderbook + 200 placed by MyAlgoLogic
    }

    @Test
    public void testGetsBestAskOrderInCurrentTick() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals( "ASK[101@100]", myAlgoLogic.getBestAskOrderInCurrentTick().toString()); 
        assertEquals("Best ask price should be 100", 100, myAlgoLogic.getBestAskPriceInCurrentTick());
        assertEquals("Best ask quantity should be 98", 101, myAlgoLogic.getBestAskQuantityInCurrentTick());
    }

    @Test
    public void testGetsTopAskOrdersInCurrentTick() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals("testing the method: getTopAskOrdersInCurrentTick()", "[ASK[101@100], ASK[200@110], ASK[5000@115], ASK[5600@119]]", myAlgoLogic.getTopAskOrdersInCurrentTick().toString());
        assertArrayEquals("Best ask prices should be 100, 110, 115, 119", new Long[]{(long) 100, (long) 110, (long) 115, (long) 119}, myAlgoLogic.getPricesOfTopAskOrdersInCurrentTick().toArray(Long[]::new));
        assertArrayEquals("Best ask quantities should be 101, 200, 5000, 5600", new Long[]{(long) 101, (long) 200, (long) 5000, (long) 5600}, myAlgoLogic.getQuantitiesOfTopAskOrdersInCurrentTick().toArray(Long[]::new));
        assertEquals("Total quantity of ask orders should be 10901", 10901, myAlgoLogic.getTotalQuantityOfAskOrdersInCurrentTick());
    }

    @Test
    public void testGetsTheSpreadAndMidPriceInCurrentTick() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals("The spread should be 2", 2, myAlgoLogic.getTheSpreadInCurrentTick());
        assertEquals("The mid price should be 99", 99, myAlgoLogic.getMidPriceInCurrentTick(), delta);
        assertEquals("The relative spread should be 2", 2, myAlgoLogic.getRelativeSpreadInCurrentTick(), delta);
    }

    @Test
    public void testCreatesThreeChildOrdersOnTheBuySide() throws Exception {
        //create a sample market data tick....
        send(Tick1());

        assertEquals("Check creates a child order on buy side", Side.BUY, container.getState().getChildOrders().get(0).getSide());
        assertEquals("Check creates a second child order on buy side", Side.BUY, container.getState().getChildOrders().get(1).getSide());
        assertEquals("Check creates a third child order on buy side", Side.BUY, container.getState().getChildOrders().get(2).getSide());
        
    }

    @Test
    public void testBooleanCheckForActiveChildBidOrders() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals("getHaveActiveBidOrders() should evaluate to true", true, myAlgoLogic.getHaveActiveBidOrders());

    }

    @Test
    public void testListOfActiveChildBidOrdersUpdates() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals("List of active child bid orders should contain 3 orders after Tick 1", 3, myAlgoLogic.getActiveChildBidOrdersList().size());
    }

    @Test
    public void testGetsDataAboutActiveChildBidOrders() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals("List of child orders as strings", "[ACTIVE CHILD BID Id:2 [100@96], ACTIVE CHILD BID Id:3 [100@97], ACTIVE CHILD BID Id:4 [100@98]]", myAlgoLogic.getActiveChildBidOrdersToStringList().toString());
        assertEquals("Price of active child bid order with the lowest price should be 96", 96, myAlgoLogic.getActiveChildBidOrderWithLowestPrice().getPrice());
        assertEquals("Price of active child bid order with the highest price should be 98", 98, myAlgoLogic.getActiveChildBidOrderWithHighestPrice().getPrice());
    }




    @Test
    public void testTotalQuantityOfFirstThreeChildOrders() throws Exception {
        send(Tick1());
         //then: get the state
        var state = container.getState(); 

        // Check that our algo state was updated to reflect the total quantity of active child bid orders 
        long totalQuantityOfActiveChildBidOrders = state.getActiveChildOrders().stream()
            .filter(order -> order.getSide() == Side.BUY)
            .map(ChildOrder::getQuantity).reduce(Long::sum).get();
        assertEquals("Check total quantity of active child BID orders is 300", 300, totalQuantityOfActiveChildBidOrders);
    }

    @Test
    public void testFirstChildBidOrderPriceAndQuantity() throws Exception {

        send(Tick1());
    
        //then: get the state
        var state = container.getState();    
        

        assertEquals("First child bid order price should be 96", 96, state.getChildOrders().get(0).getPrice());
        assertEquals("First child bid order quantity should be 100", 100, state.getChildOrders().get(0).getQuantity());
    }

        @Test
        public void testSecondChildBidOrderPriceAndQuantity() throws Exception {
        send(Tick1());
    
        //then: get the state
        var state = container.getState();         

        assertEquals("Second child bid order price should be 97", 97, state.getChildOrders().get(1).getPrice());
        assertEquals("Second child bid order quantity should be 100", 100, state.getChildOrders().get(1).getQuantity());
        }


        @Test
        public void testThirdChildBidOrderPriceAndQuantity() throws Exception {
        send(Tick1());
    
        //then: get the state
        var state = container.getState(); 
        assertEquals("Third child bid order price should be 98", 98, state.getChildOrders().get(2).getPrice());
        assertEquals("Third child bid order quantity should be 100", 100, state.getChildOrders().get(2).getQuantity());
        
    }

    @Test
    public void testChildBidOrderId4ExecutesAfterTick2() throws Exception {
    send(Tick1());
    send(Tick2());
    //then: get the state
    var state = container.getState(); 
    
    ChildOrder filledChildOrder = state.getChildOrders().stream()
        .filter(order -> order.getFilledQuantity() > 0)
        .findFirst()
        .orElse(null);
    
    assertEquals(4, filledChildOrder.getOrderId());

    long filledQuantity = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).get();
    assertEquals(100, filledQuantity);

}


@Test
    public void testChildBidOrderId4IsAddedToListOfFilledAndPartFilledChildBidOrders() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals(1, myAlgoLogic.getFilledAndPartFilledChildBidOrdersList().size());
        assertEquals(4, myAlgoLogic.getFilledAndPartFilledChildBidOrdersList().get(0).getOrderId());
    }

    @Test
    public void testGetMethodForTotalFilledBidQuantityUpdates () throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals(100, myAlgoLogic.getTotalFilledBidQuantity());
    }

    @Test
    public void testGetMethodForTotalExpenditureUpdates () throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals(9800, myAlgoLogic.getTotalExpenditure());
    }

    @Test
    public void testCalculatesTotalProfitOrLoss () throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals(-9800, myAlgoLogic.getTotalProfitOrLoss());
    }

    

    @Test
    public void testUpdatesNumOfSharesOwned () throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals("Num of shares owned after tick 2 should be 100", 100, myAlgoLogic.getNumOfSharesOwned());
    }
    
    @Test
    public void testGetMethodForAverageEntryPriceUpdates () throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());

        //then: get the state
        var state = container.getState();
        myAlgoLogic.evaluate(state);

        assertEquals("Average entry price should evaluate to 0 after tick 1", 0, myAlgoLogic.getAverageEntryPrice());

        send(Tick2());

        //then: get the state again
        var state2 = container.getState();

        myAlgoLogic.evaluate(state2);
        assertEquals("Average entry price should evaluate to 98 after tick 2", 98, myAlgoLogic.getAverageEntryPrice());
    }
    
    @Test
    public void testBooleanCheckForActiveChildAskOrders() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());

        //then: get the state
        var state = container.getState();
        myAlgoLogic.evaluate(state);

        assertEquals("Should not have active child ask orders after Tick1", false, myAlgoLogic.getHaveActiveAskOrders());

        send(Tick2());

         //then: get the state again
        var state2 = container.getState();
        myAlgoLogic.evaluate(state2);

        assertEquals("Should have an active child ask order after Tick2", true, myAlgoLogic.getHaveActiveAskOrders());
    }

    @Test
    public void testBooleanCheckForFilledChildAskOrders() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());

         //then: get the state again
        var state = container.getState();
        myAlgoLogic.evaluate(state);

        assertEquals("Should not have any filled child ask orders after Tick2", false, myAlgoLogic.getHaveFilledAskOrders());
    }

    
    @Test
    public void testCreatesChildOrderOnSellSide () throws Exception {
        send(Tick1());
        send(Tick2());

        var state = container.getState();
        assertEquals("Check creates a child order on sell side", Side.SELL, state.getChildOrders().get(3).getSide());
        assertEquals("Quantity of child order on sell side should be 100", 100, state.getChildOrders().get(3).getQuantity());

    }

    @Test
    public void testCalculatesChildAskOrderBasedOnTargetProfitOfAverageEntryPricePlusThreePercent () throws Exception {
        send(Tick1());
        send(Tick2());
        assertEquals("Price of child order on sell side should be 101 based on average entry price of 98 (Math.ceil(98 * 1.03))", 101, container.getState().getChildOrders().get(3).getPrice());
    }

    @Test
    public void testListOfActiveChildAskOrdersGetsUpdated() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());
        var state = container.getState();
        myAlgoLogic.evaluate(state);

        assertEquals("There should be 2 active child ask orders in the list", 2, myAlgoLogic.getActiveChildAskOrdersList().size());

    }
    
    @Test
    public void testGetsDataAboutActiveChildAskOrderWithHighestPrice() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());

        var state = container.getState();
        myAlgoLogic.evaluate(state);

        assertEquals("Price of active child ask order with the highest price should be 101", 101, myAlgoLogic.getActiveChildAskOrderWithHighestPrice().getPrice());
        assertEquals("Quantity of active child ask order with the highest price should be 100", 100, myAlgoLogic.getActiveChildAskOrderWithHighestPrice().getQuantity());
        assertEquals("Order ID of active child ask order with the highest price should be 5", 5, myAlgoLogic.getActiveChildAskOrderWithHighestPrice().getOrderId());


    }

    @Test
    public void testGetsDataAboutActiveChildAskOrderWithLowestPrice() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());

        var state = container.getState();
        myAlgoLogic.evaluate(state);

        // After Tick 2 the active child order with the lowest price should be the same as that with the highest price
        assertEquals("Price of active child ask order with the lowest price should be 101", 101, myAlgoLogic.getActiveChildAskOrderWithLowestPrice().getPrice());
        assertEquals("Quantity of active child ask order with the lowest price should be 100", 100, myAlgoLogic.getActiveChildAskOrderWithLowestPrice().getQuantity());
        assertEquals("Order ID of active child ask order with the lowest price should be 5", 5, myAlgoLogic.getActiveChildAskOrderWithHighestPrice().getOrderId());

    }

    @Test
    public void testSetsTargetChildAskOrderPrice() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());

        var state = container.getState();
        myAlgoLogic.evaluate(state);

        assertEquals("Target child ask order price should be average entry price * 1.03 rounded up with Math.ceil", 101, myAlgoLogic.getTargetChildAskOrderPrice());

    }

    @Test
    public void testLeastCompetitiveBidOrderGetsCancelled() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());


        var state = container.getState();
        myAlgoLogic.evaluate(state);
        assertEquals("Least competitve bid order after tick 2 should be order Id 2", 2, myAlgoLogic.getActiveChildBidOrderWithLowestPrice().getOrderId());

        // send(Tick1());
        // send(Tick2());
        send(Tick3());

        var state2 = container.getState();
        myAlgoLogic.evaluate(state2);
        assertEquals("Least competitve bid order after tick 3 should still be order Id 2", 2, myAlgoLogic.getActiveChildBidOrderWithLowestPrice().getOrderId());

        // send(Tick1());
        // send(Tick2());
        // send(Tick3());
        send(Tick4());

        var state3 = container.getState();
        
        List<ChildOrder> cancelledOrders = state3.getChildOrders().stream()
            .filter(order -> order.getState() == OrderState.CANCELLED)
            .collect(Collectors.toList());

        assertEquals("There should be one cancelled child order", 1, cancelledOrders.size());
        assertEquals("The canceled child order should be order Id 2", 2, cancelledOrders.get(0).getOrderId());


    }
    
}
        //when: market data moves towards us
        // send(Tick2());

        //then: get the state
        // var state = container.getState();

        //Check things like filled quantity, cancelled order count etc....
        // long filledQuantity = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).get();
        //and: check that our algo state was updated to reflect our fills when the market data
        // assertEquals(225, filledQuantity);