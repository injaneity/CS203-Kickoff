import React from 'react';

interface SliderProps {
  selected: string;
  onChange: (value: string) => void;
}

const Slider: React.FC<SliderProps> = ({ selected, onChange }) => {
  return (
    <div className="flex items-center justify-center space-x-2">
      <div
        onClick={() => onChange('yes')}
        className={`cursor-pointer px-6 py-2 rounded-lg transition-colors duration-200 ${
          selected === 'yes'
            ? 'bg-green-600 text-white font-bold'
            : 'bg-gray-300 text-gray-600'
        }`}
      >
        Yes
      </div>
      <div
        onClick={() => onChange('no')}
        className={`cursor-pointer px-6 py-2 rounded-lg transition-colors duration-200 ${
          selected === 'no'
            ? 'bg-red-600 text-white font-bold'
            : 'bg-gray-300 text-gray-600'
        }`}
      >
        No
      </div>
    </div>
  );
};

export default Slider;
