package kr.ac.hansung.config;

import kr.ac.hansung.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/login", "/signup",
                                 "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 일반 유저(USER)가 진입할 때 403 예외를 터뜨릴 핵심 보안 경로 제어
                .requestMatchers("/products/add", "/products/*/delete", "/products/*/edit").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/products").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // 💡 리다이렉트 핸들러를 제거하여 예외 발생 시 브라우저에 순수 403 응답이 바로 꽂히도록 처리
            .userDetailsService(userDetailsService);

        return http.build();
    }
}