package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import messages.marketdata.BookUpdateEncoder;
import messages.marketdata.InstrumentStatus;
import messages.marketdata.MessageHeaderEncoder;
import messages.marketdata.Source;
import messages.marketdata.Venue;
import org.agrona.concurrent.UnsafeBuffer;
import java.nio.ByteBuffer;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


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

   protected UnsafeBuffer createTick2(){
        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();


        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        //write the encoded output to the direct buffer
        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        //set the fields to desired values
        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);
        encoder.source(Source.STREAM);

        encoder.askBookCount(4)
                .next().price(99L).size(101L)
                .next().price(110L).size(200L)
                .next().price(115L).size(5000L)
                .next().price(119L).size(5600L);        

        encoder.bidBookCount(3)
                .next().price(98L).size(100L)
                .next().price(95L).size(200L)
                .next().price(91L).size(300L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);

        return directBuffer;
    }

    protected UnsafeBuffer createTick3(){

        final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        final BookUpdateEncoder encoder = new BookUpdateEncoder();

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        //write the encoded output to the direct buffer
        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);

        //set the fields to desired values
        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);
        encoder.source(Source.STREAM);

        encoder.askBookCount(4)
                .next().price(98L).size(501L)
                .next().price(101L).size(200L)
                .next().price(110L).size(5000L)
                .next().price(119L).size(5600L);

        encoder.bidBookCount(3)
                .next().price(95L).size(100L)
                .next().price(93L).size(200L)
                .next().price(91L).size(300L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);

        return directBuffer;
    }

    //CHECKING THE ALGO'S BEHAVIOUR AFTER 1ST TICK

    @Test
    public void testDispatchThroughSequencerFirstTestTick1() throws Exception {

        //create a sample market data tick from first tick
        send(createTick());

            //assert to check we had 5 orders created
            assertEquals(5, container.getState().getChildOrders().size());


            //assert to check that we created a Buy order for 200 shares at 100 and no Sell orders
            
            for (ChildOrder childOrder : container.getState().getChildOrders()) {

                assertEquals(messages.order.Side.BUY, childOrder.getSide());
                assertNotEquals(messages.order.Side.SELL, childOrder.getSide());
                assertEquals(200, childOrder.getQuantity());
                assertEquals(100, childOrder.getPrice());

                System.out.println("Order ID: " + childOrder.getOrderId() + 
                                " | Side: " + childOrder.getSide() +            
                                " | Price: " + childOrder.getPrice() +
                                " | Ordered Qty: " + childOrder.getQuantity() +
                                " | Filled Qty: " + childOrder.getFilledQuantity() +
                                " | State of the order: " + childOrder.getState());
            }

    }


    //CHECKING THE ALGO'S BEHAVIOUR AFTER 2ND TICK

    @Test
    public void testDispatchThroughSequencerSecondTestTick2() throws Exception {

       //create a sample market data tick from second tick
       send(createTick2());

           //assert to check we had 5 orders created
           assertEquals(container.getState().getChildOrders().size(), 5);


           //assert to check that we created a Buy order for 200 shares at 99 and no Sell orders
           
           for (ChildOrder childOrder : container.getState().getChildOrders()) {

               assertEquals(childOrder.getSide(), messages.order.Side.BUY);
               assertNotEquals(childOrder.getSide(), messages.order.Side.SELL);
               assertEquals(childOrder.getQuantity(),  200);
               assertEquals(childOrder.getPrice(),  99);
               assertEquals(childOrder.getFilledQuantity(),  0);

               System.out.println("Order ID: " + childOrder.getOrderId() + 
                               " | Side: " + childOrder.getSide() +            
                               " | Price: " + childOrder.getPrice() +
                               " | Ordered Qty: " + childOrder.getQuantity() +
                               " | Filled Qty: " + childOrder.getFilledQuantity() +
                               " | State of the order: " + childOrder.getState());
        }

    }


        
    //check that our algo state was updated when the market data changed

    @Test
    public void testDispatchThroughSequencerThirdTestTick2() throws Exception {
     
        //create a sample market data tick from second tick
        send(createTick2());

        var state = container.getState();         

        int totalOrdersCountTick2 = container.getState().getChildOrders().size(); // added 15/9/2024
        int activeChildOrdersTick2 = container.getState().getActiveChildOrders().size();
        int cancelledChildOrdersTick2 = totalOrdersCountTick2 - activeChildOrdersTick2;

        long totalOrderedQuantityTick2 = state.getChildOrders().stream().map(ChildOrder::getQuantity).reduce (Long::sum).get();            
        long filledQuantityTick2 = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).get();
        long unFilledQuantityTick2 = totalOrderedQuantityTick2 - filledQuantityTick2;

        //Check total quantity ordered, filled quantity, unfilled quantity, cancelled order count etc....
        assertEquals(1000, totalOrderedQuantityTick2);            
        assertEquals(0, filledQuantityTick2);
        assertEquals(1000, unFilledQuantityTick2);           
        assertEquals(0, cancelledChildOrdersTick2);

    }

    //CHECKING THE ALGO'S BEHAVIOUR AFTER 3RD TICK

    @Test
    public void testDispatchThroughSequencerFourthTestTick3() throws Exception {

        //create a sample market data tick from 3rd tick
        send(createTick3());

            //assert to check we had 5 orders created
            assertEquals(container.getState().getChildOrders().size(), 5);        

            //assert to check that we created a Buy order for 200 shares at 98 and no sell orders           
            for (ChildOrder childOrder : container.getState().getChildOrders()) {

                assertEquals(childOrder.getSide(), messages.order.Side.BUY);
                assertNotEquals(childOrder.getSide(), messages.order.Side.SELL);
                assertEquals(childOrder.getQuantity(),  200);
                assertEquals(childOrder.getPrice(),  98);
                assertEquals(childOrder.getFilledQuantity(),  0);

                System.out.println("Order ID: " + childOrder.getOrderId() + 
                                " | Side: " + childOrder.getSide() +            
                                " | Price: " + childOrder.getPrice() +
                                " | Ordered Qty: " + childOrder.getQuantity() +
                                " | Filled Qty: " + childOrder.getFilledQuantity() +
                                " | State of the order: " + childOrder.getState());
            }

    }



    @Test
    public void testDispatchThroughSequencerFifthTestTick3() throws Exception {
        
        //create a sample market data tick from 3rd tick
        send(createTick3());
        var state = container.getState();


        int totalOrdersCountTick3 = container.getState().getChildOrders().size(); // added 15/9/2024
        int activeChildOrdersTick3 = container.getState().getActiveChildOrders().size();
        int cancelledChildOrdersTick3 = totalOrdersCountTick3 - activeChildOrdersTick3;

        long totalOrderedQuantityTick3 = state.getChildOrders().stream().map(ChildOrder::getQuantity).reduce (Long::sum).get();            
        long filledQuantityTick3 = state.getChildOrders().stream().map(ChildOrder::getFilledQuantity).reduce(Long::sum).get();
        long unFilledQuantityTick3 = totalOrderedQuantityTick3 - filledQuantityTick3;

        //Check total quantity ordered, filled quantity, unfilled quantity, cancelled order count etc....
        assertEquals(1000, totalOrderedQuantityTick3);            
        assertEquals(0, filledQuantityTick3);
        assertEquals(1000, unFilledQuantityTick3);           
        assertEquals(0, cancelledChildOrdersTick3);

    }   

}


// ORIGINAL CODE
// package codingblackfemales.gettingstarted;

// import codingblackfemales.algo.AlgoLogic;
// import org.junit.Test;


// /**
//  * This test is designed to check your algo behavior in isolation of the order book.
//  *
//  * You can tick in market data messages by creating new versions of createTick() (ex. createTick2, createTickMore etc..)
//  *
//  * You should then add behaviour to your algo to respond to that market data by creating or cancelling child orders.
//  *
//  * When you are comfortable you algo does what you expect, then you can move on to creating the MyAlgoBackTest.
//  *
//  */
// public class MyAlgoTest extends AbstractAlgoTest {

//     @Override
//     public AlgoLogic createAlgoLogic() {
//         //this adds your algo logic to the container classes
//         return new MyAlgoLogic();
//     }


//     @Test
//     public void testDispatchThroughSequencer() throws Exception {

//         //create a sample market data tick....
//         send(createTick());

//         //simple assert to check we had 3 orders created
//         //assertEquals(container.getState().getChildOrders().size(), 3);
//     }
// }

// mvn test -pl :getting-started -DMyAlgoTest 
// mvn -Dtest=MyAlgoTest test --projects algo-exercise/getting-started