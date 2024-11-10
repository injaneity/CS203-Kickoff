'use client'

import React, { useState } from 'react'
import { Button } from '../components/ui/button'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '../components/ui/card'
import { Input } from '../components/ui/input'
import Label from '../components/ui/label'
import { PlayerPosition } from '../types/profile'
import { motion } from 'framer-motion'
import { ChevronLeft, ChevronRight, User, Users } from 'lucide-react'

interface NewUserGuideProps {
  onComplete: (description: string, preferredPositions: PlayerPosition[]) => void
  onSkip: () => void
}

export default function NewUserGuide({ onComplete, onSkip }: NewUserGuideProps) {
  const [description, setDescription] = useState('')
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
    return position.replace('POSITION_', '').charAt(0) + position.replace('POSITION_', '').slice(1).toLowerCase()
  }

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <Card className="w-full max-w-2xl bg-gray-800 text-white">
        <form onSubmit={handleSubmit}>
          <CardHeader>
            <CardTitle className="text-3xl font-bold text-center">Welcome to Our Soccer App!</CardTitle>
            <CardDescription className="text-center text-gray-300">Let's set up your profile in a few quick steps.</CardDescription>
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
                    <User className="w-6 h-6 text-blue-400" />
                    <Label htmlFor="description" className="text-xl font-semibold text-white">Tell us about yourself:</Label>
                  </div>
                  <Input
                    id="description"
                    placeholder="I've been playing soccer since high school and love being a midfielder."
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="h-24 bg-gray-700 text-white border-gray-600 focus:border-blue-400"
                  />
                </div>
              ) : (
                <div className="space-y-4">
                  <div className="flex items-center space-x-2">
                    <Users className="w-6 h-6 text-blue-400" />
                    <Label className="text-xl font-semibold text-white">Select Your Preferred Positions:</Label>
                  </div>
                  <CardDescription className="text-gray-300">You can choose multiple positions.</CardDescription>
                  <div className="grid grid-cols-2 gap-3">
                    {Object.values(PlayerPosition).map((position) => (
                      <label key={position} className="flex items-center space-x-2 p-2 rounded-md bg-gray-700 hover:bg-gray-600 transition-colors">
                        <input
                          type="checkbox"
                          checked={preferredPositions.includes(position)}
                          onChange={() => handlePreferredPositionsChange(position)}
                          className="form-checkbox h-4 w-4 text-blue-600 rounded"
                        />
                        <span className="text-white">{formatPosition(position)}</span>
                      </label>
                    ))}
                  </div>
                </div>
              )}
            </motion.div>
          </CardContent>
          <CardFooter className="flex justify-between items-center">
            {step === 1 ? (
              <Button variant="ghost" onClick={onSkip} className="text-gray-300 hover:text-white">
                Skip
              </Button>
            ) : (
              <Button variant="ghost" onClick={() => setStep(1)} className="text-gray-300 hover:text-white">
                <ChevronLeft className="w-4 h-4 mr-2" />
                Back
              </Button>
            )}
            <div className="flex items-center space-x-4">
              <div className="text-sm text-gray-400">
                Step {step} of 2
              </div>
              <Button type="submit" disabled={step === 1 && !description.trim()} className="bg-blue-600 hover:bg-blue-700">
                {step === 1 ? (
                  <>
                    Next
                    <ChevronRight className="w-4 h-4 ml-2" />
                  </>
                ) : (
                  'Finish'
                )}
              </Button>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  )
}