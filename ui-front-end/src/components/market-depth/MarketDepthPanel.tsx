import { MarketDepthRow } from "./useMarketDepthData";
import "./MarketDepthPanel.css"; // Import the new CSS file


interface MarketDepthPanelProps {
    data: MarketDepthRow[];
  }
 
  export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
    const { data } = props; // from copilot
    
    return (
      <div className='marketDepthPanel'> {/*parent containter*/}
          <table className="marketDepthPanel-table">
            <thead>
              <tr>
                <th></th> {/* Centered level column */}
                <th colSpan="2" className="centered-header">Bid</th> {/* Span over bid and bidQuantity */}
                <th colSpan="2" className="centered-header">Ask</th> {/* Span over offer and offerQuantity */}
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
              {data.map((row, index) => ( // Ensure 10 rows. From chat GPT
                <tr key={index}>
                  <td>{row.level}</td> 
                  <td>{row.bidQuantity}</td>                   
                  <td>{row.bid}</td>                
                  <td>{row.offer}</td>
                  <td>{row.offerQuantity}</td>
                </tr>
              ))}
            </tbody>        
          </table>      
        </div>
    );
  }
