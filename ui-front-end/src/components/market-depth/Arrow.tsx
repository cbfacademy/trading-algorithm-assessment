// Arrow.tsx
import React from 'react';

interface ArrowProps {
  direction: 'up' | 'down'; // Specify direction of the arrow
}

export const Arrow: React.FC<ArrowProps> = ({ direction }) => {
  return (
    <span className={`arrow ${direction}`}>
      {direction === 'up' ? '↑' : '↓'}
    </span>
  );
};

