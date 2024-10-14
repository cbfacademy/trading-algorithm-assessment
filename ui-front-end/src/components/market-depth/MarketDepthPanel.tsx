import { MarketDepthRow } from "./useMarketDepthData";
import "./MarketDepthPanel.css";
import { PriceCell } from "./PriceCell";

interface MarketDepthPanelProps {
  data: MarketDepthRow[];
}

export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
  console.log({ props });

  const quantities = props.data.map((row) => {
    return Math.max(row.bidQuantity, row.offerQuantity);
  });

  const max = Math.max(...quantities);

  const percentageQuantity = (quantity: number, max: number) =>
    (quantity / max) * 100;

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
            {props.data.map((row, index) => (
              <tr key={index}>
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
                <td className="percentage-cell bid">
                  <PriceCell
                    price={row.bid}
                    isBid={true}
                    showPercentage={true}
                  />
                </td>
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
                <td className="percentage-cell ask">
                  <PriceCell
                    price={row.offer}
                    isBid={false}
                    showPercentage={true}
                  />
                </td>
                <td className="ask">
                  <div className="gauge-container">
                    <div
                      className="gauge-bar"
                      style={{
                        width: `${percentageQuantity(row.offerQuantity, max)}%`,
                      }}
                    ></div>
                    <span>{row.offerQuantity}</span>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
};
