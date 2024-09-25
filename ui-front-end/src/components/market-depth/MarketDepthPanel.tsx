import React, { useEffect, useState } from 'react';
import { Arrow } from './Arrow';
import './Arrow.css';
import './MarketDepthPanel.css';
import { MarketDepthRow } from './useMarketDepthData';

interface MarketDepthPanelProps {
  data: MarketDepthRow[];
}

export const MarketDepthPanel: React.FC<MarketDepthPanelProps> = ({ data }) => {
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
            <th></th>
            <th colSpan="2" className="centered-header">Bid</th> {/* Span over bidQuantity and Price */}
            <th colSpan="2" className="centered-header">Ask</th> {/* Span over offer and offerQuantity */}
          </tr>
          <tr>
            <th>Level</th>
            <th>Quantity</th>
            <th>Price</th>
            <th>Price</th>
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

                {/* Bid Price with dynamic arrow */}
                <td className="price-cell">
                  <span className="arrow">
                    <Arrow direction={bidArrowDirection} />
                  </span>
                  <span className="price">{row.bid}</span> {/* Bid Price */}
                </td>

                {/* Ask Price with dynamic arrow */}
                <td className="price-cell">
                  <span className="price">{row.offer}</span> {/* Ask Price */}
                  <span className="arrow">
                    <Arrow direction={offerArrowDirection} />
                  </span>
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




// ORIGINAL CODE BEFORE THE ARROW 

// import { MarketDepthRow } from "./useMarketDepthData";
// import "./MarketDepthPanel.css"; // Import the new CSS file
// import { Arrow } from './Arrow'; // Import the Arrow component
// interface MarketDepthPanelProps {
//     data: MarketDepthRow[];
//   }
//  export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
//      const { data } = props; // from copilot
//   return (
//     <div className='marketDepthPanel'> {/*parent containter*/}
//         <table className="marketDepthPanel-table">
//           <thead>
//             <tr>
//               <th></th> {/* Centered level column */}
//               <th colSpan="2" className="centered-header">Bid</th> {/* Span over bid and bidQuantity */}
//               <th colSpan="2" className="centered-header">Ask</th> {/* Span over offer and offerQuantity */}
//             </tr>              
//             <tr>
//               <th></th>
//               <th>Quantity</th>                                
//               <th>Price</th>                
//               <th>Price</th>
//               <th>Quantity</th>
//             </tr>
//           </thead>
//           <tbody>
//             {data.map((row, index) => ( // Ensure 10 rows. From chat GPT
//               <tr key={index}>
//                 <td>{row.level}</td> 
//                 <td>{row.bidQuantity}</td>                   
//                 <td>{row.bid}</td>                
//                 <td>{row.offer}</td>
//                 <td>{row.offerQuantity}</td>
//               </tr>
//             ))}
//           </tbody>        
//         </table>      
//       </div>
//   );
// }

