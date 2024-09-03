package com.zj.auth.config;

import com.zj.auth.entity.Constants;
import com.zj.auth.handler.JwtAuthenticationTokenFilter;
import com.zj.auth.handler.LogoutSuccessHandlerImpl;
import com.zj.auth.handler.UserAccessDecisionManager;
import com.zj.auth.handler.UserAccessDeniedHandler;
import com.zj.auth.handler.UserNotLoginHandler;
import com.zj.auth.handler.WindyUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class AuthSecurityConfig extends WebSecurityConfigurerAdapter {
    public final WindyUserDetailsService windyUserDetailsService;
    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    private final LogoutSuccessHandlerImpl logoutSuccessHandler;
    private final UserAccessDecisionManager accessDecisionManager;

    public AuthSecurityConfig(LogoutSuccessHandlerImpl logoutSuccessHandler,
                              UserAccessDecisionManager accessDecisionManager,
                              WindyUserDetailsService windyUserDetailsService,
                              JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter) {
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.accessDecisionManager = accessDecisionManager;
        this.windyUserDetailsService = windyUserDetailsService;
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable()
                .exceptionHandling()
                .accessDeniedHandler(new UserAccessDeniedHandler())
                .authenticationEntryPoint(new UserNotLoginHandler()).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers(Constants.USER_LOGIN_URL).permitAll()
                .antMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/static/**").permitAll()
                .anyRequest().authenticated()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O o) {
                        o.setAccessDecisionManager(accessDecisionManager);
                        return o;
                    }
                })
                .and()
                .anonymous().disable()
                .headers().frameOptions().disable();
        httpSecurity.logout().logoutUrl("/v1/devops/user/logout").logoutSuccessHandler(logoutSuccessHandler);
        httpSecurity.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(windyUserDetailsService).passwordEncoder(NoOpPasswordEncoder.getInstance());
    }
}
