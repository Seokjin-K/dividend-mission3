package com.dividend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 토큰은 HTTP 프로토콜의 헤더에 포함된다.
    // 어떤 키를 기준으로 토큰을 주고받을 지 키값을 정의
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer "; // 인증 타입
    // 요청이 올 때 HTTP 헤더의 TOKEN_HEADER(키)를 기준으로 value 는 'Bearer '로 하면,
    // 우리가 발급해준 토큰이 'Bearer xxxx.yyyy.zzz' 형태로 요청이 오게 된다.

    private final TokenProvider tokenProvider;

    // OncePerRequestFilter 를 상속하면 doFilterInternal 를 필수로 구현하게 돼있다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = this.resolveTokenFromRequest(request);

        if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
            Authentication auth = this.tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private String resolveTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);

        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            // 정상적인 토큰인지는 아직 모르지만, 토큰의 형태를 띄고 있다.
            return token.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
