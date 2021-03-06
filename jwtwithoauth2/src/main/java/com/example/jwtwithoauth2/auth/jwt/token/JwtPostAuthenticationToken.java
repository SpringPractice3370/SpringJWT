package com.example.jwtwithoauth2.auth.jwt.token;

import com.example.jwtwithoauth2.account.Account;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * <h1>JwtPostAuthenticationToken</h1>
 * <p>
 * JWT 인증 후 객체
 * </p>
 * <p>
 * 인증 후 객체는 {@link com.example.jwtwithoauth2.account.Account} 객체를 가지고 있다. <br>
 * 주의! 단 영속성 컨텍스트에 저장되어 영속상태가 된 것은 아니다.
 * </p>
 */
public class JwtPostAuthenticationToken extends AbstractAuthenticationToken {

    private Account account;

    /**
     * 인증 시도 사용자의 계정 정보를 바탕으로 인증 후 객체를 생성한다.
     *
     * @param account jwt token 의 값을 기반으로 하는 계정 엔티티
     */
    public JwtPostAuthenticationToken(Account account) {
        super(List.of(new SimpleGrantedAuthority(account.getRole().name())));
        this.account = account;
        this.setAuthenticated(true);
    }

    // OAuth2 인증 이므로 별도의 패스워드 없음 사용 X
    @Override
    public Object getCredentials() {
        return "";
    }

    // 로그인 된 사용자 계정 정보 반환
    @Override
    public Object getPrincipal() {
        return account;
    }
}
