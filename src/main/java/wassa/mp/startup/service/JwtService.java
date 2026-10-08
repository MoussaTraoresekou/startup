package wassa.mp.startup.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import wassa.mp.startup.model.User;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Service
public class JwtService {

    /*
     * Clé secrète utilisée pour signer et vérifier les JWT.
     *
     * IMPORTANT :
     * Cette clé doit rester exactement la même entre
     * la génération et la vérification des tokens.
     */
    private static final String SECRET_KEY = "bba20372a3a78e002274ba2a24866a5e377f480f4e04c38157613c908f5bcbd2";

    /*
      Durée de validité du token :10 minutes
     */
    private static final long EXPIRATION_TIME = 10 * 60 * 1000L;

    /**
     * Génère la clé utilisée pour signer/vérifier le JWT.
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }
    /**
     * Génération du JWT.
     */
    public String generetedToken(User user) {
        HashMap<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims(claims)
                /*
                 * IMPORTANT :
                 * Mets ici la même valeur que celle retournée
                 * par UserDetails.getUsername().
                 *
                 * Si ton UserDetails utilise le nom :
                 */
                .subject(user.getEmail())
                .issuer("DCB")
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis()
                                + EXPIRATION_TIME)
                )
                .signWith(getSecretKey())
                .compact();
    }
    /**
     * Récupère le username depuis le JWT.
     */
    public String extraUserName(String token) {

        return extractClaims(
                token,
                Claims::getSubject
        );
    }

    /**
     * Extrait une information quelconque du JWT.
     */
    private <T> T extractClaims(
            String token,
            Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    /**
     * Vérifie la signature du JWT et récupère ses claims.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    /**
     * Vérifie si le token est valide pour l'utilisateur.
     */
    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {
        final String username = extraUserName(token);
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }
    /**
     * Vérifie si le token est expiré.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token)
                .before(new Date());
    }
    /**
     * Récupère la date d'expiration.
     */
    private Date extractExpiration(String token) {
        return extractClaims(
                token,
                Claims::getExpiration
        );
    }
}