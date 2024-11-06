import { MarketDepthRow } from "./useMarketDepthData";
import "./MarketDepthPanel.css";

interface MarketDepthPanelProps {
  data: MarketDepthRow[];
}

export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
  console.log({ props });

  return (
    <div className="MarketDepthPanel">
      <table className="MarketDepthPanel-table">
        <thead>
          <tr>
            <th></th>
            <th colSpan={2} className="MarketDepthPanel-table-heading">
              Bid
            </th>
            <th colSpan={2} className="MarketDepthPanel-table-heading">
              Ask
            </th>
          </tr>
          <tr>
            <th></th>
            <th className="MarketDepthPanel-table-heading">Quantity</th>
            <th className="MarketDepthPanel-table-heading">Price</th>
            <th className="MarketDepthPanel-table-heading">Price</th>
            <th className="MarketDepthPanel-table-heading">Quantity</th>
          </tr>
        </thead>

        <tbody>
          {Array.from({ length: 10 }, (_, index) => (
            <tr key={index} className="MarketDepthPanel-level">
              <td>{index}</td>
              <td></td>
              <td></td>
              <td></td>
              <td></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};