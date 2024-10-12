package codingblackfemales.gettingstarted;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import messages.marketdata.BookUpdateEncoder;
import messages.marketdata.InstrumentStatus;
import messages.marketdata.MessageHeaderEncoder;
import messages.marketdata.Source;
import messages.marketdata.Venue;
import messages.order.Side;
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
public class MyProfitAlgoTest extends AbstractAlgoTest {

    @Override
    public AlgoLogic createAlgoLogic() {

        //this adds your profit algo logic to the container classes
        return new MyProfitAlgo();
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
    public void testAlgoBehaviourAfterFirstTick() throws Exception {
      
        send(createTick());  //create a sample market data tick from first tick

            //assert that we created 5 Buy orders for 200 shares at 100, and no Sell orders

            assertEquals(5, container.getState().getActiveChildOrders().size()); 
 
            if (!container.getState().getActiveChildOrders().isEmpty()) {

                for (ChildOrder childOrder : container.getState().getActiveChildOrders()) {

                        assertEquals(200, childOrder.getQuantity());
                        assertEquals(messages.order.Side.BUY, childOrder.getSide());
                        assertNotEquals(messages.order.Side.SELL, childOrder.getSide());
                        assertEquals(true, childOrder.getSide() == Side.BUY);
                        assertEquals(true, childOrder.getSide() != Side.SELL);
                        assertEquals(100, childOrder.getPrice());                

                        System.out.println("Order ID: " + childOrder.getOrderId() + 
                                        " | Side: " + childOrder.getSide() +            
                                        " | Price: " + childOrder.getPrice() +
                                        " | Ordered Qty: " + childOrder.getQuantity() +
                                        " | Filled Qty: " + childOrder.getFilledQuantity() +
                                        " | State of the order: " + childOrder.getState()
                                        );
                }

            }

            //Check total child orders count, total active child orders and total ordered quantity 

            int totalChildOrdersCountTick1 = container.getState().getChildOrders().size(); // added 15/9/2024
            int totalActiveChildOrdersTick1 = container.getState().getActiveChildOrders().size();
            long totalOrderedQuantityTick1 = container.getState().getActiveChildOrders()
                                            .stream()
                                            .map(ChildOrder::getQuantity).reduce (Long::sum)
                                            .orElse(0L);            
            // long totalOrderedQuantityTick1 = container.getState().getChildOrders()
            //                                             .stream()
            //                                             .map(ChildOrder::getQuantity).reduce (Long::sum).get();


            // assert that there is a total of 5 child orders, a total of 5 active child orders and total ordered quantity is 1000

            assertEquals(5, totalChildOrdersCountTick1);
            assertEquals(5, totalActiveChildOrdersTick1);
            assertEquals(1000, totalOrderedQuantityTick1);

    }


    //  //CHECKING THE ALGO'S BEHAVIOUR AFTER 2ND TICK

    @Test
    public void testAlgoBehaviourAfterSecondTick() throws Exception {
    
        send(createTick2());  //create a sample market data tick from second tick

            //assert that we created 5 Buy orders for 200 shares at 99, and no Sell orders

            assertEquals(5, container.getState().getActiveChildOrders().size()); 

            if (!container.getState().getActiveChildOrders().isEmpty()) {                  
                for (ChildOrder childOrder : container.getState().getActiveChildOrders()) {

                        assertEquals(200, childOrder.getQuantity());
                        assertEquals(messages.order.Side.BUY, childOrder.getSide());
                        assertNotEquals(messages.order.Side.SELL, childOrder.getSide());
                        assertEquals(true, childOrder.getSide() == Side.BUY);
                        assertEquals(true, childOrder.getSide() != Side.SELL);
                        assertEquals(99, childOrder.getPrice());                

                        System.out.println("Order ID: " + childOrder.getOrderId() + 
                                        " | Side: " + childOrder.getSide() +            
                                        " | Price: " + childOrder.getPrice() +
                                        " | Ordered Qty: " + childOrder.getQuantity() +
                                        " | Filled Qty: " + childOrder.getFilledQuantity() +
                                        " | State of the order: " + childOrder.getState()
                                        );
                }

            }

            //Check total child orders count, total active child orders and total ordered quantity

            int totalChildOrdersCountTick2 = container.getState().getChildOrders().size(); // added 15/9/2024
            int totalActiveChildOrdersTick2 = container.getState().getActiveChildOrders().size();
            long totalOrderedQuantityTick2 = container.getState().getActiveChildOrders()
                                            .stream()
                                            .map(ChildOrder::getQuantity).reduce (Long::sum)
                                            .orElse(0L);            
            // long totalOrderedQuantityTick1 = container.getState().getChildOrders()
            //                                             .stream()
            //                                             .map(ChildOrder::getQuantity).reduce (Long::sum).get();


            // assert that there is a total of 5 child orders, a total of 5 active child orders and total ordered quantity is 1000

            assertEquals(5, totalChildOrdersCountTick2);
            assertEquals(5, totalActiveChildOrdersTick2);
            assertEquals(1000, totalOrderedQuantityTick2);

    }

        
    //CHECKING THE ALGO'S BEHAVIOUR AFTER 3RD TICK
    @Test
    public void testAlgoBehaviourAfterThirdTick() throws Exception {
    
        send(createTick3());  //create a sample market data tick from third tick

            //assert that we created 5 Buy orders for 200 shares at 98, and no Sell orders

            assertEquals(5, container.getState().getActiveChildOrders().size()); 

            if (!container.getState().getActiveChildOrders().isEmpty()) {                  
                for (ChildOrder childOrder : container.getState().getActiveChildOrders()) {

                        assertEquals(200, childOrder.getQuantity());
                        assertEquals(messages.order.Side.BUY, childOrder.getSide());
                        assertNotEquals(messages.order.Side.SELL, childOrder.getSide());
                        assertEquals(true, childOrder.getSide() == Side.BUY);
                        assertEquals(true, childOrder.getSide() != Side.SELL);
                        assertEquals(98, childOrder.getPrice());                

                        System.out.println("Order ID: " + childOrder.getOrderId() + 
                                        " | Side: " + childOrder.getSide() +            
                                        " | Price: " + childOrder.getPrice() +
                                        " | Ordered Qty: " + childOrder.getQuantity() +
                                        " | Filled Qty: " + childOrder.getFilledQuantity() +
                                        " | State of the order: " + childOrder.getState()
                                        );
                }

            }

            //Check total child orders count, total active child orders and total ordered quantity

            int totalChildOrdersCountTick3 = container.getState().getChildOrders().size(); // added 15/9/3024
            int totalActiveChildOrdersTick3 = container.getState().getActiveChildOrders().size();
            long totalOrderedQuantityTick3 = container.getState().getActiveChildOrders()
                                            .stream()
                                            .map(ChildOrder::getQuantity).reduce (Long::sum)
                                            .orElse(0L);            
            // long totalOrderedQuantityTick1 = container.getState().getActiveChildOrders()
            //                                  .stream()
            //                                  .map(ChildOrder::getQuantity).reduce (Long::sum).get();


            // assert that there is a total of 5 child orders, a total of 5 active child orders and total ordered quantity is 1000

            assertEquals(5, totalChildOrdersCountTick3);
            assertEquals(5, totalActiveChildOrdersTick3);
            assertEquals(1000, totalOrderedQuantityTick3);

    }
   
 } 
 
// USE EITHER OF THE BELOW CODE TO RUN MYPROFITALGOTEST FROM A WINDOWS MACHINE WITH MAVEN INSTALLED
// mvn test -pl :getting-started -DMyProfitAlgoTest 
// mvn clean test --projects algo-exercise/getting-started
// mvn -Dtest=MyProfitAlgoTest test --projects algo-exercise/getting-started
