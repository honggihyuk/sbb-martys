package com.mysite.sbb.member.service;

import com.mysite.sbb.member.constant.MemberRole;
import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// 로그인 처리 : Spring Security 가 로그인 폼의 username 으로 이 메서드를 호출하고,
// 돌려받은 UserDetails 의 (암호화된) 비밀번호와 입력한 비밀번호를 PasswordEncoder 로 비교한다.
@Service
@RequiredArgsConstructor
public class MemberSecurityService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<GrantedAuthority> authorities = new ArrayList<>();
        // DB 에 저장된 권한을 그대로 사용 (예전에 가입해서 role 이 비어 있으면 이용자로 본다)
        MemberRole role = (member.getRole() == null) ? MemberRole.USER : member.getRole();
        authorities.add(new SimpleGrantedAuthority(role.getValue()));

        return new User(member.getUsername(), member.getPassword(), authorities);
    }
}
