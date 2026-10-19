package wassa.mp.startup.service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import wassa.mp.startup.model.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    //private static final long EXPIRATION_TIME = 10 * 60 * 1000L;
    private static final long EXPIRATION_TIME = 1000L * 60 * 30;; // 30 munites

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generetedToken(User user) {
        Date maintenant = new Date();
        Date expiration = new Date(
                maintenant.getTime() + EXPIRATION_TIME
        );

        return Jwts.builder()
                .subject(user.getEmail())
                .issuer("DCB")
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }

    public String extraUserName(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        Claims claims = extractAllClaims(token);
        Date expiration = claims.getExpiration();

        return expiration != null
                && expiration.after(new Date())
                && claims.getSubject() != null
                && claims.getSubject().equals(
                userDetails.getUsername()
        );
    }
}
