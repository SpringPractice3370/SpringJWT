package com.example.jwt.config.jwt;

import com.example.jwt.config.auth.PrincipalDetails;
import com.example.jwt.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

// 스프링 시큐리티에서 UsernamePasswordAuthenticationFilter 가 있음
// /login 요청해서 username, password 전송하면 (POST)
// UsernamePasswordAuthenticationFilter 가 동작을 함
// 근데 .formLogin().disable()를 해서 동작을 안함
// SecurityConfig 에서  .addFilter(new JwtAuthenticationFilter(authenticationManager())) 하면 됨

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    // /login 요청을 하면 로그인 시도를 위해서 실행되는 함수

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("JwtAuthenticationFilter : 로그인 시도 중");

        // 정상이면
        // 1. username, password 받아서
        try {
//            BufferedReader br = request.getReader();
//            String input = null;
//            while ((input = br.readLine()) != null) {
//                System.out.println("input = " + input);
//            }
            ObjectMapper om = new ObjectMapper();
            User user = om.readValue(request.getInputStream(), User.class);
            System.out.println("user = " + user);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

            // PrincipalDetailService 의 loadUserByUsername() 함수가 실행됨, 정상이면 authentication 이 리턴됨 ->  // DB에 있는 username과 password 가 일치함
            // authenticationManager 에 토큰을 보내서 던지면 인증해줌
            // authentication 에 로그인한 정보가 담김
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // => 로그인이 되었다는 뜻
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            System.out.println("로그인 완료됨 :" + principalDetails.getUser().getUsername()); // 잘 나오면 로그인이 정상적으로 된 것.

            // authentication 객체가 session 영역에 저장해야 하고 그 방법이 return 해주면 됨.
            // 리턴의 이유는 권한 관리를 security 가 대신 해주기 떄문에 편하려고 하는거임.
            // 굳이 JWT 토큰을 사용하면서 세션을 만들 이유가 없음. 근데 단지 권한 처리 때문에 session 을 넣어줌.
            return authentication;

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("2===============================");

        // 2. 정상인지 로그인 시도를 해봄.
        // authenticationManager 로 로그인 시도를 하면! PrincipalDetailsService 가 호출이 됨 -> loadUserByUsername() 함수 실행

        // 3. PrincipalDetails 를 세션에 담고 -> 담는 이유 : 권한 관리를 위해서
        // 4. JWT 토큰을 만들어서 응답해주면 됨
        return null;
    }

    // attemptAuthentication 실행 후 인증이 정상적으로 되었으면 successfulAuthentication 함수가 실행됨.
    // JWT 토큰을 만들어서 request 요청한 사용자에게 JWT 토큰을 response 해주면 됨.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("successfulAuthentication 실행됨 : 인증 완료 되었다는 뜻");

        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

        // RSA 방식은 아니고 Hash 암호 방식
        String jwtToken = JWT.create()
                .withSubject(principalDetails.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME)) // 만료 시간, 짧게 주는 것이 좋음
                .withClaim("id", principalDetails.getUser().getId())
                .withClaim("username", principalDetails.getUser().getUsername())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET)); // 서버가 갖는 고유한 값, 서명

        response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);
    }
}
