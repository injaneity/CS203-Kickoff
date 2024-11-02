import React, { forwardRef, ReactNode } from 'react';

interface ScrollAreaProps {
  children: ReactNode;
  className?: string;
}

const ScrollArea = forwardRef<HTMLDivElement, ScrollAreaProps>(
  ({ children, className }, ref) => {
    return (
      <div
        ref={ref}
        className={`overflow-y-auto max-h-full ${className}`}
      >
        {children}
      </div>
    );
  }
);

ScrollArea.displayName = "ScrollArea";

export { ScrollArea };