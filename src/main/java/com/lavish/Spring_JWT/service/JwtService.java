package com.lavish.Spring_JWT.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    private String secretkey;
    public JwtService(){
        secretkey =generateSecretKey();
    }
    public String generateSecretKey(){
        try{
            KeyGenerator keyGen= KeyGenerator.getInstance("HmacSHA256");
            SecretKey secretKey=keyGen.generateKey();
            System.out.println("Secret Key: "+secretKey.toString());
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException("Error generating secret key", e);
        }
    }
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 3))
                .signWith(getKey())
                .compact();
    }
    public Key getKey(){
        byte[] keyByte= Decoders.BASE64.decode(secretkey);
        return Keys.hmacShaKeyFor(keyByte);
    }

    public String extractUserName(String token) {
        return "";
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return true;
    }
}
