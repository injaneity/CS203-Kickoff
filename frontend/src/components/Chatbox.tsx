'use client'

import { useState, useEffect, useRef } from 'react'
import { Button } from "./ui/button"
import { ScrollArea } from "./ui/scroll-area"
import { MessageCircle, X, GripHorizontal } from 'lucide-react'

const chatBotURL = import.meta.env.VITE_CHATBOT_API_URL || 'http://18.141.196.214';

export default function Chatbot() {
  const [isOpen, setIsOpen] = useState(false)
  const [messages, setMessages] = useState<{ role: string, content: string }[]>([
    { role: 'bot', content: 'Hello! I\'m your personal assistant regarding all things Kickoff. How can I help you today?' }
  ])
  const [input, setInput] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isTyping, setIsTyping] = useState(false)
  const [height, setHeight] = useState(384) // Initial height (24rem = 384px)
  const [isResizing, setIsResizing] = useState(false)
  const scrollAreaRef = useRef<HTMLDivElement>(null)
  const chatboxRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    if (scrollAreaRef.current) {
      scrollAreaRef.current.scrollTop = scrollAreaRef.current.scrollHeight
    }
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages, isTyping])

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (isResizing && chatboxRef.current) {
        const newHeight = chatboxRef.current.getBoundingClientRect().bottom - e.clientY
        setHeight(Math.max(200, newHeight)) // Minimum height of 200px
      }
    }

    const handleMouseUp = () => {
      setIsResizing(false)
    }

    if (isResizing) {
      document.addEventListener('mousemove', handleMouseMove)
      document.addEventListener('mouseup', handleMouseUp)
    }

    return () => {
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
    }
  }, [isResizing])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!input.trim()) return

    const newMessages = [...messages, { role: 'user', content: input }]
    setMessages(newMessages)
    setInput('')
    setIsLoading(true)
    setIsTyping(true)
    scrollToBottom()

    try {
      const response = await fetch(chatBotURL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ query: input }),
      })

      if (!response.ok) {
        throw new Error('Network response was not ok')
      }
      
      const data = await response.json();
      setMessages([...newMessages, { role: 'bot', content: data.response }])
    } catch (error) {
      console.error('Error:', error)
      setMessages([...newMessages, { role: 'bot', content: 'Sorry, there was an error processing your request.' }])
    } finally {
      setIsLoading(false)
      setIsTyping(false)
      scrollToBottom()
    }
  }

  return (
    <div className="fixed bottom-6 right-6 z-50">
      {isOpen ? (
        <div 
          ref={chatboxRef}
          className="bg-gray-800 rounded-lg shadow-xl w-80 flex flex-col text-white"
          style={{ height: `${height}px` }}
        >
          <div 
            className="flex justify-between items-center p-4 border-b border-gray-700 cursor-ns-resize"
            onMouseDown={(e) => {
              if (e.target === e.currentTarget) {
                setIsResizing(true);
              }
            }}
          >
            <div className="flex items-center">
              <GripHorizontal className="h-4 w-4 mr-2 text-gray-400" />
              <h2 className="text-lg font-semibold">Kickoff Assistant</h2>
            </div>
            <Button 
              variant="ghost" 
              size="icon" 
              onClick={(e) => {
                e.stopPropagation();
                setIsOpen(false);
              }} 
              onMouseDown={(e) => e.stopPropagation()}
              className="text-gray-400 hover:text-white"
            >
              <X className="h-4 w-4" />
            </Button>
          </div>
          <ScrollArea className="flex-grow p-4" ref={scrollAreaRef}>
            {messages.map((message, index) => (
              <div
                key={index}
                className={`mb-4 ${
                  message.role === 'user' ? 'text-right' : 'text-left'
                }`}
              >
                <span
                  className={`inline-block p-2 rounded-lg ${
                    message.role === 'user'
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-700 text-gray-200'
                  }`}
                >
                  {message.content}
                </span>
              </div>
            ))}
            {isTyping && (
              <div className="flex items-center space-x-2 mb-4">
                <div className="w-8 h-8 rounded-full bg-gray-700 flex items-center justify-center">
                  <span className="animate-pulse">...</span>
                </div>
                <div className="text-sm text-gray-400">typing</div>
              </div>
            )}
          </ScrollArea>
          <form onSubmit={handleSubmit} className="p-4 border-t border-gray-700">
            <div className="flex">
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder="Type your message..."
                className="flex-grow mr-2 p-2 bg-gray-700 text-white border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <Button type="submit" disabled={isLoading} className="bg-blue-600 hover:bg-blue-700 text-white">
                Send
              </Button>
            </div>
          </form>
        </div>
      ) : (
        <Button
          onClick={() => setIsOpen(true)}
          className="rounded-full w-12 h-12 flex items-center justify-center bg-blue-600 hover:bg-blue-700 text-white"
        >
          <MessageCircle className="h-6 w-6" />
        </Button>
      )}
    </div>
  )
}