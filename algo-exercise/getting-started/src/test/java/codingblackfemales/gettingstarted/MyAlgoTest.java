package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import messages.order.Side;
import org.junit.Assert;
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
    public void testBuyCreation() throws Exception {
        //create a sample market data tick....
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        //Retrieve the state from the container
        SimpleAlgoState state = container.getState();

        long buyOrdersCount = state.getChildOrders().stream()
                .filter(childOrder -> childOrder.getSide() == Side.BUY)
                .count();
        //assertion
        Assert.assertTrue("BUY orders count should be at least 3", buyOrdersCount >= 3);

    }

    @Test
    public void testSellCreation() throws Exception {
        //create a sample market data tick....
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        //Retrieve the state from the container
        SimpleAlgoState state = container.getState();

        long sellOrdersCount = state.getChildOrders().stream()
                .filter(childOrder -> childOrder.getSide() == Side.SELL)
                .count();
        //assertion
        Assert.assertTrue("SELL orders count should be at least 3", sellOrdersCount >= 3);

    }

    @Test
    public void testBuyOrderCancel() throws Exception {
        //create a sample market data tick....
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        //Retrieve the state from the container
        SimpleAlgoState state = container.getState();

        //verify buy orders are cancelled if prices don't match the best bid price
        long cancelledBuyOrders = state.getCancelledChildOrders().stream()
                .filter(childOrder -> childOrder.getSide() == Side.BUY)
                .count();
        Assert.assertTrue("At least one Buy order should be cancelled", cancelledBuyOrders > 0);
    }

    @Test
    public void testSellOrderCancel() throws Exception {
        //create a sample market data tick....
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        //Retrieve the state from the container
        SimpleAlgoState state = container.getState();

        //verify Sell orders are cancelled if prices don't match the best Ask price
        long cancelledSellOrders = state.getCancelledChildOrders().stream()
                .filter(childOrder -> childOrder.getSide() == Side.SELL)
                .count();
        //assertion
       Assert.assertTrue("At least one Sell order should be cancelled", cancelledSellOrders < 1);
    }


    @Test
    public void testNoActionOnSmallSpread() throws Exception {
        //create a sample market data tick....
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());

        //Retrieve the state from the container
        SimpleAlgoState state = container.getState();
        //calling evaluate()
        Action action = createAlgoLogic().evaluate(state);

      //assertion
        assertEquals("No action should be taken if the spread is below the threshold", NoAction.NoAction,action);

    }


    }
