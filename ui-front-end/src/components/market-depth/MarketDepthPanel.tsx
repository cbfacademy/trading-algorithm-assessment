import { MarketDepthRow } from "./useMarketDepthData.ts";
import "./MarketDepthPanel.css";

interface MarketDepthPanelProps {
    data: MarketDepthRow[];
}

export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
    // Logging props as instructions suggest (for debugging if needed)
    console.log({ props });

    return (
        <table className="market-depth-panel">
            <thead>
            <tr>
                <th rowSpan={2}></th>
                <th colSpan={2}>Bid</th>
                <th colSpan={2}>Ask</th>
            </tr>
            <tr>
                <th>Quantity</th>
                <th>Price</th>
                <th>Price</th>
                <th>Quantity</th>
            </tr>
            </thead>
            <tbody>
            {props.data.map((row: MarketDepthRow, index: number) => (
                <tr key={index}>
                    {/* Display the level of this data row */}
                    <td>{row.level}</td>

                    {/* Bid Quantity as a visual bar */}
                    <td>
                        <div className="bid-bar" style={{ width: `${row.bidQuantity / 100}%` }}>
                            {row.bidQuantity}
                        </div>
                    </td>

                    {/* Bid Price */}
                    <td>{row.bid}</td>

                    {/* Ask Price */}
                    <td>{row.offer}</td>

                    {/* Ask Quantity as a visual bar */}
                    <td>
                        <div className="ask-bar" style={{ width: `${row.offerQuantity / 100}%` }}>
                            {row.offerQuantity}
                        </div>
                    </td>
                </tr>
            ))}
            </tbody>
        </table>
    );
};
