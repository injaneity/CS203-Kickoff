import { Button } from "./ui/button"
import { Card, CardContent, CardFooter } from "./ui/card"

interface ClubCardProps {
  name: string
  description: string
  members: number
  image: string
  applied: boolean
}

export default function ClubCard({ name, description, members, image, applied }: ClubCardProps) {
  return (
    <Card className="bg-gray-800 border-gray-700">
      <CardContent className="p-0">
        <img src={image} alt={name} className="w-full h-48 object-cover rounded-t-lg" />
        <div className="p-4">
          <h3 className="text-lg font-semibold text-white">{name}</h3>
          <p className="text-sm text-gray-300">{description}</p>
        </div>
      </CardContent>
      <CardFooter className="flex justify-between items-center p-4 border-t border-gray-700">
        <div className="flex items-center space-x-2 text-gray-300">
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          <span>{members} Members</span>
        </div>
        <Button variant={applied ? "secondary" : "default"}>
          {applied ? "Applied" : "Join"}
        </Button>
      </CardFooter>
    </Card>
  )
}