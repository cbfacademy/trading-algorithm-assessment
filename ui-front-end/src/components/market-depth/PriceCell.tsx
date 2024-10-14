import { useRef, useEffect } from "react";
import "./PriceCell.css";
export interface PriceCellProps {
  price: number;
  isBid?: boolean;
  showPercentage?: boolean;
  // New prop to conditionally show percentage difference
}

export const PriceCell = (props: PriceCellProps) => {
  const lastValueRef = useRef(props.price);
  const diff = props.price - lastValueRef.current;
  const percentageDiff = (diff / lastValueRef.current) * 100;

  useEffect(() => {
    lastValueRef.current = props.price;
  }, [props.price]);

  const color = diff > 0 ? "green" : diff < 0 ? "red" : "black";
  const formattedPrice = props.price.toFixed(2); // Format price to two decimal places

  return (
    <td className={`price-cell ${props.isBid ? "bid" : "offer"}`}>
      {props.showPercentage ? (
        <span
          className="percentage"
          style={{
            color,
          }}
        >
          ({percentageDiff.toFixed(2)}%)
        </span>
      ) : (
        <>
          {props.isBid ? (
            <span className="price-arrow" style={{ color }}>
              {diff > 0 ? "▲" : diff < 0 ? "▼" : null}
              {formattedPrice}
            </span>
          ) : (
            <span className="price-arrow" style={{ color }}>
              {formattedPrice}
              {diff > 0 ? "▲" : diff < 0 ? "▼" : null}
            </span>
          )}
        </>
      )}
    </td>
  );
};
