package com.mysite.sbb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// 로그인을 두 갈래로 나눈다.
//  - 관리자 : /admin/**  → 관리자 로그인 화면(/admin/login), ADMIN 권한만 접근
//  - 이용자 : 나머지     → 이용자 로그인 화면(/member/login)
// SecurityFilterChain 을 2개 만들고, @Order 숫자가 작은 쪽(관리자)을 먼저 검사한다.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ===== 1. 관리자용 =====
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {

        http.securityMatcher("/admin/**");   // 이 체인은 /admin 으로 시작하는 요청만 담당

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/admin/login").permitAll()
                .anyRequest().hasRole("ADMIN"));          // ROLE_ADMIN 만 통과

        http.formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin", true)
                .failureUrl("/admin/login?error")
                .permitAll());

        http.logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .invalidateHttpSession(true));

        // 이용자 계정으로 관리자 화면에 들어오면 관리자 로그인 화면으로 돌려보낸다
        http.exceptionHandling(ex -> ex.accessDeniedPage("/admin/login?denied"));

        return http.build();
    }

    // ===== 2. 이용자용 =====
    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1) URL 별 접근 권한 (gracestars.kr 처럼 로그인해야 모든 화면을 볼 수 있다)
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**", "/js/**", "/images/**", "/data/**", "/favicon.ico").permitAll()
                .requestMatchers("/member/**").permitAll()   // 회원가입, 로그인 화면
                // 공지 작성·수정·삭제는 관리자만
                .requestMatchers("/notice/create", "/notice/*/edit", "/notice/*/delete").hasRole("ADMIN")
                // 회의록은 조장·리더(관리자 포함)만
                .requestMatchers("/minutes/**").hasAnyRole("LEADER", "ADMIN")
                .anyRequest().authenticated());              // 나머지는 로그인 필요

        // 권한이 없으면 안내 화면으로
        http.exceptionHandling(ex -> ex.accessDeniedPage("/denied"));

        // 2) 로그인 : 기본 로그인 화면 대신 직접 만든 화면(/member/login) 사용
        //    POST /member/login 요청은 Spring Security 가 처리하고,
        //    사용자 조회는 MemberSecurityService(UserDetailsService) 가 담당
        http.formLogin(form -> form
                .loginPage("/member/login")
                .defaultSuccessUrl("/")
                .failureUrl("/member/login?error")
                .permitAll());

        // 3) 로그아웃 : POST /member/logout
        http.logout(logout -> logout
                .logoutUrl("/member/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
