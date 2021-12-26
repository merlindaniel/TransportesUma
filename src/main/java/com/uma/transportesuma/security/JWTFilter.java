package com.uma.transportesuma.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JWTFilter extends OncePerRequestFilter {

    @Value("${my.jwt.privatekey}")
    private String SECRET_KEY;


    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        try{
            String tokenJwt = this.getJWT(request);
            if(tokenJwt != null){
                DecodedJWT tokenDecoded = verifyToken(tokenJwt);
                String username = tokenDecoded.getSubject();

                //Comprobamos que existe para evitar errores
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                //Creamos la authenticationToken
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                //Guardamos el authenticationToken en el SecurityContextHolder (patron Singleton)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex){
            logger.error("Cannot set user authentication: {}", ex);
        }
        filterChain.doFilter(request, response);
    }

    private DecodedJWT verifyToken(String token) throws JWTVerificationException{
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes());
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("auth0")
                .build();

        DecodedJWT jwt = verifier.verify(token);
        return jwt;
    }

    private String getJWT(HttpServletRequest request){
        String headerAuthorization = request.getHeader("Authorization");
        if(headerAuthorization!= null && headerAuthorization.startsWith("Bearer"))
            return headerAuthorization.substring(7, headerAuthorization.length());
        return null;
    }
}
