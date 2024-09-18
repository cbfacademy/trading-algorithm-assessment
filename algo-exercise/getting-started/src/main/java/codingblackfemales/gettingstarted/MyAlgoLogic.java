package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.marketdata.gen.RandomMarketDataGenerator;
import codingblackfemales.orderbook.channel.MarketDataChannel;
import codingblackfemales.service.MarketDataService;
import codingblackfemales.service.OrderService;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.OrderState;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.plaf.nimbus.State;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    //private static final long SELL_THRESHOLD = 500; // SELL when price goes above this

    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);


        final BidLevel bidLevel = state.getBidAt(0);
        final long bidPrice = bidLevel.price;
        final long bidQuantity = bidLevel.quantity;

        final AskLevel askLevel = state.getAskAt(0);
        final long askPrice = askLevel.price;
        final long askQuantity = askLevel.quantity;

        //capture current size of order before creating new one
        // int currentOrderSize = state.getChildOrders().size();// capture current size of order before creating new one
        // logger.info("[MYALGO] Current order size is :" + currentOrderSize );

        // seperate childorder into BUY AND SELL
        long buyOrdersCount = state.getChildOrders().stream().filter(ChildOrder -> ChildOrder.getSide() == Side.BUY).count();
        long sellOrdersCount = state.getChildOrders().stream().filter(ChildOrder -> ChildOrder.getSide() == Side.SELL).count();


        //check if there are few than 5 child orders
       // if (state.getChildOrders().size() < 5) {
            if (buyOrdersCount < 4) { // create new Buy order if fewer than 5 child order exist
                // If less than 4 BUY orders, create a new BUY order
                logger.info("[PASSIVEALGO] Have: " + buyOrdersCount + " orders. ADD New BUY ORDER " + bidQuantity + " @ " + bidPrice);
                //  int initialOrderSize = state.getChildOrders().size();// capture current size of order before creating new one
                return new CreateChildOrder(Side.BUY, bidQuantity, bidPrice); // Create a new BUY order
            }
 //If no actions are taken, return NoAction
           //   return NoAction.NoAction;
           // }

            if (sellOrdersCount < 3) { //create new sell order if fewer than 5 child order exist
              logger.info("[PASSIVEALGO] Ha: " + sellOrdersCount + " orders. ADD New ASK ORDER " + askQuantity + " @ " + askPrice);
            //int initialOrderSize = state.getChildOrders().size();// capture current size of order before creating new one
              return new CreateChildOrder(Side.SELL, askQuantity, askPrice); // Create a new child order
               }

// If no actions are taken, return NoAction
            return NoAction.NoAction;
        }
    }
















