import React, { useState } from 'react';
import { Input } from '../components/ui/input';
import { Button } from '../components/ui/button';
import { PlayerPosition } from '../types/profile';
import { Slider } from '../components/RoleSlider';
import eyePassword from '@/assets/eyePassword.svg';
import eyePasswordOff from '@/assets/eyePasswordOff.svg';
import { toast } from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import { signup } from '../services/userService';

export default function SignupPage() {
    const navigate = useNavigate();

    // States for sign-up form
    const [role, setRole] = useState('player');
    const [username, setUsername] = useState('');
    const [usernameError, setUsernameError] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [showPasswordCriteria, setShowPasswordCriteria] = useState(false);
    const [passwordCriteria, setPasswordCriteria] = useState({
        length: false,
        capital: false,
        lowercase: false,
        symbol: false,
        number: false,
    });
    const [confirmPasswordError, setConfirmPasswordError] = useState('');
    const [preferredPositions, setPreferredPositions] = useState<PlayerPosition[]>([]);

    // Toggle password visibility
    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    const handleUsernameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setUsername(value);

        if (value.length < 5 || value.length > 20) {
            setUsernameError('Username must be 5-20 characters.');
        } else {
            setUsernameError('');
        }
    };

    const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setPassword(value);

        setPasswordCriteria({
            length: value.length >= 8,
            capital: /[A-Z]/.test(value),
            lowercase: /[a-z]/.test(value),
            symbol: /[!@#$%^&*]/.test(value),
            number: /[0-9]/.test(value),
        });
    };

    const handleConfirmPasswordBlur = () => {
        if (password !== confirmPassword) {
            setConfirmPasswordError('Passwords do not match.');
        } else {
            setConfirmPasswordError('');
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

    // Handle form submission
    const handleSignup = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        // Check if username meets the length requirements
        if (username.length < 5 || username.length > 20) {
            toast.error('Username must be between 5 and 20 characters.', {
                duration: 3000,
                position: 'top-center',
            });
            return;
        }

        // Check if password meets all criteria
        if (!passwordCriteria.length || !passwordCriteria.capital || !passwordCriteria.lowercase || !passwordCriteria.symbol || !passwordCriteria.number) {
            toast.error('Password does not meet all criteria.', {
                duration: 3000,
                position: 'top-center',
            });
            return;
        }

        // Check if passwords match
        if (password !== confirmPassword) {
            toast.error('Passwords do not match.', {
                duration: 3000,
                position: 'top-center',
            });
            return;
        }

        const payload = {
            username,
            email,
            password,
            preferredPositions,
            role,
        };
        console.log(payload);
        try {
            const response = await signup(payload);

            if (response.status === 201) {
                toast.success('Sign up successful!', {
                    duration: 3000,
                    position: 'top-center',
                });
                navigate('/profile');
            }
        } catch (error: unknown) {
            const errorMessage = (error as any).response?.data || 'An unknown error occurred';
            toast.error(errorMessage, {
                duration: 4000,
                position: 'top-center',
            });
        }
    };

    const formatPosition = (position: string) => {
        return position.replace('POSITION_', '').charAt(0) + position.replace('POSITION_', '').slice(1).toLowerCase();
    };

    return (
        <div className="flex justify-center bg-gray-900 relative z-0">
            <div className="bg-gray-800 p-8 rounded-lg shadow-lg w-full max-w-md relative z-10">
                <h2 className="text-3xl font-extrabold text-white text-center mb-6">Sign Up as...</h2>

                <Slider selected={role} onChange={setRole} />
                <form className="space-y-6" onSubmit={handleSignup}>
                    <div className="space-y-4">
                        {/* Username Input */}
                        <div>
                            <label
                                htmlFor="username"
                                className="block text-sm font-medium text-white mb-1"
                            >
                                Username
                            </label>
                            <Input
                                id="username"
                                name="username"
                                value={username}
                                onChange={handleUsernameChange}
                                required
                                className="w-full"
                                placeholder="Enter Username"
                            />
                        </div>
                        {usernameError && (
                            <p className="text-red-500 text-sm">{usernameError}</p>
                        )}

                        <div>
                            <label htmlFor="email" className="block text-sm font-medium text-white mb-1">Email</label>
                            <Input
                                id="email"
                                name="email"
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                className="w-full"
                                placeholder="Enter Email"
                            />
                        </div>

                        <div className="relative">
                            <label htmlFor="password" className="block text-sm font-medium text-white mb-1">Password</label>
                            <div className="relative">
                                <Input
                                    id="password"
                                    name="password"
                                    value={password}
                                    onChange={handlePasswordChange}
                                    onFocus={() => setShowPasswordCriteria(true)}
                                    onBlur={() => setShowPasswordCriteria(false)}
                                    type={showPassword ? 'text' : 'password'}
                                    required
                                    className="w-full"
                                    placeholder="Enter Password"
                                />
                                <div className="absolute inset-y-0 right-0 flex items-center px-4 text-gray-600 cursor-pointer" onClick={togglePasswordVisibility}>
                                    <img src={showPassword ? eyePassword : eyePasswordOff} alt="Toggle Password Visibility" className="h-5 w-5" />
                                </div>
                            </div>

                            {showPasswordCriteria && (
                                <div className="absolute top-0 left-full ml-4 bg-gray-700 text-white p-4 rounded-lg shadow-lg text-sm w-64 z-50">
                                    <div className="relative">
                                        <p className={passwordCriteria.length ? 'text-green-500' : 'text-red-500'}>
                                            At least 8 characters
                                        </p>
                                        <p className={passwordCriteria.capital ? 'text-green-500' : 'text-red-500'}>
                                            Includes a capital letter
                                        </p>
                                        <p className={passwordCriteria.lowercase ? 'text-green-500' : 'text-red-500'}>
                                            Includes a lowercase letter
                                        </p>
                                        <p className={passwordCriteria.symbol ? 'text-green-500' : 'text-red-500'}>
                                            Includes a symbol
                                        </p>
                                        <p className={passwordCriteria.number ? 'text-green-500' : 'text-red-500'}>
                                            Includes a number
                                        </p>
                                    </div>
                                </div>
                            )}
                        </div>

                        <div>
                            <label htmlFor="confirmPassword" className="block text-sm font-medium text-white mb-1">Confirm Password</label>
                            <div className="relative">
                                <Input
                                    id="confirmPassword"
                                    name="confirmPassword"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    onBlur={handleConfirmPasswordBlur}
                                    type={showPassword ? 'text' : 'password'}
                                    required
                                    className="w-full"
                                    placeholder="Confirm Password"
                                />
                                <div className="absolute inset-y-0 right-0 flex items-center px-4 text-gray-600 cursor-pointer" onClick={togglePasswordVisibility}>
                                    <img src={showPassword ? eyePassword : eyePasswordOff} alt="Toggle Password Visibility" className="h-5 w-5" />
                                </div>
                            </div>
                        </div>
                        {confirmPasswordError && (
                            <p className="text-red-500 text-sm">{confirmPasswordError}</p>
                        )}
                    </div>

                    {/* Conditional Rendering of Player Positions */}
                    {role === 'player' && (
                        <div className="mb-6">
                            <h2 className="text-xl font-semibold text-white mb-2">Preferred Positions</h2>
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

                    {/* Submit Button */}
                    <Button
                        type="submit"
                        className="w-full bg-blue-600 hover:bg-blue-700"
                    >
                        Sign Up
                    </Button>

                    <div className="text-center text-sm text-white">
                        Already have an account?{' '}
                        <a
                            onClick={() => navigate('/profile/')}
                            className="text-indigo-400 cursor-pointer"
                        >
                            Login now
                        </a>
                    </div>
                </form>
            </div >
        </div >
    );
}
