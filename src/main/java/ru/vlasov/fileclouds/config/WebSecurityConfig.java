package ru.vlasov.fileclouds.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import ru.vlasov.fileclouds.security.AuthProvider;

@EnableWebSecurity
public class WebSecurityConfig {

    private final AuthProvider authProvider;

    @Autowired
    public WebSecurityConfig(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests((requests) -> requests
//                        .requestMatchers("/", "/home").permitAll()
//                        .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .formLogin((form) -> form
//                        .loginPage("/login-custom")
//                        .defaultSuccessUrl("home.html", true)
//                        .permitAll()
//                )
//                .logout((logout) -> logout.permitAll());

        http
                .authorizeHttpRequests(
                        (requests) -> requests
//                        .requestMatchers("css/**", "/js/**", "/img/**").permitAll()
//                        .requestMatchers("/", "/home").permitAll()
                        .anyRequest().authenticated()
//                        .requestMatchers("/sign-up").anonymous()
                        )
                .authenticationProvider(authProvider)
                .formLogin((form) -> form
                        .loginPage("login")
                        .permitAll())
                .logout((logout) -> logout.permitAll());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user =
                User.withDefaultPasswordEncoder()
                        .username("user")
                        .password("password")
                        .roles("USER")
                        .build();

        return new InMemoryUserDetailsManager(user);
    }
}
