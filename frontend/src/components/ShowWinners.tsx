import React, { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Button } from "./ui/button";
import { Club } from '../types/club';

interface ShowWinnersProps {
  winningClub: Club;
  onClose: () => void;
}

const ShowWinners: React.FC<ShowWinnersProps> = ({ winningClub, onClose }) => {
  const [showPodium, setShowPodium] = useState(false);
  const videoRef = useRef<HTMLVideoElement>(null);

  useEffect(() => {
    const video = videoRef.current;
    if (video) {
      video.onended = () => setShowPodium(true);
      video.play();
    }
  }, []);

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 bg-black bg-opacity-80 flex items-center justify-center z-50"
    >
      <div className="w-full h-full flex flex-col items-center justify-center">
        {!showPodium && (
          <video
            ref={videoRef}
            className="w-full h-full object-cover"
            src="/assets/WinningPage.mp4"
            muted
          />
        )}
        
        <AnimatePresence>
          {showPodium && (
            <motion.div
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ duration: 0.5 }}
              className="bg-gray-800 p-8 rounded-lg shadow-lg text-center"
            >
              <h2 className="text-3xl font-bold mb-4">Tournament Winner</h2>
              <div className="podium mb-6">
                <motion.div
                  initial={{ height: 0 }}
                  animate={{ height: 200 }}
                  transition={{ duration: 1, delay: 0.5 }}
                  className="bg-yellow-400 w-64 mx-auto relative"
                >
                  <motion.img
                    initial={{ y: 50, opacity: 0 }}
                    animate={{ y: 0, opacity: 1 }}
                    transition={{ duration: 0.5, delay: 1.5 }}
                    src={`https://picsum.photos/seed/${winningClub.id}/200/200`}
                    alt={winningClub.name}
                    className="w-32 h-32 rounded-full absolute top-0 left-1/2 transform -translate-x-1/2 -translate-y-1/2"
                  />
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ duration: 0.5, delay: 2 }}
                    className="absolute bottom-4 left-0 right-0 text-center"
                  >
                    <h3 className="text-2xl font-bold">{winningClub.name}</h3>
                    <p className="text-xl">ELO: {winningClub.elo.toFixed(0)}</p>
                  </motion.div>
                </motion.div>
              </div>
              <Button onClick={onClose} className="mt-4">
                Continue
              </Button>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </motion.div>
  );
};

export default ShowWinners;