package com.in.jplearning.config;

import com.in.jplearning.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";


    //extract username from token
    public String extractUsername(String token){
        return extractClaims(token, Claims::getSubject);
    }

    //extract expiration time
    public Date extractExpiration(String token){
        return extractClaims(token, Claims::getExpiration);
    }


    //extract data from JSON
    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //return claims
    public Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //get Signing Key method
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //check expiration token
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    //generate token
    public String generateToken(String username, Role role){
        Map<String, Object> claims = new HashMap<>();
        claims.put("role",role.name());
        return createToken(claims,username);
    }

    public String generateToken(UserDetails userDetails){
        return createToken(new HashMap<>(),userDetails);
    }


    //create token
    private String createToken(Map<String, Object> claims, String username){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 *10))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }
    private String createToken(Map<String, Object> claims, UserDetails userDetails){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 *10))
                .signWith(getSigningKey(),SignatureAlgorithm.HS256).compact();
    }

    //validate token
    public boolean validateToken (String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}
