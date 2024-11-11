package com.crashcourse.kickoff.tms.security;

import org.springframework.stereotype.Service;

@Service
public class JwtTokenProvider {

    public String getToken(String token) {
        return token.substring(7);
    }

}

