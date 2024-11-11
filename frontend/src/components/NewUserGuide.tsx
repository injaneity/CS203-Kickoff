"use client"

import React, { useState } from "react"
import { Button } from "./ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "./ui/card"
import { Input } from "./ui/input"
import { Label } from "./ui/label"
import { PlayerPosition } from '../types/profile'
import { ChevronLeft, ChevronRight, User, Users } from "lucide-react"
import { motion } from "framer-motion"


interface NewUserGuideProps {
  onComplete: (description: string, preferredPositions: PlayerPosition[]) => void
  onSkip: () => void
}

export default function Component({ onComplete, onSkip }: NewUserGuideProps = {
  onComplete: () => { },
  onSkip: () => { },
}) {
  const [description, setDescription] = useState("")
  const [step, setStep] = useState(1)
  const [preferredPositions, setPreferredPositions] = useState<PlayerPosition[]>([])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (step === 1 && description.trim()) {
      setStep(2)
    } else if (step === 2) {
      onComplete(description, preferredPositions)
    }
  }

  const handlePreferredPositionsChange = (position: PlayerPosition) => {
    setPreferredPositions((prevPositions) =>
      prevPositions.includes(position)
        ? prevPositions.filter((pos) => pos !== position)
        : [...prevPositions, position]
    )
  }

  const formatPosition = (position: string) => {
    return position.replace("POSITION_", "").charAt(0) + position.replace("POSITION_", "").slice(1).toLowerCase()
  }

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <Card className="w-full max-w-2xl bg-background border-none shadow-2xl">
        <form onSubmit={handleSubmit}>
          <CardHeader>
            <CardTitle className="text-3xl font-bold text-center">Welcome to Our Soccer App!</CardTitle>
            <CardDescription className="text-center">Let's set up your profile in a few quick steps.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <motion.div
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.3 }}
            >
              {step === 1 ? (
                <div className="space-y-4">
                  <div className="flex items-center space-x-2">
                    <User className="w-6 h-6 text-primary" />
                    <Label htmlFor="description" className="text-xl font-semibold">
                      Tell us about yourself:
                    </Label>
                  </div>
                  <Input
                    id="description"
                    placeholder="I've been playing soccer since high school and love being a midfielder."
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="h-24"
                  />
                </div>
              ) : (
                <div className="space-y-6">
                  <div className="flex items-center space-x-2">
                    <Users className="w-6 h-6 text-primary" />
                    <Label className="text-xl font-semibold">Select Your Preferred Positions:</Label>
                  </div>
                  <CardDescription>You can choose multiple positions.</CardDescription>
                  <div className="grid grid-cols-2 gap-4">
                    {Object.values(PlayerPosition).map((position) => (
                      <button
                        key={position}
                        type="button"
                        onClick={() => handlePreferredPositionsChange(position)}
                        className={`flex items-center space-x-3 p-4 rounded-lg border-2 transition-all duration-200 ${preferredPositions.includes(position)
                            ? "border-primary bg-blue-600  text-primary"
                            : "border-muted bg-gray-700/50 hover:border-primary/50"
                          }`}
                      >
                        <div
                          className={`w-4 h-4 rounded-sm border transition-colors ${preferredPositions.includes(position)
                              ? "bg-primary border-primary"
                              : "border-muted-foreground"
                            }`}
                        >
                          {preferredPositions.includes(position) && (
                            <svg
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth="3"
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              className="w-4 h-4 text-primary-foreground"
                            >
                              <polyline points="20 6 9 17 4 12" />
                            </svg>
                          )}
                        </div>
                        <span className="flex-1 text-left font-medium">{formatPosition(position)}</span>
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </motion.div>
          </CardContent>
          <CardFooter className="flex justify-between items-center">
            {step === 1 ? (
              <Button variant="ghost" onClick={onSkip}>
                Skip
              </Button>
            ) : (
              <Button
                variant="ghost"
                onClick={(event) => {
                  event.preventDefault()
                  setStep(1)
                }}
              >
                <ChevronLeft className="w-4 h-4 mr-2" />
                Back
              </Button>
            )}
            <div className="flex items-center space-x-4">
              <div className="text-sm text-muted-foreground">Step {step} of 2</div>
              <Button
                type="submit"
                disabled={step === 1 && !description.trim()}
                variant={step === 1 && !description.trim() ? "secondary" : "default"}
                className={step === 1 && !description.trim() ? "opacity-50 cursor-not-allowed" : ""}
              >
                {step === 1 ? (
                  <>
                    Next
                    <ChevronRight className="w-4 h-4 ml-2" />
                  </>
                ) : (
                  "Finish"
                )}
              </Button>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  )
}