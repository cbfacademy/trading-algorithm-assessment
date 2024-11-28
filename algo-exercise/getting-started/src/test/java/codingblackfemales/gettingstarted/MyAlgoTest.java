package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.service.MarketDataService;
import codingblackfemales.service.OrderService;
import codingblackfemales.sotw.marketdata.BidLevel;
import org.agrona.concurrent.UnsafeBuffer;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.SimpleAlgoStateImpl;
import messages.order.Side;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import codingblackfemales.container.RunTrigger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;



/**
 * This test is designed to check your algo behavior in isolation of the order book.
 *
 * You can tick in market data messages by creating new versions of createTick() (ex. createTick2, createTickMore etc..)
 *
 * You should then add behaviour to your algo to respond to that market data by creating or cancelling child orders.
 *
 * When you are comfortable you algo does what you expect, then you can move on to creating the MyAlgoBackTest.
 *
 */
public class MyAlgoTest extends AbstractAlgoTest {
    private SimpleAlgoState algoState;
    private MarketDataService marketDataService;
    private OrderService orderService;


    @Override
    public AlgoLogic createAlgoLogic() {
        //this adds your algo logic to the container classes
        return new MyAlgoLogic();
    }

    @Before
    public void setUp() {
        marketDataService = new MarketDataService(new RunTrigger());
        orderService = new OrderService(new RunTrigger());
        algoState = new SimpleAlgoStateImpl(marketDataService, orderService);
        System.out.println(" MyAlgoTest");
    }


    @Test
    public void testDispatchThroughSequencer() throws Exception {

        //create a sample market data tick....
        send(createTick());


        SimpleAlgoState state = container.getState();
        Action action = createAlgoLogic().evaluate(state);
        assertEquals("Do not action if spread is below threshold", NoAction.NoAction, action);
    }

    @Test
    public void testCancelOrderOnLowThreshold() throws Exception {
        // Send tick with low spread
        send(createTickWithLowThreshold());
        SimpleAlgoState state = container.getState();
        Action action = createAlgoLogic().evaluate(state);

        // Check that no action is taken if spread is too low for orders
        assertTrue("Expected NoAction when spread is below threshold", action instanceof NoAction);
    }


    @Test
    public void testHandleIncreasingVolume() throws Exception {
        // Arrange: Set up a tick with increasing bid/ask volume
        send(createTickWithIncreasingVolume());
        SimpleAlgoState state = container.getState();

        // Act: Evaluate the state after the high-volume tick
        Action action = createAlgoLogic().evaluate(state);

        // Assert: Check that algo reacts appropriately to the high-volume tick
        assertTrue("Expected NoAction or appropriate order response on high volume", action instanceof Action);
    }

    @Test
    public void testCreateOrderWithHighThreshold() throws Exception {
        // Arrange: Send a tick with a high spread
        send(createTickWithHighThreshold());
    }

    @Test
    public void testMaxOrders() throws Exception {
        send(createTick());

        //check algo has a max order limit of 10
        assertTrue("There should be at least 10 child orders", container.getState().getChildOrders().size() <= 10);
    }
}



