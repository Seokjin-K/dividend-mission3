package com.dividend.service;

import com.dividend.model.Auth;
import com.dividend.model.MemberEntity;
import com.dividend.persist.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder; // AppConfig 에서 @Bean 주입받음
    private final MemberRepository memberRepository;

    @Override // 스프링 시큐리티에서 지원하는 기능을 사용하기 위해서는 메서드를 구현해야 한다.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // MemberEntity 는 UserDetails 를 상속했기 때문에 바로 반환이 가능하다.
        return this.memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user " + username));
    }

    // 회원가입 메서드
    public MemberEntity register(Auth.SignUp member) {
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new RuntimeException("이미 사용중인 아이디 입니다.");
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        return this.memberRepository.save(member.toEntity());
    }

    // 로그인할 때 검증을 위한 메서드
    public MemberEntity authenticate(Auth.SignIn member) {

        MemberEntity user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 ID 입니다."));

        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }
}
