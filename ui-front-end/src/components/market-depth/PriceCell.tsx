import React, { useRef, useEffect, useState } from 'react';
export interface PriceCellProps{
    prices:number;
    }
export const PriceCell =(props: PriceCellProps)=>{
    const lastPriceRef = useRef(props.price);
    //const [arrowPosition, setArrowPosition] =useState(0);
    const diff = props.price - lastPriceRef.current;
    useEffect(() =>{
  lastPriceRef.current =props.price;
    // Log the price difference for debugging
            console.log(`Price: ${props.price}, Last Price: ${lastPriceRef.current}, Diff: ${diff}`);


    }, [props.price]); // run when price changes

return (
        <div className="price-cell-container" style={{ display: 'flex', alignItems: 'center' }}>
            <span style={{ fontSize: '16px', marginRight: '5px' }}>
                {props.price} {/* Display the price */}
            </span>
            {/* Directly display the arrow based on the price difference */}
            {diff > 0 ? (
                <span className="arrow" style={{ fontSize: '10px',textAlign:"center" }}>
                    ↑ {/* Up arrow */}
                </span>
            ) : diff < 0 ? (
                <span className="arrow" style={{ fontSize: '10px',textAlign:"center" }}>
                    ↓ {/* Down arrow */}
                </span>
            ) : null}
        </div>
    );
};
