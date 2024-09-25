import { MarketDepthRow } from "./useMarketDepthData";
import { MarketDepthPanelCss } from './MarketDepthPanelCss';

interface MarketDepthPanelProps {
    data: MarketDepthRow[];
  }
  
  export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
    console.log({ props });
    return <div />;
     <table></table>;
  };
