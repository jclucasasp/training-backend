//package org.lucas.arbackend.service;
//
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class JwtService {
//
//     private static final String SECRET_KEY = "your-very-secure-and-very-long-secret-key-here";
//    private static final long EXPIRATION_TIME = 86400000; // 24 hours
//
//    public String generateToken(UserDetails userDetails, Long orgId) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("orgId", orgId);
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(userDetails.getUsername())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                .compact();
//    }
//
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        final String username = extractUsername(token);
//        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
//    }
//
//    // TODO: Implement the methods below
//    public Long extractClaim(String token, Object orgId) {
//        return null;
//    }
//
//}
