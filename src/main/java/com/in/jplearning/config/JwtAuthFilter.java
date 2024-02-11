package com.in.jplearning.config;

import com.in.jplearning.enums.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private Claims claims;
    private final UserDetailsService userDetailsService;

    private String userName;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;


        //check token null or co bat dau bang bearer hay ko
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        jwt = authHeader.substring(7);
        // todo extract user email from JWT token;
        userName = jwtUtil.extractUsername(jwt);
        claims = jwtUtil.extractAllClaims(jwt);
        //kiem tra email va xem nguoi dung la ai
        if(userName!= null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userName);
            //check token co valid hay ko
            if(jwtUtil.validateToken(jwt, userDetails)){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request,response);
    }

    //check if user is admin
    public boolean isAdmin(){
        return String.valueOf(Role.ADMIN).equalsIgnoreCase(String.valueOf(claims.get("role")));
    }

    //check if user
    public boolean isUser(){
        return String.valueOf(Role.USER).equalsIgnoreCase(String.valueOf(claims.get("role")));
    }

    //check if manager
    public boolean isManager(){
        return String.valueOf(Role.MANAGER).equalsIgnoreCase(String.valueOf(claims.get("role")));
    }
    public String getCurrentUser(){
        return userName;
    }
}
