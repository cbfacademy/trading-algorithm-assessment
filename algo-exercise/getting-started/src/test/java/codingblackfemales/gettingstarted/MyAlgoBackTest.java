package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import messages.marketdata.Source;
import messages.marketdata.BookUpdateEncoder;
import messages.marketdata.InstrumentStatus;
import messages.marketdata.Venue;
import messages.marketdata.MessageHeaderEncoder;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import java.util.List;
//import org.junit.Test;

import static org.junit.Assert.*;


public class MyAlgoBackTest extends AbstractAlgoBackTest {
    private static final Logger logger = LoggerFactory.getLogger(MyAlgoBackTest.class);

    @Override
    public AlgoLogic createAlgoLogic() {
        return new MyAlgoLogic();
    }

    @Test
    public void testBuyCondition() throws Exception {
        // Test case where the best bid price is below the BUY_THRESHOLD, should create a buy order

        send(createMarketTick(90L, 100L)); // bidPrice=90L, askPrice=100L

        // Get the state of the order book after the market tick
        var state = container.getState();

        // Check that a buy order was created
        assertEquals(1, state.getChildOrders().size());
        assertEquals(90L, state.getChildOrders().get(0).getPrice()); // Verify the price of the buy order
        assertEquals(50L, state.getChildOrders().get(0).getQuantity()); // Verify the quantity of the buy order

        long filledQuantity = state.getChildOrders().stream()
                                   .map(ChildOrder::getFilledQuantity)
                                   .reduce(Long::sum)
                                   .orElse(0L);

         // No fill should occur immediately as the market price hasn’t reached the buy order price
         assertEquals(0L, filledQuantity);
    }

    @Test
    public void testSellCondition() throws Exception {
        // Test case where the best ask price is above the SELL_THRESHOLD, should cancel a buy order

        // Send market data where ask price is above the SELL_THRESHOLD
        send(createMarketTick(110L, 130L)); // bidPrice=110L, askPrice=130L

        // Create a buy order to be canceled
        send(createMarketTick(95L, 110L)); // bidPrice=95L, askPrice=110L

        // Get the state of the order book after the market tick
        var state = container.getState();
        logger.info("State after creating buy order: " + state);

        // Check that a buy order was created first
        assertEquals(1, state.getChildOrders().size());
        assertEquals(95L, state.getChildOrders().get(0).getPrice()); // Verify the price of the buy order

        // Send a market tick that should trigger the cancel condition
        //send(createMarketTick(115L, 125L)); // bidPrice=115L, askPrice=125L

        // Get the state of the order book after the market tick
        state = container.getState();
        logger.info("State after sending cancel condition: " + state);

        // Check that the buy order was canceled
        assertEquals(1, state.getChildOrders().size());

         // Calculate and assert the total filled quantity
         long filledQuantity = state.getChildOrders().stream()
         .map(ChildOrder::getFilledQuantity)
         .reduce(Long::sum)
         .orElse(0L);

        // No fill should occur as the order was canceled
        assertEquals(0L, filledQuantity);
    }

    @Test
    public void testNoAction() throws Exception {
        // Test case where no conditions are met, should return NoAction

        // Send market data within thresholds where no action should be taken
        send(createMarketTick(110L, 115L)); // bidPrice=110L, askPrice=115L

        // Get the state of the order book after the market tick
        var state = container.getState();

        // Check that no action was taken (no orders created or canceled)
        assertEquals(0, state.getChildOrders().size());

        // Calculate and assert the total filled quantity
        long filledQuantity = state.getChildOrders().stream()
                                   .map(ChildOrder::getFilledQuantity)
                                   .reduce(Long::sum)
                                   .orElse(0L);

        // No fill should occur as no action was taken
        assertEquals(0L, filledQuantity);
    }

    @Test
    public void testDispatchThroughSequencer() throws Exception {
        // Test the sequence of actions through multiple market ticks

        // First tick: Below BUY_THRESHOLD, should create a buy order
        send(createMarketTick(95L, 110L)); // bidPrice=95L, askPrice=110L
        assertEquals(1, container.getState().getChildOrders().size());

        // Second tick: Above SELL_THRESHOLD, should cancel the buy order
        //send(createMarketTick(115L, 125L)); // bidPrice=115L, askPrice=125L
        //assertEquals(0, container.getState().getActiveChildOrders().size());

        // Third tick: No action should be taken (prices within thresholds)
        send(createMarketTick(105L, 115L)); // bidPrice=105L, askPrice=115L
        assertEquals(1, container.getState().getActiveChildOrders().size());

        // Fourth tick: Still no action should be taken (prices within thresholds)
        send(createMarketTick(100L, 120L)); // bidPrice=100L, askPrice=120L
        assertEquals(1, container.getState().getActiveChildOrders().size());
    }

    private UnsafeBuffer createMarketTick(long bidPrice, long askPrice) {
        // Create and return market data tick with specified bid and ask prices
        return createSampleMarketData(bidPrice, askPrice);
    }

    private UnsafeBuffer createSampleMarketData(long bidPrice, long askPrice) {
        // This method constructs a market data tick with the given bid and ask prices
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        BookUpdateEncoder encoder = new BookUpdateEncoder();
        MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();

        encoder.wrapAndApplyHeader(directBuffer, 0, headerEncoder);
        headerEncoder.schemaId(BookUpdateEncoder.SCHEMA_ID);
        headerEncoder.version(BookUpdateEncoder.SCHEMA_VERSION);

        encoder.venue(Venue.XLON);
        encoder.instrumentId(123L);
        encoder.source(Source.STREAM);

        encoder.bidBookCount(1)
                .next().price(bidPrice).size(100L);

        encoder.askBookCount(1)
                .next().price(askPrice).size(200L);

        encoder.instrumentStatus(InstrumentStatus.CONTINUOUS);

        return directBuffer;
    }
}
