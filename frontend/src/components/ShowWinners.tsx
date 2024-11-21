import React, { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Club } from '../types/club';
import videoSource from '../assets/WinningPage.mp4';
import { Button } from "./ui/button";

interface ShowWinnersProps {
  winningClub: Club;
  onClose: () => void;
}

const ShowWinners: React.FC<ShowWinnersProps> = ({ winningClub, onClose }) => {
  const [showWinnerText, setShowWinnerText] = useState(false);
  const videoRef = useRef<HTMLVideoElement>(null);

  useEffect(() => {
    const video = videoRef.current;
    if (video) {
      video.muted = true;
      video.playsInline = true;
      video.play().catch((error) => {
        console.error('Video playback failed:', error);
      });

      const handleVideoEnd = () => {
        video.pause();
        setShowWinnerText(true);
      };
      video.addEventListener('ended', handleVideoEnd);

      return () => {
        video.removeEventListener('ended', handleVideoEnd);
      };
    }
  }, []);

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 bg-black bg-opacity-80 flex items-center justify-center z-50"
    >
      <div className="relative w-full h-full flex flex-col items-center justify-center">
        <video
          ref={videoRef}
          className="absolute inset-0 w-full h-full object-cover"
          src={videoSource}
          muted
          playsInline
          controls={false}
          onClick={() => {
            if (videoRef.current) {
              videoRef.current.currentTime = videoRef.current.duration;
            }
          }}
        />

        {!showWinnerText && (
          <motion.div
            className="absolute top-1/4 text-center text-white"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 1 }}
          >
            <h2 className="text-4xl md:text-6xl font-bold">
              The tournament winner goes to...
            </h2>
          </motion.div>
        )}

        <AnimatePresence>
          {showWinnerText && (
            <motion.div
              initial={{ opacity: 0, scale: 0.8 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.5 }}
              className="absolute flex flex-col items-center text-center text-white"
            >
              {/* Club details at the top */}
              <div className="flex flex-col items-center mb-4">
                <img
                  src={`https://picsum.photos/seed/${winningClub.id}/200/200`}
                  alt={winningClub.name}
                  className="w-32 h-32 rounded-full mb-2"
                />
                <h3 className="text-3xl font-bold">{winningClub.name}</h3>
                <p className="text-xl">ELO: {winningClub.elo.toFixed(0)}</p>
              </div>

              {/* Podium-style yellow box */}
              <div className="bg-yellow-400 p-16 rounded-lg relative mb-4">
                <div className="absolute text-black text-6xl font-bold inset-0 flex items-center justify-center">
                  🥇
                </div>
              </div>

              {/* Tournament Winner text below the podium */}
              <h2 className="text-4xl font-bold mt-4">Tournament Winner</h2>

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
