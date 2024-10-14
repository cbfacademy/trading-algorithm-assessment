package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.sotw.marketdata.AskLevel;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySellForProfitLogic {

    private static final Logger logger = LoggerFactory.getLogger(MySellForProfitLogic.class);

    public Action executeSell(long entryPrice, long bestBidPrice, SimpleAlgoState state) {
        double profitTarget = entryPrice * 1.00033;  // 0.033% profit target

        if (entryPrice > 0 && bestBidPrice > profitTarget) {
            AskLevel topAsk = state.getAskAt(0);
            logger.info("[MYALGO] Selling to take profit at " + bestBidPrice);
            return new CreateChildOrder(Side.SELL, topAsk.getQuantity(), topAsk.getPrice());
        }

        return NoAction.NoAction;
    }
}
