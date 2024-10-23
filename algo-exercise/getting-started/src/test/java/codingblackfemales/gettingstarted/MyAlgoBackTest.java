package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.OrderState;
import codingblackfemales.sotw.SimpleAlgoState;
import messages.order.Side;
import org.junit.Before;
import org.junit.Test;
import codingblackfemales.sotw.ChildOrder;

import static junit.framework.TestCase.assertEquals;

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
    public void competitiveBuyOrderCreationTest() throws Exception {

        send(createTick());//initial tick
        send(createTick2());//these two ticks allow the algorithm to meet the two filled child order requirement

        SimpleAlgoState state = container.getState();
        final var buyOrderCount = state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.BUY)).toList().size();
        final var filledStateCount = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED).toList().size();

        //ADD asserts when you have implemented your algo logic
        assertEquals("Filled order size is 2",filledStateCount, 2);//asserts that the algorithm has reached the two filled order requirement and can now make competitive orders
        assertEquals("active Buy order count is",buyOrderCount,3);//asserts the buy order size is 3 before tick

        //when: the market has an imbalance and increased pressure on the sell side a buy order is created if the best ask is greater than or equal to the VWAP benchmark

        send(createTick3());//this tick tests the algorithms logic and reaction to volume imbalance (best bid volume =100, best ask volume = 200)
        long newChildOrderCount = state.getChildOrders().size();
        final var newBuyOrderCount = state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.BUY)).toList().size();

        assertEquals("active Buy order count has increased by 1",newBuyOrderCount,4);//asserts active buy orders have increased by 1

    }

    @Test
    public void competitiveSellOrderCreationTest() throws Exception {

        send(createTick());//initial tick
        send(createTick2());//these two ticks allows the algorithm to meet the minimum two filled child order requirement so any new orders will be created competitively

        SimpleAlgoState state = container.getState();
        final var sellOrderCount =  state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.SELL)).toList().size();
        final var filledStateCount = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED).toList().size();

        //ADD asserts when you have implemented your algo logic
        assertEquals("Filled order size is 2",filledStateCount, 2);//asserts that the algorithm has reached the two filled order requirement and can now make competitive orders
        assertEquals("active Sell order count is",sellOrderCount,0);//asserts the sell order size is 0 before tick

        //when: the market has an imbalance with increased pressure on the buy side a sell order is created if the best bid is less or equal to the VWAP benchmark

        send(createTick3());//this tick tests the algorithms logic and reaction to volume imbalance (best bid volume =400, best ask volume = 200)

        final var newSellOrderCount = state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.SELL)).toList().size();
        assertEquals("active Sell order count has increased by 1",newSellOrderCount,1);//asserts sell orders have increased by 1

    }
    @Test
    public void  buyOrderCancellationTest() throws Exception {

        SimpleAlgoState state = container.getState();

        final var cancelledBuyOrdersCount = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.CANCELLED && order.getSide() == Side.BUY).count();

        //ADD asserts when you have implemented your algo logic
        assertEquals("Cancelled buy order count is 0",cancelledBuyOrdersCount, 0);//asserts that the cancelled buy order count is 0 before new tick

        //when: the market is indicating volatility due to the bid ask spread going over 4.5%. orders must be cancelled
        send(createTick());
        send(createTick2());// the tickets simulate a volatile market in which my algorithm dictates that non-viable orders should be cancelled to ensure best prices.

        final var  newCancelledBuyOrdersCount = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.CANCELLED && order.getSide() == Side.BUY).count();
        //then: get the state
        assertEquals("Cancelled buy order count has increased by 1 ",newCancelledBuyOrdersCount,1);//asserts that the cancelled buy order count is 1 after new tick

    }
    @Test
    public void  sellOrderCancellationTest() throws Exception {
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());


        SimpleAlgoState state = container.getState();

        final var cancelledSellOrdersCount = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.CANCELLED && order.getSide() == Side.SELL).count();

        //ADD asserts when you have implemented your algo logic
        assertEquals("Cancelled sell order count is 0",cancelledSellOrdersCount, 0);//asserts that the cancelled sell order count is 0 before new tick

        //when: the market  indicates volatility due to the bid ask spread going over 4.5%.  orders must be cancelled

        send(createTick5());// the ticket simulates a volatile market in which my algorithm dictates that non-viable orders should be cancelled to ensure best prices.

        final var  newCancelledSellOrdersCount = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.CANCELLED && order.getSide() == Side.SELL).count();
        //then: get the state
        assertEquals("cancelled sell order count has increased by 1",newCancelledSellOrdersCount,1);//asserts that the cancelled sell order count is 1 after new tick

    }

    @Test
    public void filledStateQuantityUpdateTest() throws Exception {
        //create a sample market data tick....
        send(createTick());//initial ticks
        send(createTick2());
        send(createTick3());

        SimpleAlgoState state = container.getState();
        final var filledStateCount = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED).toList().size();
        final var filledQuantity = state.getChildOrders().stream().mapToLong(ChildOrder::getFilledQuantity).sum();


        //ADD asserts when you have implemented your algo logic
        assertEquals("Filled state order count is 2",filledStateCount, 2);//asserts that the algorithm has reached the two filled orders
        assertEquals("Filled order quantity is ",filledQuantity,602);//asserts the filled quantity  is 602 before new ticks


        send(createTick4());//this ticks simulate conditions for orders to fill
        send(createTick5());
        final var newFilledStateCount = state.getChildOrders().stream().filter(order -> order.getState() == OrderState.FILLED).toList().size();
        final var newFilledQuantity = state.getChildOrders().stream().mapToLong(ChildOrder::getFilledQuantity).sum();

        assertEquals("New filled state order count is 4",newFilledStateCount, 4);//asserts that filled state count is updated when orders are filled
        assertEquals("New filled order quantity is ",newFilledQuantity,712);//asserts the filled quantity is updated to reflect the volume of the new fill orders


    }

//Unit Tests



    @Test
    public void ChildOrderSize() throws Exception {
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5());


        //when
        SimpleAlgoState state = container.getState();

        //then
        assertEquals(state.getChildOrders().size(), 6);//asserting that 6 Child Orders have been created
    }

    @Test
    public void activeChildOrderSize() throws Exception {
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5());


        //when
        SimpleAlgoState state = container.getState();

        //then
        assertEquals(state.getActiveChildOrders().size(), 4);
    }
    @Test
    public void VWAPCalculation() throws Exception {
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5());


        //when
        var state = container.getState(); // Ensuring state is retrieved
        MyAlgoLogic algoLogic = new MyAlgoLogic();//instance of algo logic to call the test on
        double calculatedVWAP = algoLogic.calculateVWAP(state); // Capturing result of the calculation

        // then

        assertEquals("VWAP calculation is", calculatedVWAP, 109.0);

    }

    @Test
    public void volumeImbalanceIndicator() throws Exception {
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5());


        //when
        var state = container.getState(); // Ensuring state is retrieved
        MyAlgoLogic algoLogic = new MyAlgoLogic();
        double calculatedVolumeImbalanceIndication = algoLogic.calculateVolumeImbalance(state); // Capturing result of the calculation

        // then
        assertEquals("Volume Imbalance calculation is", calculatedVolumeImbalanceIndication,0.42857142857142855);

    }
    @Test
    public void buyOrderSize() throws Exception {
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5());


        //when
        SimpleAlgoState state = container.getState();

        //then
        assertEquals( state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.BUY)).toList().size(), 4);
    }
    @Test
    public void sellOrderSize() throws Exception {
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5());


        //when
        SimpleAlgoState state = container.getState();

        //then
        assertEquals( state.getActiveChildOrders().stream().filter(order -> order.getSide().equals(Side.SELL)).toList().size(), 0);

    }
    @Test
    public void cancelledOrderSize() throws Exception {
        send(createTick());
        send(createTick2());
        send(createTick3());
        send(createTick4());
        send(createTick5());


        //when
        SimpleAlgoState state = container.getState();

        //then
        assertEquals( state.getChildOrders().stream().filter(order -> order.getState() == OrderState.CANCELLED).toList().size(), 2);

    }
}






