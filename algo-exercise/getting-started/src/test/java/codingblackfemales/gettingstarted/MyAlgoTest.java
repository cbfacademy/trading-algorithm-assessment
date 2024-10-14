package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.OrderState;
import codingblackfemales.sotw.SimpleAlgoState;
import messages.order.Side;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


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

    @Before
    public void setUp() throws Exception {
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5());


    }

    @Test
    public void VWAPCalculation() throws Exception {//CHILD ORDDER AND ACRIVE CHILD ORDER

        //when
        var state = container.getState(); // Ensuring state is retrieved
        MyAlgoLogic algoLogic = new MyAlgoLogic();//instance of algo logic to call the test on
        long calculatedVWAP = algoLogic.calculateVWAP(state); // Capturing result of the calculation

        // then

        assertEquals("VWAP calculation is", calculatedVWAP, 0);

    }

    @Test
    public void volumeImbalanceIndicator() throws Exception {

        //when
        var state = container.getState(); // Ensuring state is retrieved
        MyAlgoLogic algoLogic = new MyAlgoLogic();
        double calculatedVolumeImbalanceIndication = algoLogic.calculateVolumeImbalance(state); // Capturing result of the calculation

        // then
        assertEquals("Volume Imbalance calculation is", calculatedVolumeImbalanceIndication,0.9607843137254902);

    }

    @Test
    public void ChildOrderSize() throws Exception {

        //when
        SimpleAlgoState state = container.getState();

        //then
        assertEquals(state.getChildOrders().size(), 7);
    }

    @Test
    public void activeChildOrderSize() throws Exception {

        //when
        SimpleAlgoState state = container.getState();

        //then
        assertEquals(state.getActiveChildOrders().size(), 7);
    }
    @Test
    public void buyOrderSize() throws Exception {

        //when
        SimpleAlgoState state = container.getState();

        //then
        assertEquals( state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.BUY)).toList().size(), 2);
    }
    @Test
    public void sellOrderSize() throws Exception {

        //when
        SimpleAlgoState state = container.getState();

        //then
        assertEquals( state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.SELL)).toList().size(), 7);

    }
    @Test
    public void cancelledOrderSize() throws Exception {

        //when
        SimpleAlgoState state = container.getState();

        //then
        assertEquals( state.getChildOrders().stream().filter(order -> order.getState() == OrderState.CANCELLED).toList().size(), 0);

    }
}
