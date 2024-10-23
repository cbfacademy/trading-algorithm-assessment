import { MarketDepthRow } from "./useMarketDepthData"; //type definition
import "./MarketDepthPanel.css";
import { PriceCell } from "./PriceCell";

interface MarketDepthPanelProps {
  data: MarketDepthRow[]; //defines the prop
}

export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
  //ask steve deconstruct data?
  console.log({ props });

  const quantities = props.data.map((row) => {
    return Math.max(row.bidQuantity, row.offerQuantity);
  }); //maps over arraw returning the maximum then store in quantity array

  const max = Math.max(...quantities); //finds maxium value and stores in max variable

  const percentageQuantity = (quantity: number, max: number) =>
    (quantity / max) * 100; //function to calculate percentage

  return (
    <>
      <h2>Algo Trading Data</h2>
      <div className="table-container">
        <table className="MarketDepthPanel">
          <thead>
            <tr>
              <th></th>
              <th colSpan={3}>Bid</th>
              <th colSpan={3}>Ask</th>
            </tr>
            <tr>
              <th></th>
              <th>Quantity</th>
              <th className="percentage-header">Bid % Change</th>
              <th className="price-header bid">Bid Price</th>
              <th className="price-header ask">Ask Price</th>
              <th className="percentage-header">Ask % Change</th>
              <th>Quantity</th>
            </tr>
          </thead>
          <tbody>
            {props.data.map(
              (
                row,
                index //maps over data array new for for each entry
              ) => (
                <tr key={index}>
                  {/*key for unique identification*/}
                  <td>{index}</td>
                  <td className="bid">
                    <div className="gauge-container">
                      <div
                        className="gauge-bar"
                        style={{
                          width: `${percentageQuantity(row.bidQuantity, max)}%`,
                        }}
                      ></div>
                      <span>{row.bidQuantity}</span>
                    </div>
                  </td>
                  <PriceCell
                    className="percentage-cell bid"
                    price={row.bid}
                    isBid={true}
                    showPercentage={true}
                  />
                  <PriceCell
                    price={row.bid}
                    isBid={true}
                    showPercentage={false}
                  />
                  <PriceCell
                    price={row.offer}
                    isBid={false}
                    showPercentage={false}
                  />
                  <PriceCell
                    className="percentage-cell ask"
                    price={row.offer}
                    isBid={false}
                    showPercentage={true}
                  />
                  <td className="ask">
                    <div className="gauge-container">
                      <div
                        className="gauge-bar"
                        style={{
                          width: `${percentageQuantity(
                            row.offerQuantity,
                            max
                          )}%`,
                        }}
                      ></div>
                      <span>{row.offerQuantity}</span>
                    </div>
                  </td>
                </tr>
              )
            )}
          </tbody>
        </table>
      </div>
    </>
  );
};
