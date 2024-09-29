// ORIGINAL CODE - using text-based arrows
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



// Ammended code - This one is using arrows from Font Awesome Icons but they did not work as they were pointing up
// import React from 'react';
// import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
// import { faArrowUp, faArrowDown } from '@fortawesome/free-solid-svg-icons';

// interface ArrowProps {
//   direction: 'up' | 'down'; // Specify direction of the arrow
// }

// export const Arrow: React.FC<ArrowProps> = ({ direction }) => {
//   console.log('Arrow direction:', direction); // Check the value  
//   return (
//     <span className={`arrow ${direction}`}>
//       {direction === 'up' ? (
//         <FontAwesomeIcon icon={faArrowUp} />
//       ) : (
//         <FontAwesomeIcon icon={faArrowDown} />
//       )}
//     </span>
//   );
// };
