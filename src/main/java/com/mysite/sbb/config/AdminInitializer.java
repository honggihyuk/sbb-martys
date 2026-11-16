package com.mysite.sbb.config;

import com.mysite.sbb.member.constant.Gender;
import com.mysite.sbb.member.constant.MemberRole;
import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// 서버가 시작될 때 관리자 계정이 없으면 하나 만들어 준다.
// 아이디/비밀번호는 application.yml 의 app.admin.* 값을 사용한다.
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin1234}")
    private String adminPassword;

    @Value("${app.admin.email:admin@sbb.local}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        if (memberRepository.existsByUsername(adminUsername)) {
            return;
        }

        Member admin = Member.builder()
                .username(adminUsername)
                .name("관리자")
                .password(passwordEncoder.encode(adminPassword))
                .email(adminEmail)
                .gender(Gender.MALE)
                .registration(true)
                .role(MemberRole.ADMIN)
                .created(LocalDateTime.now())
                .build();

        memberRepository.save(admin);
        log.info("=======> 관리자 계정 생성 : {}", adminUsername);
    }
}
