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

        assertEquals("[BID[200@98], BID[100@97], BID[200@95], BID[300@91]]", myAlgoLogic.getTopBidOrdersInCurrentTick().toString()); //
        assertArrayEquals("Best bid prices should be 98, 97, 95, 91", new Long[]{(long) 98, (long) 97, (long) 95, (long) 91}, myAlgoLogic.getPricesOfTopBidOrdersInCurrentTick().toArray(Long[]::new));
        assertArrayEquals("Best bid quantities should be 200, 100, 200, 300", new Long[]{(long) 200,(long) 100, (long) 200, (long) 300}, myAlgoLogic.getQuantitiesOfTopBidOrdersInCurrentTick().toArray(Long[]::new));
        assertEquals("Total quantity of best bids should be 800", 800, myAlgoLogic.getTotalQuantityOfBidOrdersInCurrentTick()); // 600 from original orderbook + 200 placed by MyAlgoLogic
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
    public void testAChildOrderOnTheBuySideAfterTick1() throws Exception {
        //create a sample market data tick....
        send(Tick1());

        assertEquals("Check creates a child order on buy side", Side.BUY, container.getState().getChildOrders().get(0).getSide());        
    }

    @Test
    public void testPriceOfChildBidOrdersAfterTick1() throws Exception {
        //create a sample market data tick....
        send(Tick1());

        assertEquals("1st child order should be priced at 97, just below best bid", 97, container.getState().getChildOrders().get(0).getPrice());
        assertEquals("2nd child order should be priced at 98, joining best bid", 98, container.getState().getChildOrders().get(1).getPrice());        

    }

    @Test
    public void testQuantityOfFirstChildBidOrderAfterTick1() throws Exception {
        //create a sample market data tick....
        send(Tick1());

        assertEquals("Child order should be for a quantity of 100", 100, container.getState().getChildOrders().get(0).getQuantity());        
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
        assertEquals("List of active child bid orders should contain 2 order after Tick 1", 2, myAlgoLogic.getActiveChildBidOrdersList().size());
    }

    @Test
    public void testGetsDataAboutActiveChildBidOrders() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals("List of child orders as strings", "[ACTIVE CHILD BID Id:2 [100@97], ACTIVE CHILD BID Id:3 [100@98]]", myAlgoLogic.getActiveChildBidOrdersToStringList().toString());
        assertEquals("Price of active child bid order with the lowest price should be 97", 97, myAlgoLogic.getActiveChildBidOrderWithLowestPrice().getPrice());
        assertEquals("Price of active child bid order with the highest price should be 98", 98, myAlgoLogic.getActiveChildBidOrderWithHighestPrice().getPrice());
    }




    @Test
    public void testTotalQuantityOfActivChildOrdersAfterTick1() throws Exception {
        send(Tick1());
         //then: get the state
        var state = container.getState(); 

        // Check that our algo state was updated to reflect the total quantity of active child bid orders 
        long totalQuantityOfActiveChildBidOrders = state.getActiveChildOrders().stream()
            .filter(order -> order.getSide() == Side.BUY)
            .map(ChildOrder::getQuantity).reduce(Long::sum).get();
        assertEquals("Total quantity of active child BID orders should be 200 after Tick 1", 200, totalQuantityOfActiveChildBidOrders);
    }


    @Test
    public void testChildBidOrderId3ExecutesAfterTick2() throws Exception {
    send(Tick1());
    send(Tick2());
    //then: get the state
    var state = container.getState(); 
    
    ChildOrder filledChildOrder = state.getChildOrders().stream()
        .filter(order -> order.getFilledQuantity() > 0)
        .findFirst()
        .orElse(null);
    
    assertEquals(3, filledChildOrder.getOrderId());

    long filledQuantity = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).get();
    assertEquals(100, filledQuantity);

}


@Test
    public void testChildBidOrderId3IsAddedToListOfFilledAndPartFilledChildBidOrders() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals(1, myAlgoLogic.getFilledAndPartFilledChildBidOrdersList().size());
        assertEquals(3, myAlgoLogic.getFilledAndPartFilledChildBidOrdersList().get(0).getOrderId());
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
        assertEquals("After Tick 2, total expenditure should be 9800", 9800, myAlgoLogic.getTotalExpenditure());
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
    public void testAverageEntryPriceIs0AfterTick1 () throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());

        //then: get the state
        var state = container.getState();
        myAlgoLogic.evaluate(state);

        assertEquals("Average entry price should evaluate to 0 after tick 1", 0, myAlgoLogic.getAverageEntryPrice());
    }

    @Test
    public void testGetMethodForAverageEntryPriceUpdatesAfterTick2 () throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());

        //then: get the state
        var state = container.getState();
        myAlgoLogic.evaluate(state);

        assertEquals("Average entry price should evaluate to 98 after tick 2", 98, myAlgoLogic.getAverageEntryPrice());
    }

    @Test
    public void testBooleanCheckForActiveChildAskOrdersEvaluatesToFalseAfterTick1() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());

        //then: get the state
        var state = container.getState();
        myAlgoLogic.evaluate(state);

        assertEquals("Should not have active child ask orders after Tick1", false, myAlgoLogic.getHaveActiveAskOrders());

    }
    
    @Test
    public void testBooleanCheckForActiveChildAskOrdersEvaluatesToTrueAfterTick2() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());

        //then: get the state
        var state = container.getState();
        myAlgoLogic.evaluate(state);

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
        assertEquals("Check creates a child order on sell side", Side.SELL, state.getChildOrders().get(2).getSide());
        assertEquals("Quantity of child order on sell side should be 100", 100, state.getChildOrders().get(2).getQuantity());

    }

    @Test
    public void testPriceOfActiveChildBidOrderIsAsExpected () throws Exception {
        send(Tick1());
        send(Tick2());
        assertEquals("Price of 1st child order on SELL side should be 101 based on average entry price of 98 (Math.ceil(98 * 1.03))", 101, container.getState().getChildOrders().get(2).getPrice());
        assertEquals("Price of 2nd child order on SELL side should be 102 based on placing an order 1 tick above current best ask)", 102, container.getState().getChildOrders().get(3).getPrice());

    }

    @Test
    public void testListOfActiveChildAskOrdersGetsUpdated() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());
        var state = container.getState();
        myAlgoLogic.evaluate(state);

        assertEquals("There should be 2 active child ask orders after Tick 2", 2, myAlgoLogic.getActiveChildAskOrdersList().size());

    }
    
    @Test
    public void testGetsDataAboutActiveChildAskOrderWithHighestPrice() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        send(Tick1());
        send(Tick2());

        var state = container.getState();
        myAlgoLogic.evaluate(state);

        assertEquals("Price of active child ask order with the highest price should be 102", 102, myAlgoLogic.getActiveChildAskOrderWithHighestPrice().getPrice());
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
        assertEquals("Order ID of active child ask order with the lowest price should be 4", 4, myAlgoLogic.getActiveChildAskOrderWithLowestPrice().getOrderId());

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
    public void testHave4ChildOrdersInTotalAfterTick2() throws Exception {
        
        send(Tick1());
        send(Tick2());

        var state = container.getState();

        assertEquals("Should have 4 child orders in total after tick 2", 4, state.getChildOrders().size());

    }


    @Test
    public void testNoActionAfterTick3() throws Exception {

        send(Tick1());
        send(Tick2());
        send(Tick3());

        var state = container.getState();

        assertEquals("Should still have only 4 child orders after tick 3", 4, state.getChildOrders().size());
    }



    @Test
    public void testStatusOfAskOrderId4() throws Exception {

        send(Tick1());
        send(Tick2());
        send(Tick3());
        send(Tick4());

        var state = container.getState();

        List <ChildOrder> childOrderId4 = state.getChildOrders().stream()
            .filter(order -> order.getOrderId() == 4)
            .collect(Collectors.toList());
        
        assertEquals("Child order ID 4 should be a sell order", Side.SELL, childOrderId4.get(0).getSide());
        assertEquals("Child order ID 4 should be a sell order priced at 101", 101, childOrderId4.get(0).getPrice());
        // assertEquals("Child order ID 3 should be filled after tick 4", OrderState.FILLED, childOrderId3.get(0).getState());
    }

    @Test
    public void testStatusOfAskOrderId5() throws Exception {

        send(Tick1());
        send(Tick2());
        send(Tick3());
        send(Tick4());

        var state = container.getState();

        List <ChildOrder> childOrderId5 = state.getChildOrders().stream()
            .filter(order -> order.getOrderId() == 5)
            .collect(Collectors.toList());
        
        assertEquals("Child order ID 5 should be a sell order", Side.SELL, childOrderId5.get(0).getSide());
        assertEquals("Child order ID 5 should be a sell order priced at 102", 102, childOrderId5.get(0).getPrice());
        // assertEquals("Child order ID 5 should be filled after tick 4", OrderState.FILLED, childOrderId5.get(0).getState());
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