package com.in.jplearning.config;


import com.in.jplearning.model.User;
import com.in.jplearning.repositories.UserDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final UserDAO userDAO;

    private User userDetail;

    public User getUserDetail(){
        return userDetail;
    }
    @Bean
    public UserDetailsService userDetailsService(){
        return username -> userDAO.findByEmail(username)
                .orElseThrow(()-> new UsernameNotFoundException("User Not Found"));

    }
    @Bean
    public AuthenticationProvider authenticationProvider(){
        //data access authentication provider
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        //bao cai DAO nay xem su dung cai user service nao
        authProvider.setUserDetailsService(userDetailsService());
        //thong bao cho DAO la dang su dung password encode nao
        authProvider.setPasswordEncoder(passwordEndCoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEndCoder() {
        return new BCryptPasswordEncoder();
    }
}
