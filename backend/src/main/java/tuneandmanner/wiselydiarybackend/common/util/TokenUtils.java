package tuneandmanner.wiselydiarybackend.common.util;

import org.springframework.stereotype.Component;

@Component
public class TokenUtils {

    private static final String BEARER = "Bearer ";

    public String extractBearerToken(String token) {
        if (token != null && token.startsWith(BEARER)) {
            return token.replace(BEARER, "");
        }
        return null;
    }

    public String addBearerPrefix(String token) {
        if (token != null && !token.startsWith(BEARER)) {
            return BEARER + token;
        }
        return token;
    }
}
