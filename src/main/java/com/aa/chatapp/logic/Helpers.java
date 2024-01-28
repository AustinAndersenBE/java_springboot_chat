package com.aa.chatapp.logic;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import com.aa.chatapp.state.Data.JwtConfig;
import com.aa.chatapp.state.model.User;

public class Helpers {

    public static String createJwtToken(JwtConfig config, User user) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date validity = new Date(nowMillis + config.validityInMilliseconds());

        Key key = Keys.hmacShaKeyFor(config.secretKey().getBytes());

        return Jwts.builder()
                .issuer("ChatApp") // Replace with your actual issuer
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(validity)
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .signWith(key)
                .compact();
    }
}