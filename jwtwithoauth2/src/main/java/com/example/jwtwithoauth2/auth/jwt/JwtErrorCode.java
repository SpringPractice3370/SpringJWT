package com.example.jwtwithoauth2.auth.jwt;

import com.example.jwtwithoauth2.auth.common.code.ResponseCode;

/**
 * <h1>JwtErrorCode</h1>
 * <p>
 *     JWT 인증 실패 시 응답 코드 Enum
 * </p>
 */
public enum JwtErrorCode implements ResponseCode {
    TOKEN_NOTFOUND(1300, "인증 토큰이 누락되었습니다."),
    TOKEN_MODULATED(1301, "변조된 토큰입니다."),
    ACCESS_TOKEN_EXPIRATION(1302, "인증 토큰이 만료되었습니다."),
    REFRESH_TOKEN_EXPIRATION(1303, "리프레쉬 토큰이 만료되었습니다.");

    private final int code;
    private final String message;

    JwtErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
