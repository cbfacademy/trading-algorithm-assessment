import { useMarketDepthData } from "./useMarketDepthData";
import { schemas } from "../../data/algo-schemas";

/**
 * TODO
 */
export const MarketDepthFeature = () => {
  const data = useMarketDepthData(schemas.prices);

  // Check if data is available
  if (!data) {
    return <div>Loading...</div>;
  }

  return (
    <div>
      <h2>Trading</h2>
      <table style={{ width: "100%"}}>
        <thead>
          <tr>
          <th colSpan={1}></th>
            <th colSpan={2} style={{ textAlign: "center" }}>Bid</th>
            <th colSpan={2} style={{ textAlign: "center" }}>Ask</th>
          </tr>
          <tr>
            <th></th>
            <th>Quantity</th>
            <th>Price</th>
            <th>Price</th>
            <th>Quantity</th>
          </tr>
        </thead>
        <tbody>
          {data.map((row, index) => (
            <tr key={index}>
              {/* Bid side */}
              <td style={{ fontSize: "12px", textAlign:"center" }}> {row.level}</td>
              <td style={{ backgroundColor: "#007bff", color: "white", textAlign:"center" }}>
                {row.bidQuantity} 
              </td>
              <td style={{ fontSize: "12px", textAlign:"center" }}>
              <span>↑</span> {row.bid}
              </td>

              {/* Ask side */}
              <td style={{ fontSize: "12px", textAlign:"center" }}>
              <span>↑</span> {row.offer}<span>↑</span>
              </td>
              <td style={{ backgroundColor: "#dc3545", color: "white",textAlign:"center" }}>
                {row.offerQuantity} 
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
