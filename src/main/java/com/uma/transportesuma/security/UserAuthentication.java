package com.uma.transportesuma.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uma.transportesuma.document.User;
import com.uma.transportesuma.dto.UserDTO;
import com.uma.transportesuma.service.UserService;
import com.uma.transportesuma.vo.JWTToken;
import com.uma.transportesuma.vo.UserAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@RestController
@RequestMapping("/authentication/user")
public class UserAuthentication {
    //https://github.com/auth0/java-jwt
    //https://blog.softtek.com/es/autenticando-apis-con-spring-y-jwt
    //https://www.bezkoder.com/spring-boot-jwt-authentication/

    @Value("${my.jwt.privatekey}")
    private String SECRET_KEY;

    private static final String GOOGLE_PUBLIC_CLIENT_ID = "634135800903-t4lnnd9m66u3lr82d4h4ncat75qbt1br.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET = "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";

    AuthenticationManager authenticationManager;
    private UserService userService;

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
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
    public ResponseEntity<JWTToken> authenticateGoogleOAuth(@RequestBody String postBody) {
        try{
            JsonObject item = JsonParser.parseString(postBody).getAsJsonObject();
            String id_token = item.get("id_token").getAsString();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(GOOGLE_PUBLIC_CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(id_token);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                // Print user identifier
                String userId = payload.getSubject();
                System.out.println("User ID: " + userId);

                // Get profile information from payload
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                String givenName = (String) payload.get("given_name");
                String randomPassword = getRandomString();

                Optional<User> thisUser = this.userService.findUserByEmail(email);
                User u = null;
                if(!thisUser.isPresent()) {
                    UserDTO userDTO = new UserDTO(email, email, name, randomPassword, pictureUrl, "");
                    u = this.userService.addUser(userDTO);
                } else {
                    u = thisUser.get();
                    u.setPassword(randomPassword);
                    u = this.userService.updateUser(u);
                }

                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(u.getUsername(), randomPassword)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                String tokenJwt = this.getTokenJWT(u.getUsername());

                return new ResponseEntity<>(new JWTToken(tokenJwt), HttpStatus.OK);
            } else {
                throw new Exception("Invalid ID token.");
            }

        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
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

    private String getRandomString(){
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 20;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return generatedString;
    }
}
