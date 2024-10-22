package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AbstractLevel;
import codingblackfemales.sotw.marketdata.BidLevel;
import codingblackfemales.util.Util;
import messages.order.Side;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);
    
    private int marketDataTickCount = 0;
    private int priceDifferentiator = 0;
    private int quantityDifferentiator = 4;

    private AbstractLevel bestBidOrderInCurrentTick;
    private long bestBidPriceInCurrentTick;

    public AbstractLevel getBestBidOrderInCurrentTick() {
        return bestBidOrderInCurrentTick;
    }

    public long getBestBidPriceInCurrentTick() {
        return bestBidPriceInCurrentTick;
    }

    @Override
    public Action evaluate(SimpleAlgoState state) {

        var orderBookAsString = Util.orderBookToString(state);

        logger.info("[MYALGO] THIS IS TICK NUMBER: " + marketDataTickCount + "\n");
        marketDataTickCount += 1;

        bestBidOrderInCurrentTick = state.getBidAt(0);
        bestBidPriceInCurrentTick = bestBidOrderInCurrentTick.getPrice();

        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        if (state.getChildOrders().size() < 3) {
            priceDifferentiator += 1;
            int quantity = 100;
            quantityDifferentiator -= 1;
            return new CreateChildOrder(Side.BUY, (quantity * quantityDifferentiator), (getBestBidPriceInCurrentTick() - 3 + priceDifferentiator));
        } else {
            return NoAction.NoAction;
        }

    }
}
