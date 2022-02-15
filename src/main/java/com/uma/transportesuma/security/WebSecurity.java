package com.uma.transportesuma.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter{

    private UserDetailsService userDetailsService;
    public WebSecurity(UserDetailsService userDetailsService){
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

     @Bean
    public JWTFilter getJwtFilter(){
        return new JWTFilter();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception{
        http.cors().and().csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                    .antMatchers(PUT, "/api/journeys/**").authenticated()
                    .antMatchers(POST, "/api/journeys/**").authenticated()
                    .antMatchers(DELETE, "/api/journeys/**").authenticated()

                    .antMatchers(PUT,"/api/users/**").authenticated()
                    .antMatchers(DELETE,"/api/users/**").authenticated()

                    .antMatchers(PUT, "/api/vehicles/**").authenticated()
                    .antMatchers(POST, "/api/vehicles/**").authenticated()
                    .antMatchers(DELETE, "/api/vehicles/**").authenticated()

                    .antMatchers("/api/chats/**").authenticated()

                    .antMatchers("/api/stripe/url", "/api/stripe/enable", "/api/stripe/payment-intent").authenticated()

                    //.antMatchers("/api/opendata/**").authenticated()
                    //.antMatchers(GET, "/api/users/**").authenticated()
                .anyRequest().permitAll();
                    //.antMatchers("/authentication/user/**").permitAll()
                    //.antMatchers(POST, "/api/users/**").permitAll()
                    //.antMatchers("/api/stripe/webhook").permitAll()
                //.antMatchers(GET,"/api/opendata/get/places/**", "/api/journeys/**", "/api/users/**").permitAll()
                //.anyRequest().authenticated();
        http.addFilterBefore(getJwtFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception{
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }
}
