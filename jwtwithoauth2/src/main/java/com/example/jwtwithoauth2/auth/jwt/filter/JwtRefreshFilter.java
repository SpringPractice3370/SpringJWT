package com.example.jwtwithoauth2.auth.jwt.filter;

import com.example.jwtwithoauth2.auth.common.CommonCode;
import com.example.jwtwithoauth2.auth.common.dto.Response;
import com.example.jwtwithoauth2.auth.jwt.JwtProperties;
import com.example.jwtwithoauth2.auth.jwt.dto.TokenOfLogin;
import com.example.jwtwithoauth2.auth.jwt.exception.JwtExpiredTokenException;
import com.example.jwtwithoauth2.auth.jwt.exception.JwtModulatedTokenException;
import com.example.jwtwithoauth2.auth.jwt.service.RefreshTokenService;
import com.example.jwtwithoauth2.auth.jwt.token.RefreshToken;
import com.example.jwtwithoauth2.auth.jwt.token.TokenType;
import com.example.jwtwithoauth2.auth.jwt.util.JwtDecoder;
import com.example.jwtwithoauth2.auth.jwt.util.JwtTokenFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.MissingClaimException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRefreshFilter extends OncePerRequestFilter {

    @Autowired
    private final RefreshTokenService refreshTokenService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private final JwtTokenFactory jwtTokenFactory;

    private ObjectMapper objectMapper = new ObjectMapper();


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().equals("/api/refresh")) {
            // Request ?????? ??????
            ServletInputStream inputStream = request.getInputStream();
            Map<String, Object> map = objectMapper.readValue(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8), Map.class);
            String refreshToken = (String) map.get("refresh");
            log.info("refresh = {}", refreshToken);

            try {
//                getJwtDecoder().verify(refreshToken, TokenType.ACCESS_TOKEN);
                TokenOfLogin tokenOfLogin = checkRefreshToken(refreshToken);
                parsingResponse(tokenOfLogin, response);
//            } catch (SignatureException  | MalformedJwtException | MissingClaimException exception) {
            } catch (MalformedJwtException | MissingClaimException exception) {
                throw new JwtModulatedTokenException("????????? JWT ???????????????");
            } catch (ExpiredJwtException ex) {
                throw new JwtExpiredTokenException("????????? JWT ???????????????.");
            }
//            refreshTokenService.renewalRefreshToken(refreshToken);

        }

        filterChain.doFilter(request, response);
    }

    public JwtDecoder getJwtDecoder() {
        return new JwtDecoder(this.jwtProperties);
    }

    public TokenOfLogin checkRefreshToken(String refreshToken){

        //true??? ?????? ????????? refreshToken ?????? ??????
        if(getJwtDecoder().verifyRefreshToken(refreshToken)){

            // ????????? refresh Token ??????
            RefreshToken savedRefreshToken = refreshTokenService.getRefreshToken(refreshToken);

            // ????????? refresh Token ??????
            String newRefreshToken = jwtTokenFactory.createRefreshToken();

            // ????????? refresh Token Entity ??????
            RefreshToken newRefreshTokenEntity = RefreshToken.createRefreshToken
                    (
                            savedRefreshToken.getAccountEmail(),
                            savedRefreshToken.getAccountId(),
                            newRefreshToken,
                            savedRefreshToken.getRole()
                    );


            // ????????? refresh Token ??????
            refreshTokenService.removeRefreshToken(savedRefreshToken.getId());

            // ????????? refresh Token ??????
            refreshTokenService.renewalRefreshToken(newRefreshTokenEntity);

            // Access Token ??????
            String newAccessToken = jwtTokenFactory.createAccessToken(newRefreshTokenEntity);

            // token ?????? ?????? ??? ??????
            return TokenOfLogin.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
        }

        // false??? ?????? ?????? refresh Token ??????
        // ????????? refresh Token ??????
        RefreshToken savedRefreshToken =
                refreshTokenService.getRefreshToken(refreshToken);

        // Access Token ??????
        String newAccessToken = jwtTokenFactory.createAccessToken(savedRefreshToken);

        // token ?????? ?????? (access, refresh)
        // token ?????? ??????

        return TokenOfLogin.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void parsingResponse(TokenOfLogin tokenOfLogin, HttpServletResponse response) throws IOException {
        Response<TokenOfLogin> responseValue = Response.of(CommonCode.GOOD_REQUEST, tokenOfLogin);
        response.setStatus(HttpStatus.OK.value());
        response.setHeader(HttpHeaders.CONTENT_ENCODING, "UTF-8");
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (Objects.nonNull(responseValue)) {
            PrintWriter writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(responseValue));
        }
        log.info("parsing ???");
    }
}
