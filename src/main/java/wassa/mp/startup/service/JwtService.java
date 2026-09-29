package wassa.mp.startup.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import wassa.mp.startup.model.User;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Service
public class JwtService {
    private String cleScret="";
    private Key genetedKey;

    public String generetedToken(User user) {
        HashMap<String, Object> claims = new HashMap<String, Object>();
        return Jwts
                .builder()
                .claims()
                .add(claims)
                .subject(user.getNom())
                .issuer("DCB")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+60*10*10000))
                .and()
                .signWith(genetedKey())
                .compact();
    }
    private SecretKey genetedKey() {
        byte[] decode= Decoders.BASE64.decode(getSecretKey());
        return Keys.hmacShaKeyFor(decode);
    }
    public String getSecretKey(){
        return cleScret=  "bba20372a3a78e002274ba2a24866a5e377f480f4e04c38157613c908f5bcbd2";
    }
    public String extraUserName(String token) {
        return   extracClaims(token, Claims::getSubject);
    }
    private <T> T extracClaims(String token, Function<Claims,T> claimsResolver) {
        Claims claims=extractClaims(token);
        return claimsResolver.apply(claims);
    }
    // cette partie à revoir
    private Claims extractClaims(String token) {
         return Jwts.parser().verifyWith(genetedKey()).build().parseSignedClaims(token).getPayload();
    }
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extraUserName(token);

        return (userName.equals(userDetails.getUsername())&& !isTokenExpired(token) );
    }
    private boolean isTokenExpired(String token) {
        return extracExpiration(token).before(new Date());
    }
    private Date extracExpiration(String token) {
        return extracClaims(token, Claims::getExpiration);
    }
}
