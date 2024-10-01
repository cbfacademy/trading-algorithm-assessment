import { PriceCell } from './PriceCell';
import { MarketDepthRow } from '../type';

interface MarketDepthPanelProps {
    data: MarketDepthRow[];
  }
  
  export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
    const { data } = props;  // Destructure the data prop
  console.log({ data });  // This will log test data to the console for inspection

  return (
    <table className="MarketDepthPanel">
      <thead>
        <tr>
          <th>Level</th>
          <th>Bid</th>
          <th>Bid Quantity</th>
          <th>Offer</th>
          <th>Offer Quantity</th>
        </tr>
      </thead>
      <tbody>
      {data.map((row, index) => (
    <tr key={index}>
      <td>{row.level}</td>
      <PriceCell price={row.bid} />
      <td>{row.bidQuantity}</td>
      <PriceCell price={row.offer} />
      <td>{row.offerQuantity}</td>
    </tr>
        ))}
      </tbody>
    </table>
  );
  };