package codingblackfemales.gettingstarted;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        send(createTick());
        //when: market data moves towards us
        send(createTick());
        send(createTick3());
        send(createTick2());

        //then: get the state
        var state = container.getState();
    }

    @Test
    public void testOrderCancellationOnPriceMove() throws Exception {
        send(createTick());
        // simulate large price movement
        send(createTickHighPrices());
        // assert that an order is canceled due to price movement beyond threshold
        assertEquals(1, container.getState().getCancelledChildOrders().size());
    }

    @Test
    public void testOrderFillingAndStateUpdate() throws Exception {
        send(createTick2());
        send(createTick3());
        send(createTick2());
        send(createTick());
        send(createTickLowLiquidity());
        send(createTickHighPrices());

        var state = container.getState();

        // calculate the total filled quantity of all child orders
        long filledQuantity = state.getChildOrders().stream()
                .mapToLong(ChildOrder::getFilledQuantity)
                .sum();
        // assert that the filled quantity matches expected value
        assertEquals(400, filledQuantity);
        // check if  algorithm state was updated to reflect the fill
        assertTrue(filledQuantity > 0);
        // check if any orders were canceled during this period
        assertEquals(3, state.getCancelledChildOrders().size());
    }

    @Test
    public void testLowLiquidityNoAction() throws Exception {
        send(createTickLowLiquidity());
        // assert that no orders are created
        assertEquals(0, container.getState().getChildOrders().size());
    }

    @Test
    public void testOrderManagement() throws Exception {
        send(createTick2());
        send(createTick3());
        send(createTick2());
        send(createTick());
        send(createTickLowLiquidity());
        send(createTickHighPrices());
        // ensure there are no more than allowed number of active child orders
        assertTrue(container.getState().getActiveChildOrders().size() <= 6);
    }
}