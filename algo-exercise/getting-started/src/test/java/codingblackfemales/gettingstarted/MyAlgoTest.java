package codingblackfemales.gettingstarted;
import codingblackfemales.algo.AlgoLogic;
import org.junit.Test;
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

    @Override
    public AlgoLogic createAlgoLogic() {
        //this adds your algo logic to the container classes
        return new MyAlgoLogic();
    }

    @Test
    public void testDispatchThroughSequencer() throws Exception {
        //create a sample market data tick....
        send(createTick3());
        send(createTick());
        send(createTickLowLiquidity());
        send(createTickHighPrices());
        send(createTick2());

        //simple assert to check we had 3 orders created
      assertEquals(container.getState().getChildOrders().size(), 3);
    }

    @Test
    public void testLowLiquidityScenario() throws Exception {
        send(createTickLowLiquidity());
        // Check that the algo does not place orders in a low liquidity scenario
        assertEquals(0, container.getState().getChildOrders().size());
    }

    @Test
    public void testMaxOrderLimit() throws Exception {
        send(createTick());
        send(createTick2());
        // check that the algo respects max order limit of 6
        assertTrue(container.getState().getChildOrders().size() <= 6);
    }

    @Test
    public void testPriceThresholdCancellation() throws Exception {
        send(createTickHighPrices());
        // algo should cancel an order if the price moves beyond threshold
        assertEquals(1, container.getState().getCancelledChildOrders().size());
    }

    @Test
    public void testNoActionOnWideSpread() throws Exception {
        send(createTickWithWideSpread());
        // check that no orders are created when the spread is wider than threshold
        assertEquals(0, container.getState().getChildOrders().size());
    }

    @Test
    public void testOrderCreationWithTightSpread() throws Exception {
        send(createTickWithTightSpread());
        // check that order is created when spread is tight
        assertEquals(1, container.getState().getChildOrders().size());
    }
}