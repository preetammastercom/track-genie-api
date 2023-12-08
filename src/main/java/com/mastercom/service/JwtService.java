package com.mastercom.service;

import com.mastercom.entity.User;
import com.mastercom.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import static com.mastercom.constant.ApplicationConstant.ADMIN_ROLE_ID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Objects.nonNull;
import static com.mastercom.constant.ApplicationConstant.DEVICE_ID;

@Service
public class JwtService {

	@Value("${application.security.jwt.secret-key}")
	private String secretKey;
	@Value("${application.security.jwt.expiration}")
	private long jwtExpiration;

	@Autowired
	private UserRepository userRepository;

	public String extractSubject(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String generateToken(User user) {

		return generateToken(new HashMap<>(), user);
	}

	public String generateToken(Map<String, Object> extraClaims, User user) {
		return buildToken(extraClaims, user);
	}

	private String buildToken(Map<String, Object> extraClaims, User user) {
		return Jwts.builder().setClaims(extraClaims).setSubject(user.getUserUniqueKey())
				.setIssuedAt(new Date(System.currentTimeMillis())).signWith(getSignInKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	public boolean isTokenValid(String token, User user) {
		final String subject = extractSubject(token);
		final String deviceID = extractKey(token, DEVICE_ID, String.class);
		if(user.getRoles().stream().anyMatch(role -> Objects.equals(role.getRoleID(), ADMIN_ROLE_ID))) {
			return (subject.equals(user.getUserUniqueKey())) && nonNull(user.getJwtToken())
					&& token.equals(user.getJwtToken());
		}
		else {
			return (subject.equals(user.getUserUniqueKey())) && nonNull(user.getJwtToken())
					&& token.equals(user.getJwtToken()) && user.getDeviceID().equals(deviceID);
		}
		
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
	}

	private Key getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public <T> T extractKey(String token, String key, Class<T> valueType) {
		final Claims claims = extractAllClaims(token);
		return claims.get(key, valueType);
	}
}
