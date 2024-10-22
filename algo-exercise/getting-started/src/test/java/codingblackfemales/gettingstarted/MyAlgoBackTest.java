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

    @Test
    public void testExampleBackTest() throws Exception {
        //create a sample market data tick....
        send(Tick1());

        assertEquals("Creates a child order on buy side", Side.BUY, container.getState().getChildOrders().get(0).getSide());
        assertEquals("Creates a second child order on buy side", Side.BUY, container.getState().getChildOrders().get(1).getSide());

        //then: get the state
        var state = container.getState();                
                
        // Check that our algo state was updated to reflect the total quantity of active child bid orders 
        long totalQuantityOfActiveChildBidOrders = state.getActiveChildOrders().stream()
            .filter(order -> order.getSide() == Side.BUY)
            .map(ChildOrder::getQuantity).reduce(Long::sum).get();
        assertEquals("Checks total quantity of active child BID orders is 200", 200, totalQuantityOfActiveChildBidOrders);

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
