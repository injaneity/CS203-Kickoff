import loginpageBackground from '@/assets/loginpage_background.png';
import eyePassword from '@/assets/eyePassword.svg';
import eyePasswordOff from '@/assets/eyePasswordOff.svg';

import {
    Input,
    Switch,
    Field,
    Label
} from "@headlessui/react";
import React from "react";
import axios from 'axios';

const Login = () => {
    const [email, setEmail] = React.useState('');
    const [password, setPassword] = React.useState('');
    const [showPassword, setShowPassword] = React.useState(false);
    const [rememberMe, setRememberMe] = React.useState(false);

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    const handleLogin = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
    
        // Encode credentials for Basic Auth (username:password in Base64)
        const credentials = btoa(`${email}:${password}`);
    
        try {
          // Make the POST request with Basic Authentication
          const response = await axios.get(
            'http://localhost:8080/users', // Replace with your API URL
            {
              headers: {
                'Authorization': `Basic ${credentials}`, // Basic Auth header
                'Content-Type': 'application/json',
              },
            }
          );
    
          // Handle the response after successful authentication
          if (response.status === 200) {
            console.log('Login successful:', response.data);
    
            // Store credentials in localStorage or sessionStorage (use sessionStorage for temporary storage)
            // localStorage.setItem('authToken', credentials); // Store Base64 encoded credentials
    
            // // Set axios defaults to include Basic Auth header in all requests
            // axios.defaults.headers.common['Authorization'] = `Basic ${credentials}`;
    
            console.log("Success")!
          }
        } catch (error: unknown) {
            // Handle unknown error type properly
            if (axios.isAxiosError(error)) {
              // Axios-specific error handling
              if (error.response) {
                console.error('Error response:', error.response.data);
                console.error('Error status:', error.response.status);
              } else if (error.request) {
                console.error('Error request:', error.request);
              } else {
                console.error('Error message:', error.message);
              }
            } else if (error instanceof Error) {
              // Generic error handling
              console.error('Generic error:', error.message);
            } else {
              console.error('Unknown error:', error);
            }
          }
      };

    return (
        <div className="flex h-screen">
            {/* Left section for the football player image */}
            <div className="w-1/2 flex items-center justify-center bg-black relative">
                <div className="text-white text-center">
                    <img
                        className="w-3/4 mx-auto rounded-lg"
                        src={loginpageBackground} // Replace with the actual path
                        alt="Football Player"
                    />
                    <h1 className="absolute text-6xl top-1/3 left-1/4 text-white font-bold">
                        21
                    </h1>
                </div>
            </div>

            {/* Right section for login form */}
            <div className="w-1/2 bg-white flex items-center justify-center">
                <div className="max-w-sm w-full space-y-8 p-8">
                    <div className="text-center">
                        <img
                            className="mx-auto h-20 w-20 rounded-full"
                            src="/assets/avatar.png" // Replace with the actual path
                            alt="Vince TYY"
                        />
                        <h2 className="mt-6 text-3xl font-extrabold text-gray-900">
                            Nice to see you again
                        </h2>
                    </div>

                    {/* Login Form */}
                    <form className="mt-8 space-y-6" onSubmit={handleLogin}>
                        <div className="rounded-md shadow-sm bg space-y-4">
                            <Field className="max-w-md">
                                <Label htmlFor="email" className="block text-sm font-medium text-gray-700 text-left mb-1">
                                    Username
                                </Label>
                                <Input
                                    id="email"
                                    name="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                    className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                                    placeholder="Email or phone number"
                                />
                                <label htmlFor="email" className="sr-only">
                                    Email address or phone number
                                </label>
                            </Field>
                            <div>
                                <label htmlFor="password" className="block text-sm font-medium text-gray-700 text-left mb-1">
                                    Password
                                </label>
                                <div className="relative">
                                    <input
                                        id="password"
                                        name="password"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        type={showPassword ? "text" : "password"}
                                        required
                                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                                        placeholder="Enter password"
                                    />
                                    <div
                                        className="absolute inset-y-0 right-0 flex items-center px-4 text-gray-600 cursor-pointer"
                                        onClick={togglePasswordVisibility}
                                    >
                                        <img
                                            src={showPassword ? eyePasswordOff : eyePassword}
                                            alt="Toggle password visibility"
                                            className="h-5 w-5"
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Remember Me with Headless UI Switch */}
                        <Field as="div" className="flex justify-between">
                            <div className="flex">
                                <Switch
                                    checked={rememberMe}
                                    onChange={setRememberMe}
                                    className={`${rememberMe ? "bg-green-400" : "bg-gray-200"
                                        } group relative flex h-7 w-14 cursor-pointer rounded-full p-1 transition-colors duration-200 ease-in-out focus:outline-none data-[focus]:outline-1 data-[focus]:outline-white`}
                                >
                                    <span className="sr-only">Remember me</span>
                                    <span
                                        className={`${rememberMe ? "translate-x-6" : "translate-x-1"
                                            } pointer-events-none inline-block size-5 translate-x-0 rounded-full bg-white ring-0 shadow-lg transition duration-200 ease-in-out group-data-[checked]:translate-x-7`}
                                    />
                                </Switch>
                                <Label className="ml-2 block text-sm text-gray-900">
                                    Remember me
                                </Label>
                            </div>
                            <div className="flex justify-between items-center">
                                <a
                                    href="#"
                                    className="text-sm text-indigo-600 hover:text-indigo-500"
                                >
                                    Forgot password?
                                </a>
                            </div>
                        </Field>

                        {/* Sign In Button */}
                        <div>
                            <button
                                type="submit"
                                className="w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                            >
                                Sign in
                            </button>
                        </div>

                        <div className="text-center text-sm mt-2">
                            Or sign in with
                            <button className="text-indigo-600 ml-1">
                                <img
                                    src="/assets/google-icon.png" // Replace with Google icon
                                    alt="Google"
                                    className="inline-block w-5 h-5 mr-2"
                                />
                                Google
                            </button>
                        </div>

                        <div className="text-center text-sm">
                            Don’t have an account?{" "}
                            <a href="#" className="text-indigo-600">
                                Sign up now
                            </a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default Login;