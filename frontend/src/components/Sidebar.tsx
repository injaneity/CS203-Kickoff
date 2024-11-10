import React, { useEffect, useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { User, Trophy, Users, Menu, X, BarChart2 } from 'lucide-react';
import { Button } from "./ui/button";
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import { selectIsAdmin, selectUserId } from '../store/userSlice';
import { fetchPlayerProfileById } from '../services/userService';

export interface SidebarProps {
  isOpen: boolean;
  setIsOpen: (isOpen: boolean) => void;
}

export default function Sidebar({ isOpen, setIsOpen }: SidebarProps) {
  const toggleSidebar = () => setIsOpen(!isOpen);
  const location = useLocation();
  const isAdmin = useSelector((state: RootState) => selectIsAdmin(state));
  const userId = useSelector(selectUserId);
  const [isHost, setIsHost] = useState(false);

  useEffect(() => {
    const checkIfHost = async () => {
      try {
        if (userId) {
          const playerProfile = await fetchPlayerProfileById(userId);
          setIsHost(!playerProfile); // If the profile is null or undefined, user is a host.
        }
      } catch (error) {
        console.error('Error fetching player profile:', error);
        setIsHost(true); // Consider user a host if there's an error fetching the profile.
      }
    };

    checkIfHost();
  }, [userId]); 

  const NavItem = ({ to, icon: Icon, children }: { to: string; icon: React.ElementType; children: React.ReactNode }) => (
    <NavLink
      to={to}
      className={({ isActive }) =>
        `flex items-center space-x-2 p-2 rounded-md transition-colors duration-200 ${
          location.pathname.startsWith(to) || isActive
            ? 'bg-gray-800 text-white'
            : 'text-gray-300 hover:bg-gray-800 hover:text-white'
        }`
      }
      onClick={() => setIsOpen(false)}
    >
      <Icon className="h-5 w-5" />
      <span>{children}</span>
    </NavLink>
  );

  return (
    <>
      <Button
        variant="ghost"
        size="icon"
        className="md:hidden fixed top-4 left-4 z-20 bg-gray-900 text-white"
        onClick={toggleSidebar}
      >
        {isOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
        <span className="sr-only">{isOpen ? 'Close menu' : 'Open menu'}</span>
      </Button>

      {isOpen && (
        <div 
          className="md:hidden fixed inset-0 bg-black bg-opacity-50 z-10" 
          onClick={toggleSidebar}
          aria-hidden="true"
        />
      )}

      <aside
        className={`
          fixed top-0 left-0 z-20 h-full w-64 bg-gray-900 p-6 space-y-6 transition-transform duration-300 ease-in-out transform
          ${isOpen ? 'translate-x-0' : '-translate-x-full'}
          md:relative md:translate-x-0
        `}
      >
        <nav className="space-y-4">
          {isAdmin ? (
            <>
              <h2 className="text-gray-300 mb-2 font-bold text-lg">Admin Sidebar</h2>
              <NavItem to="/admin/players" icon={User}>Players</NavItem>
              <NavItem to="/admin/clubs" icon={Users}>Clubs</NavItem>
              <NavItem to="/admin/tournaments" icon={Trophy}>Tournaments</NavItem>
            </>
          ) : isHost ? (
            <>
              <h2 className="text-gray-300 mb-2 font-bold text-lg">Host Sidebar</h2>
              <NavItem to="/profile" icon={User}>Profile</NavItem>
              <NavItem to="/tournaments" icon={Trophy}>Tournaments</NavItem>
              <NavItem to="/leaderboard" icon={BarChart2}>Leaderboard</NavItem>
            </>
          ) : (
            <>
              <NavItem to="/profile" icon={User}>Profile</NavItem>
              <NavItem to="/clubs" icon={Users}>Clubs</NavItem>
              <NavItem to="/tournaments" icon={Trophy}>Tournaments</NavItem>
              <NavItem to="/leaderboard" icon={BarChart2}>Leaderboard</NavItem>
            </>
          )}
        </nav>
      </aside>
    </>
  );
}
