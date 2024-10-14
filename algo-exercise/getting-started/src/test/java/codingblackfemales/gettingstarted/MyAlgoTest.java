package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.container.Actioner;
import codingblackfemales.container.AlgoContainer;
import codingblackfemales.container.RunTrigger;
import codingblackfemales.sequencer.DefaultSequencer;
import codingblackfemales.sequencer.net.TestNetwork;
import codingblackfemales.service.MarketDataService;
import codingblackfemales.service.OrderService;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import messages.order.Side;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static codingblackfemales.container.Actioner.logger;
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

import org.junit.Before;
import org.junit.After;

import java.util.List;

public class MyAlgoTest extends AbstractAlgoTest {

        @Override
        public AlgoLogic createAlgoLogic() {
            return new MyAlgoLogic();
        }

        @Before
        public void setup() {
            // This will reset the AlgoContainer and all necessary services before each test case
            container = new AlgoContainer(new MarketDataService(new RunTrigger()), new OrderService(new RunTrigger()), new RunTrigger(), new Actioner(new DefaultSequencer(new TestNetwork())));
            container.setLogic(createAlgoLogic());
        }

        @After
        public void tearDown() {
            // Optional: Clean up if necessary
            container = null;
        }

        @Test
        public void testAlgoNeverExceedsMaxOrderCount() throws Exception {
            for (int i = 0; i <= 25; i++) {
                send(createTick());
            }
            assertTrue(container.getState().getChildOrders().size() <= 20);
        }

        @Test
        public void testAlgoCreatesBuyOrder() throws Exception {
            for (int i = 0; i <= 6; i++) {
                send(createTick());  // Send each tick
            }

            // Wait for the state to be updated
           // Thread.sleep(100);  // Adjust the timing as needed

            // Retrieve and update the state
            SimpleAlgoState state = container.getState();

            // Assertion after the state has been updated
            long buyOrdersCount = state.getChildOrders().stream()
                    .filter(childOrder -> childOrder.getSide() == Side.BUY)
                    .count();

            //SimpleAlgoState state = container.getState();

            assertEquals(3, buyOrdersCount);
        }


    @Test
    public void testAlgoExecutesStopLoss() throws Exception {
        // Send ticks to trigger stop-loss logic
        for (int i = 0; i < 6; i++) {
            send(createTickStopLoss());
        }

        // Retrieve the state after sending the stop-loss tick
        SimpleAlgoState state = container.getState();

        // Assert that orders have been canceled after stop-loss is triggered
        List<ChildOrder> cancelledOrders = state.getCancelledChildOrders();
        assertTrue("There should be cancelled orders due to stop-loss", cancelledOrders.size() > 0);
        logger.info("Cancelled orders count: " + cancelledOrders.size());

        // Assert that there are no active child orders after stop-loss (if applicable)
       // assertEquals("There should be no active child orders after stop-loss", 0, state.getActiveChildOrders().size());
    }


    @Test
        public void testAlgoCreatesSellOrder() throws Exception {
            for (int i = 0; i <= 6; i++) {
                send(createTickSell());
            }
            assertEquals(3, container.getState().getChildOrders().stream()
                    .filter(childOrder -> childOrder.getSide() == Side.SELL)
                    .count());
        }
    }

