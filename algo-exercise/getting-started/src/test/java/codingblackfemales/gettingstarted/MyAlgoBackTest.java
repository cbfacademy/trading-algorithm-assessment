package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;

import org.junit.Test;

import messages.order.Side;
import static org.junit.Assert.assertEquals;


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

    // UNIT TESTING
    @Test
    public void unitTest() throws Exception {
        MyAlgoLogic myAlgoLogic = new MyAlgoLogic();

        // //create a sample market data tick....
        send(Tick1());

        //then: get the state
        var state = container.getState();

        myAlgoLogic.evaluate(state);
        assertEquals("testing the method: getBestBidOrderInCurrentTick()", "BID[200@98]", myAlgoLogic.getBestBidOrderInCurrentTick().toString()); // quantity = 100 from original orderbook + 100 placed by MyAlgoLogic
        assertEquals("testing the method: getBestBidPriceInCurrentTick()", 98, myAlgoLogic.getBestBidPriceInCurrentTick());
        assertEquals("testing the method: getBestBidQuantityInCurrentTick()", 200, myAlgoLogic.getBestBidQuantityInCurrentTick()); // 100 from original orderbook + 100 placed by MyAlgoLogic
        
        assertEquals("testing the method: getTopBidOrdersInCurrentTick()", "[BID[200@98], BID[100@97], BID[100@96], BID[200@95], BID[300@91]]", myAlgoLogic.getTopBidOrdersInCurrentTick().toString());
        assertEquals("testing the method: getPricesOfTopBidOrdersInCurrentTick()", "[98, 97, 96, 95, 91]", myAlgoLogic.getPricesOfTopBidOrdersInCurrentTick().toString());
        assertEquals("testing the method: getQuantitiesOfTopBidOrdersInCurrentTick()", "[200, 100, 100, 200, 300]", myAlgoLogic.getQuantitiesOfTopBidOrdersInCurrentTick().toString());
        assertEquals("testing the method: getTotalQuantityOfBidOrdersInCurrentTick()", 900, myAlgoLogic.getTotalQuantityOfBidOrdersInCurrentTick()); // 600 from original orderbook + 200 placed by MyAlgoLogic
        
        assertEquals("testing the method: getBestAskOrderInCurrentTick()", "ASK[101@100]", myAlgoLogic.getBestAskOrderInCurrentTick().toString()); 
        assertEquals("testing the method: getBestAskPriceInCurrentTick()", 100, myAlgoLogic.getBestAskPriceInCurrentTick());
        assertEquals("testing the method: getBestAskQuantityInCurrentTick()", 101, myAlgoLogic.getBestAskQuantityInCurrentTick());
        
        assertEquals("testing the method: getTopAskOrdersInCurrentTick()", "[ASK[101@100], ASK[200@110], ASK[5000@115], ASK[5600@119]]", myAlgoLogic.getTopAskOrdersInCurrentTick().toString());
        assertEquals("testing the method: getPricesOfTopAskOrdersInCurrentTick()", "[100, 110, 115, 119]", myAlgoLogic.getPricesOfTopAskOrdersInCurrentTick().toString());
        assertEquals("testing the method: getQuantitiesOfTopAskOrdersInCurrentTick()", "[101, 200, 5000, 5600]", myAlgoLogic.getQuantitiesOfTopAskOrdersInCurrentTick().toString());
        assertEquals("testing the method: getTotalQuantityOfAskOrdersInCurrentTick()", 10901, myAlgoLogic.getTotalQuantityOfAskOrdersInCurrentTick());
        
        assertEquals("testing the method: getTheSpreadInCurrentTick()", 2, myAlgoLogic.getTheSpreadInCurrentTick());
        assertEquals("testing the method: getMidPriceInCurrentTick()", 99, myAlgoLogic.getMidPriceInCurrentTick(), delta);
        assertEquals("testing the method: getRelativeSpreadInCurrentTick()", 2, myAlgoLogic.getRelativeSpreadInCurrentTick(), delta);


        // more asserts after a 2nd tick to see above update
    }


    @Test
    public void backTest() throws Exception {
        //create a sample market data tick....
        send(Tick1());

        assertEquals("Check creates a child order on buy side", Side.BUY, container.getState().getChildOrders().get(0).getSide());
        assertEquals("Check creates a second child order on buy side", Side.BUY, container.getState().getChildOrders().get(1).getSide());
        assertEquals("Check creates a third child order on buy side", Side.BUY, container.getState().getChildOrders().get(2).getSide());

        //then: get the state
        var state = container.getState();                
                
        // Check that our algo state was updated to reflect the total quantity of active child bid orders 
        long totalQuantityOfActiveChildBidOrders = state.getActiveChildOrders().stream()
            .filter(order -> order.getSide() == Side.BUY)
            .map(ChildOrder::getQuantity).reduce(Long::sum).get();
        assertEquals("Check total quantity of active child BID orders is 300", 300, totalQuantityOfActiveChildBidOrders);

        assertEquals("Check first child bid order price is 96", 96, container.getState().getChildOrders().get(0).getPrice());
        assertEquals("Check second child bid order price is 97", 97, container.getState().getChildOrders().get(1).getPrice());
        assertEquals("Check third child bid order price is 98", 98, container.getState().getChildOrders().get(2).getPrice());

        assertEquals("Check first child bid order quantity is 100", 100, container.getState().getChildOrders().get(0).getQuantity());
        assertEquals("Check second child bid order quantity is 100", 100, container.getState().getChildOrders().get(1).getQuantity());
        assertEquals("Check third child bid order quantity is 100", 100, container.getState().getChildOrders().get(2).getQuantity());

        //when: market data moves towards us
        // send(Tick2());

        //then: get the state
        // var state = container.getState();

        //Check things like filled quantity, cancelled order count etc....
        //long filledQuantity = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).get();
        //and: check that our algo state was updated to reflect our fills when the market data
        //assertEquals(225, filledQuantity);
    }

}
