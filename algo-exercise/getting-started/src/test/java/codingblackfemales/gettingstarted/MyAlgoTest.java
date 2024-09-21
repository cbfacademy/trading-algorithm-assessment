package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import messages.order.Side;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(MyAlgoTest.class.getName());
    @Override
    public AlgoLogic createAlgoLogic() {
        //this adds your algo logic to the container classes
        return new MyAlgoLogic();
    }

    @Test
    public void testDispatchThroughSequencer() throws Exception {

        //create a sample market data tick....
        send(createTick());
        send(createTick2());
        send(createTick3());

        //Retrieve the state from the container
        SimpleAlgoState state = container.getState();
        // Run the algorithm logic
       Action action = createAlgoLogic().evaluate(state);
        // count initial active buy orders
        long initialBuyOrderCount = state.getActiveChildOrders().stream()
                .filter(order -> order.getSide() == Side.BUY)
                .count();
        // Check for canceled buy orders
        long cancelBuyOrderCount = state.getCancelledChildOrders().stream()
                .filter(order -> order.getSide() == Side.BUY )
                .count();
        //Check  initial active Sell Orders
        long initialSellOrderCount = state.getActiveChildOrders().stream()
                .filter(order -> order.getSide() == Side.SELL)
                .count();
        // Check for canceled Sell orders
        long cancelSellOrderCount = state.getCancelledChildOrders().stream()
                .filter(order -> order.getSide() == Side.SELL )
                .count();


        //Assertions
       logger.info("Verifying assertions...");
        // verify 3 buy order has been created
        assertEquals("Should create 3 Buy orders",3, state.getBidLevels());
     // check that the canceled buy order match the expected number
       assertTrue("Should cancel at least 1 BUY order", cancelBuyOrderCount >=1);


        // verify 3 Ask order has been created
        assertEquals("Should create 3 Ask orders", 3, state.getAskLevels());
        // check that the canceled Sell order match the expected number
        assertTrue("Should cancel at least 1 SELL order", cancelSellOrderCount >=1);
        logger.info("Test completed successfully");

    }

}

