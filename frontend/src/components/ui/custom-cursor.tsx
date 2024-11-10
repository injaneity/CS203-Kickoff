import { useEffect, useState } from "react"
import { motion, AnimatePresence } from "framer-motion"
import SoccerBallImage from "../../../public/kickoff.png";


export default function CustomCursor() {
  const [position, setPosition] = useState({ x: 0, y: 0 })
  const [trail, setTrail] = useState<{ x: number; y: number; id: number }[]>([])

  useEffect(() => {
    const updateCursor = (e: MouseEvent) => {
      setPosition({ x: e.clientX, y: e.clientY })
      setTrail((prev) => [
        { x: e.clientX, y: e.clientY, id: Date.now() },
        ...prev.slice(0, 10), // Keep only 6 trail points
      ])
    }

    document.addEventListener("mousemove", updateCursor)
    return () => document.removeEventListener("mousemove", updateCursor)
  }, [])

  useEffect(() => {
    // Hide the default cursor
    document.body.style.cursor = "none"
    return () => {
      document.body.style.cursor = "auto"
    }
  }, [])

  return (
    <div className="pointer-events-none fixed inset-0 z-50">
      {/* Soccer ball cursor */}
      <motion.div
        className="absolute w-8 h-8"
        style={{
          x: position.x - 16, // Center the cursor (half of width)
          y: position.y - 16, // Center the cursor (half of height)
        }}
      >
        <img
          src={SoccerBallImage}
          alt="Soccer Ball"
          className="w-full h-full"
        />
      </motion.div>

      {/* Trail effect */}
      <AnimatePresence>
        {trail.map((point) => (
          <motion.div
            key={point.id}
            initial={{ scale: 1, opacity: 0.5 }}
            animate={{ scale: 0.5, opacity: 0 }}
            exit={{ scale: 0, opacity: 0 }}
            transition={{ duration: 0.5, ease: "easeOut" }}
            style={{
              position: "absolute",
              left: point.x,
              top: point.y,
              transform: "translate(-50%, -50%)",
              filter: "brightness(2)", // Increase brightness here
            }}
            className="w-4 h-4 rounded-full bg-blue-500/50 blur-sm"
          />
        ))}
      </AnimatePresence>
    </div>
  )
}
