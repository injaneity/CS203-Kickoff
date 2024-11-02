import * as React from "react";
import * as Slider from "@radix-ui/react-slider";

interface EloRangeSliderProps {
  value: [number, number];
  onValueChange: (value: [number, number]) => void;
}

export function EloRangeSlider({ value, onValueChange }: EloRangeSliderProps) {
  return (
    <div className="flex flex-col space-y-2">
      <div className="flex justify-between items-center">
        <span className="text-sm whitespace-nowrap">ELO Range</span>
        <span className="text-sm whitespace-nowrap ml-2">{value[0]} - {value[1]}</span>
      </div>
      <Slider.Root
        className="relative flex items-center select-none touch-none w-full h-5"
        value={value}
        onValueChange={onValueChange}
        max={3000}
        step={50}
        minStepsBetweenThumbs={1}
      >
        <Slider.Track className="bg-gray-700 relative grow rounded-full h-2">
          <Slider.Range className="absolute bg-blue-600 rounded-full h-full" />
        </Slider.Track>
        <Slider.Thumb className="block w-5 h-5 bg-white rounded-full hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 focus:ring-offset-gray-800" />
        <Slider.Thumb className="block w-5 h-5 bg-white rounded-full hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 focus:ring-offset-gray-800" />
      </Slider.Root>
    </div>
  );
}