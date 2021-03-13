package com.tuos.Collab.security.config;

import com.tuos.Collab.collabuser.CollabUser;
import com.tuos.Collab.collabuser.CollabUserService;
import com.tuos.Collab.security.PasswordEncoder;
import com.tuos.Collab.security.jwt.JwtTokenVerifier;
import com.tuos.Collab.security.jwt.JwtUsernameAndPasswordAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final CollabUserService collabUserService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http.cors().and().csrf().disable()
//
//                .authorizeRequests()
//                .antMatchers("/api/user/**","/collab-editor/**")
//                .authenticated().and().exceptionHandling().authenticationEntryPoint(new Http403ForbiddenEntryPoint()).and()//
//                .formLogin().permitAll()//
//                // If login fails, return 401
//                .failureHandler(new HTTPStatusHandler(HttpStatus.UNAUTHORIZED))//
//                // If login succeeds return 200
//                .successHandler(new HTTPStatusHandler(HttpStatus.OK)).and()//
//                .logout()//
//                // If logout succeeds return 200
//                .logoutSuccessHandler(new HTTPStatusHandler(HttpStatus.OK));//

//        http.cors().and().csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/api/user/**","/collab-editor/**")
//                .permitAll().anyRequest().authenticated().and()
//                .formLogin().permitAll();

        http.csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager()))
                .addFilterAfter(new JwtTokenVerifier(),JwtUsernameAndPasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/api/doc/**","/collab-editor/**")
                .authenticated();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        provider.setUserDetailsService(collabUserService);
        return provider;
    }

//    @Bean
//    public CorsFilter corsFilter() {
//        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        final CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        // Don't do this in production, use a proper list  of allowed origins
//        config.setAllowedOriginPatterns(Collections.singletonList("*"));
//        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept"));
//        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "DEL", "PATCH"));
//        source.registerCorsConfiguration("/**", config);
//        return new CorsFilter(source);
//    }





    /*


    inner class for handling authentication
     */
    class HTTPStatusHandler
            implements AuthenticationFailureHandler, AuthenticationSuccessHandler, LogoutSuccessHandler {

        private HttpStatus status;

        public HTTPStatusHandler(HttpStatus status) {
            this.status = status;
        }

        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                            AuthenticationException exception) throws IOException, ServletException {
            onAuthenticationSuccess(request, response, null);
        }

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) throws IOException, ServletException {
            response.setStatus(status.value());
        }

        @Override
        public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                    Authentication authentication) throws IOException, ServletException {
            onAuthenticationSuccess(request, response, null);
        }

    }

}



