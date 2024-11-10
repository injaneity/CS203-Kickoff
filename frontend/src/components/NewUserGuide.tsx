import React, { useState } from 'react';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from './ui/card';
import { Input } from './ui/input';
import Label from './ui/label';
import { PlayerPosition } from '../types/profile';
import { useNavigate } from 'react-router-dom';

interface NewUserGuideProps {
  onComplete: (description: string, preferredPositions: PlayerPosition[]) => void;
  onSkip: () => void;
}

export default function NewUserGuide({ onComplete, onSkip }: NewUserGuideProps) {
  const [description, setDescription] = useState('');
  const [step, setStep] = useState(1);
  const [preferredPositions, setPreferredPositions] = useState<PlayerPosition[]>([]);
  const navigate = useNavigate();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (step === 1 && description.trim()) {
      setStep(2);
    } else if (step === 2) {
      onComplete(description, preferredPositions);
      navigate('/clubs');
    }
  };

  // Handle preferred positions change
  const handlePreferredPositionsChange = (position: PlayerPosition) => {
    setPreferredPositions((prevPositions) =>
      prevPositions.includes(position)
        ? prevPositions.filter((pos) => pos !== position)
        : [...prevPositions, position]
    );
  };

  const formatPosition = (position: string) => {
    return position.replace('POSITION_', '').charAt(0) + position.replace('POSITION_', '').slice(1).toLowerCase();
  };

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <Card className="w-full max-w-2xl relative">
        <form onSubmit={handleSubmit}>
          <CardHeader>
            <CardTitle className="text-2xl font-bold">Welcome to Our App!</CardTitle>
            <CardDescription>Let's get you started with a few quick steps.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            {step === 1 ? (
              <div className="space-y-2">
                <Label htmlFor="description" className="text-white">Tell us about yourself (Profile Description):</Label>
                <Input
                  id="description"
                  placeholder="I have played soccer in my secondary school and occasionally in University"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="h-24"
                />
              </div>
            ) : (
              <div className="space-y-6">
                <h2 className="text-xl font-semibold text-white mb-2">Indicate Your Preferred Positions
                <   CardDescription>You can choose more than one position.</CardDescription>
                </h2>
                <div className="flex flex-wrap">
                  {Object.values(PlayerPosition).map((position) => (
                    <label key={position} className="mr-4 mb-2 flex items-center">
                      <input
                        type="checkbox"
                        checked={preferredPositions.includes(position)}
                        onChange={() => handlePreferredPositionsChange(position)}
                        className="form-checkbox h-4 w-4 text-blue-600"
                      />
                      <span className="ml-2 text-white">{formatPosition(position)}</span>
                    </label>
                  ))}
                </div>
              </div>
            )}
          </CardContent>
          <CardFooter className="flex justify-between items-center">
            <div className="text-sm text-muted-foreground">
              Step {step} / 2
            </div>
            <Button type="submit" disabled={step === 1 && !description.trim()}>
              {step === 1 ? 'Next' : 'Finish'}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
