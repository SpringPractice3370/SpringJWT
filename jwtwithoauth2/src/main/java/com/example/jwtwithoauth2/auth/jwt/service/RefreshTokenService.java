package com.example.jwtwithoauth2.auth.jwt.service;

import com.example.jwtwithoauth2.account.Account;
import com.example.jwtwithoauth2.auth.jwt.repository.RefreshTokenRepository;
import com.example.jwtwithoauth2.auth.jwt.token.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(Account loggedInUser, String refreshToken) {
        // refresh token Entity
        RefreshToken refreshTokenEntity = new RefreshToken();

        refreshTokenRepository.save(createRefreshToken(loggedInUser, refreshToken));
    }

    public void renewalRefreshToken(RefreshToken refreshToken){
        refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken createRefreshToken(Account loggedInUser, String refreshToken){

        return RefreshToken.createRefreshToken
                (
                        loggedInUser.getEmail(),
                        loggedInUser.getId(),
                        refreshToken,
                        loggedInUser.getRole()
                );
    }
    public RefreshToken getRefreshToken(String refreshToken){
        return refreshTokenRepository.findByRefreshToken(refreshToken);
    }

    public void removeRefreshToken(Long id){
        refreshTokenRepository.deleteById(id);
    }
}
