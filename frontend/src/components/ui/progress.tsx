import * as React from "react";
import { Trophy } from "lucide-react"; // Importing the trophy icon

interface ProgressProps extends React.HTMLAttributes<HTMLDivElement> {
  value: number;
}

const Progress = React.forwardRef<HTMLDivElement, ProgressProps>(
  ({ value, className, ...props }, ref) => {
    const percentage = Math.min(100, Math.max(0, value));

    return (
      <div className="relative w-full" {...props}>
        {/* Trophy icon positioned on top of the progress bar */}
        <Trophy
          className="absolute text-yellow-400 h-4 w-4" // h-5 w-5 to make the trophy smaller
          style={{
            top: '-26px', // Adjust to place the icon above the bar
            left: `calc(${percentage}% - 10px)` // Adjust to center the icon based on progress
          }}
        />
        <div
          ref={ref}
          role="progressbar"
          aria-valuemin={0}
          aria-valuemax={100}
          aria-valuenow={percentage}
          className={`relative w-full overflow-hidden rounded-full bg-gray-700 ${className}`}
        >
          <div
            className="h-full w-full flex-1 bg-blue-600 transition-all"
            style={{ transform: `translateX(-${100 - percentage}%)` }}
          />
        </div>
      </div>
    );
  }
);

Progress.displayName = "Progress";

export { Progress };
