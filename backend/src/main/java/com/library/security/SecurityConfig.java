package com.library.security;

import com.library.common.Result;
import com.library.common.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Spring Security Configuration
 * - Stateless session (JWT-based)
 * - RBAC role-based access control
 * - JWT filter before UsernamePasswordAuthenticationFilter
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // Allow preflight CORS
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public endpoints
                .antMatchers("/auth/login", "/auth/register").permitAll()
                .antMatchers("/error").permitAll()
                // GET requests: all authenticated users can view books, categories, borrow records
                .antMatchers(HttpMethod.GET, "/books/**", "/categories/**", "/borrow/**", "/statistics/**").authenticated()
                // POST/PUT/DELETE on books & categories: ADMIN and LIBRARIAN only
                .antMatchers(HttpMethod.POST, "/books/**", "/categories/**").hasAnyRole("ADMIN", "LIBRARIAN")
                .antMatchers(HttpMethod.PUT, "/books/**", "/categories/**").hasAnyRole("ADMIN", "LIBRARIAN")
                .antMatchers(HttpMethod.DELETE, "/books/**", "/categories/**").hasAnyRole("ADMIN", "LIBRARIAN")
                // User management: ADMIN only
                .antMatchers("/users/**").hasRole("ADMIN")
                // Borrow return/check-overdue: ADMIN and LIBRARIAN
                .antMatchers(HttpMethod.PUT, "/borrow/return/**", "/borrow/check-overdue/**").hasAnyRole("ADMIN", "LIBRARIAN")
                // All other endpoints: authenticated
                .anyRequest().authenticated()
            .and()
            .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) ->
                        writeJsonResponse(response, ResultCode.UNAUTHORIZED))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                        writeJsonResponse(response, ResultCode.FORBIDDEN));

        // Add JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    private void writeJsonResponse(javax.servlet.http.HttpServletResponse response, ResultCode code) throws IOException {
        response.setStatus(code.getCode());
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(code);
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
