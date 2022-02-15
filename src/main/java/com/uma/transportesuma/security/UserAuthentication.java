package com.uma.transportesuma.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.uma.transportesuma.vo.JWTToken;
import com.uma.transportesuma.vo.UserAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/authentication/user")
public class UserAuthentication {
    //https://github.com/auth0/java-jwt
    //https://blog.softtek.com/es/autenticando-apis-con-spring-y-jwt
    //https://www.bezkoder.com/spring-boot-jwt-authentication/

    @Value("${my.jwt.privatekey}")
    private String SECRET_KEY;


    AuthenticationManager authenticationManager;

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    //Logea y CREA un token JWT
    @PostMapping("/login")
    public JWTToken authenticateUser(@RequestBody UserAuth user){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String tokenJwt = this.getTokenJWT(user.getUsername());

        return new JWTToken(tokenJwt);
    }

    @PostMapping("/google/oauth")
    public JWTToken authenticateGoogleOAuth(){
        return null;

    }


    private String getTokenJWT(String user){
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("ROLE_USER");
        String roles[] = {"ROLE_USER"};

        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes());
            String token = JWT.create()
                    .withIssuer("auth0")
                    .withSubject(user)
                    .withArrayClaim("authorities", roles)
                    .withExpiresAt(new Date(System.currentTimeMillis() + (1000*60*60*24)))  //1 day
                    .sign(algorithm);
            return "Bearer " + token;
        } catch (JWTCreationException exception){

        }
        return null;
    }
}
