import React, { useEffect, useState } from 'react';
import { Arrow } from './Arrow';
import './MyExtraArrow.css';
import './MyExtraMarketDepthPanel.css';
import { MarketDepthRow } from './useMarketDepthData';

interface MyExtraMarketDepthPanelProps {
  data: MarketDepthRow[];
}

export const MyExtraMarketDepthPanel: React.FC<MyExtraMarketDepthPanelProps> = ({ data }) => {
  // State to store previous prices for each row
  const [previousPrices, setPreviousPrices] = useState<{ [key: number]: { bid: number; offer: number } }>({});

  // Track previous prices on data update
  useEffect(() => {
    const newPreviousPrices = data.reduce((acc, row, index) => {
      acc[index] = {
        bid: previousPrices[index]?.bid || row.bid,
        offer: previousPrices[index]?.offer || row.offer,
      };
      return acc;
    }, {} as { [key: number]: { bid: number; offer: number } });

    setPreviousPrices(newPreviousPrices);
  }, [data]);

  return (
    <div className='marketDepthPanel'>
      <table className="marketDepthPanel-table">
        <thead>
          <tr>
            {/* Original code */}
            <th></th> 
            <th colSpan="3" className="centered-header">Bid</th> {/* Span over bid Quantity, bid Price and bid Arrow */}
            <th colSpan="3" className="centered-header">Ask</th> {/* Span over ask Quantity, ask Price and ask Arrow */}
          </tr>
          <tr>
            <th></th> 
            <th>Quantity</th>  
            <th></th>
            <th>Price</th>                
            <th>Price</th>    
            <th></th>
            <th>Quantity</th>
          </tr>
        </thead>
        <tbody>
          {data.map((row, index) => {
            // Compare current and previous prices
            const prevBid = previousPrices[index]?.bid || row.bid;
            const prevOffer = previousPrices[index]?.offer || row.offer;

            const bidArrowDirection = row.bid > prevBid ? 'up' : 'down';
            const offerArrowDirection = row.offer > prevOffer ? 'up' : 'down';

            return (
              <tr key={index}>
                <td>{row.level}</td>
                <td>{row.bidQuantity}</td>

                {/* New column for the Bid Arrow */}
                <td>
                  <Arrow direction={bidArrowDirection} />
                </td>

                {/* Bid Price */}
                {/*<td className="price-cell">*/}
                <td className={`price-cell ${row.bid > prevBid ? 'price-up' : 'price-down'}`}>
                  <span className="price">{row.bid}</span>
                </td>

                {/* Ask Price */}
                {/*<td className="price-cell">*/}
                <td className={`price-cell ${row.offer > prevOffer ? 'price-up' : 'price-down'}`}>
                  <span className="price">{row.offer}</span>
                </td>

                {/* New column for the Ask Arrow */}
                <td>
                  <Arrow direction={offerArrowDirection} />
                </td>

                <td>{row.offerQuantity}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};


// export const MarketDepthPanel: React.FC<MarketDepthPanelProps> = ({ data }) => {
//   // State to store previous prices for each row
//   const [previousPrices, setPreviousPrices] = useState<{ [key: number]: { bid: number; offer: number } }>({});

//   // Track previous prices on data update
//   useEffect(() => {
//     const newPreviousPrices = data.reduce((acc, row, index) => {
//       acc[index] = {
//         bid: previousPrices[index]?.bid || row.bid,
//         offer: previousPrices[index]?.offer || row.offer,
//       };
//       return acc;
//     }, {} as { [key: number]: { bid: number; offer: number } });

//     setPreviousPrices(newPreviousPrices);
//   }, [data]);

//   return (
//     <div className='marketDepthPanel'>
//       <table className="marketDepthPanel-table">
//         <thead>
//           <tr>
//             <th></th>
//             <th colSpan="2" className="centered-header">Bid</th> {/* Span over bid Quantity, bid Price */}
//             <th colSpan="2" className="centered-header">Ask</th> {/* Span over ask Quantity, ask price */}
//           </tr>
//           <tr>
//             <th></th>
//             <th>Quantity</th>
//             <th>Price</th>
//             <th>Price</th>
//             <th>Quantity</th>
//           </tr>
//         </thead>
//         <tbody>
//           {data.map((row, index) => {
//             // Compare current and previous prices
//             const prevBid = previousPrices[index]?.bid || row.bid;
//             const prevOffer = previousPrices[index]?.offer || row.offer;

//             const bidArrowDirection = row.bid > prevBid ? 'up' : 'down';
//             const offerArrowDirection = row.offer > prevOffer ? 'up' : 'down';

//             return (
//               <tr key={index}>
//                 <td>{row.level}</td>
//                 <td>{row.bidQuantity}</td>

//                 {/* Bid Price with dynamic arrow */}
//                 {/*<td className="price-cell">*/}
//                 <td className={`price-cell ${row.bid > prevBid ? 'price-up' : 'price-down'}`}> {/* ammended code to include background colours in bid and ask prices when they move up and down*/}
//                   <span className="arrow">
//                     <Arrow direction={bidArrowDirection} />
//                   </span>
//                   <span className="price">{row.bid}</span> {/* Bid Price */}
//                 </td>

//                 {/* Ask Price with dynamic arrow */}
//                 {/*<td className="price-cell">*/}
//                 <td className={`price-cell ${row.offer > prevOffer ? 'price-up' : 'price-down'}`}> {/* ammended code to include background colours in bid and ask prices when they move up and down*/}
//                   <span className="price">{row.offer}</span> {/* Ask Price */}
//                   <span className="arrow">
//                     <Arrow direction={offerArrowDirection} />
//                   </span>
//                 </td>

//                 <td>{row.offerQuantity}</td>
//               </tr>
//             );
//           })}
//         </tbody>
//       </table>
//     </div>
//   );
// };