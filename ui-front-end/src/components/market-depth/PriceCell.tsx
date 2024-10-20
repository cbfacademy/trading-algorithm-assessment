export interface PriceCellProps {
    price: number;
}

export const PriceCell = (props: PriceCellProps) => {
    return <td>{props.price}</td>;
};
