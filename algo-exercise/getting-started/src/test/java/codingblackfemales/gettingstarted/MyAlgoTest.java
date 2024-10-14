package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import messages.order.Side;
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
    public void testAlgoNeverExceedsMaxOrderCount() throws Exception {

        for (int i = 0; i <= 20; i++) { //as long as number of orders is below 20, create buy tick


            send(createTick());
        }
        assertTrue(container.getState().getChildOrders().size() <= 20);
    }

    @Test
    public void testAlgoCreatesBuyOrder() throws Exception {

        for (int i = 0; i <= 6; i++) {
            //create a sample market data tick....
            send(createTick());
        }
        //simple assert to check we had 3 orders created
        assertEquals(3, container.getState().getChildOrders().size());
    }

    @Test
    public void testAlgoExecutesStopLoss() throws Exception {
        //stop loss should do two things: cancel existing buy orders and sell any it might have immediately
        //Using a for loop to avoid sending multiple ticks using a loop
        for (int i = 0; i < 6; i++) {
            send(createTickStopLoss());
        }
        //assertion that this orderS was canceled after buying
        assertEquals(0, container.getState().getChildOrders().size());
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

