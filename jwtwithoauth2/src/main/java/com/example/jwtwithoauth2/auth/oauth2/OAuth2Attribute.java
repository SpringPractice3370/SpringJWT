package com.example.jwtwithoauth2.auth.oauth2;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class OAuth2Attribute {
    private Map<String, Object> attributes;
    private String attributeKey;
    private String email;
    private String name;
//    private String picture;

    static OAuth2Attribute of(String provider, String attributeKey, Map<String, Object> attributes) {
        switch (provider) {
            case "google":
                return ofGoogle(attributeKey, attributes);
            case "kakao":
                return ofKakao(attributeKey, attributes);
            case "naver":
                return ofNaver(attributeKey, attributes);
            default:
                throw new RuntimeException();
        }
    }

    private static OAuth2Attribute ofGoogle(String attributeKey, Map<String, Object> attributes) {
        return OAuth2Attribute.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .attributeKey(attributeKey)
                .build();
    }

    private static OAuth2Attribute ofKakao(String attributeKey, Map<String, Object> attributes) {

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) attributes.get("profile");

        return OAuth2Attribute.builder()
                .name((String) kakaoProfile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .attributes(kakaoAccount)
                .attributeKey(attributeKey)
                .build();
    }


    private static OAuth2Attribute ofNaver(String attributeKey, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuth2Attribute.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .attributes(response)
                .attributeKey(attributeKey)
                .build();
    }

    Map<String, Object> convertToMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", attributeKey);
        map.put("key", attributeKey);
        map.put("name", name);
        map.put("email", email);

        return map;
    }
}