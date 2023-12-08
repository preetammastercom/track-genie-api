package com.mastercom.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastercom.entity.User;
import com.mastercom.dto.jwtDTO.ErrorResponse;
import com.mastercom.repository.UserRepository;
import com.mastercom.service.JwtService;
import lombok.RequiredArgsConstructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.mastercom.constant.ApplicationConstant.*;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{
	 private final JwtService jwtService;
	    private final UserRepository userRepository;
	    private final AntPathMatcher pathMatcher = new AntPathMatcher();
	    private final ObjectMapper mapper = new ObjectMapper();
	    private static final Logger logger = LogManager.getLogger(JwtAuthenticationFilter.class);
	    @Override
	    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
	                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
	    	 response.setHeader("Access-Control-Allow-Origin", "*"); // Allow any origin (not recommended for production)
	         response.setHeader("Access-Control-Allow-Methods", "*");
	         response.setHeader("Access-Control-Allow-Headers", "*");
	         response.setHeader("Access-Control-Allow-Credentials", "true"); // Allow cookies
	         response.setHeader("Access-Control-Max-Age", "3600"); // Cache pre-flight response for 1 hour

	         if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
	             response.setStatus(HttpServletResponse.SC_OK);
	             return;
	         }

	        String path = request.getRequestURI();
	        if (pathMatcher.match("/trackgenie/public/**", path)
	                || pathMatcher.match("/v3/api-docs/**", path)
	                || pathMatcher.match("/swagger-ui.html", path)
	                || pathMatcher.match("/swagger-ui/**", path)
	                || pathMatcher.match("/trackgenie/common/download/**", path)) {
	            filterChain.doFilter(request, response);
	            return;
	        }
	        final String jwt;
	        final String subject;
	        final User user;
	        final String authHeader = request.getHeader("Authorization");
	        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	        	
	            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
	            response.setStatus(HttpStatus.UNAUTHORIZED.value());
	            ErrorResponse errorResponse = ErrorResponse.builder()
	                    .success(false)
	                    .time(String.valueOf(LocalDateTime.now()))
	                    .journeyId(UUID.randomUUID().toString())
	                    .statusCode(UNAUTHORIZED_ACCESS)
	                    .message(INVALID_OR_MISSING_AUTH_TOKEN)
	                    .build();
	            response.getWriter().write(mapper.writeValueAsString(errorResponse));
	            logger.warn("Invalid or missing token");
	            return;
	        }
	        jwt = authHeader.substring(7);
	        subject = jwtService.extractSubject(jwt);
	user = userRepository.findByUserUniqueKey(subject);
	        
	        if (!jwtService.isTokenValid(jwt, user)) {
	        	
	            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
	            response.setStatus(HttpStatus.UNAUTHORIZED.value());
	            ErrorResponse errorResponse = ErrorResponse.builder()
	                    .success(false)
						.time(String.valueOf(LocalDateTime.now()))
	                    .journeyId(UUID.randomUUID().toString())
	                    .statusCode(UNAUTHORIZED_ACCESS)
	                    .message(UNAUTHORIZED_ACCESS_MSG)
	                    .build();
	            response.getWriter().write(mapper.writeValueAsString(errorResponse));
	        logger.warn("Invalid token");
	            return;
	        }
	        if (isNotAuthorizedForAccess(user, path)) {
	        	
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
	            response.setStatus(HttpStatus.FORBIDDEN.value());
	            ErrorResponse errorResponse = ErrorResponse.builder()
	                    .success(false)
						.time(String.valueOf(LocalDateTime.now()))
	                    .journeyId(UUID.randomUUID().toString())
	                    .statusCode(FORBIDDEN)
	                    .message(FORBIDDEN_MSG)
	                    .build();
	            response.getWriter().write(mapper.writeValueAsString(errorResponse));
	            logger.warn("Access forbidden");
	            return;
	        }
	       request.setAttribute(USER_ID, user.getUserID());
	      
	        filterChain.doFilter(request, response);
	    }

	    private Boolean isNotAuthorizedForAccess(User user, String path) {
	       Boolean isNotAuthorized = true;
	        if (pathMatcher.match("/trackgenie/admin/**", path)
	                && user.getRoles()
	                .stream()
	                .anyMatch(role -> Objects.equals(role.getRoleID(), ADMIN_ROLE_ID))) {
	            isNotAuthorized = false;
	        } else if (pathMatcher.match("/trackgenie/teacher/**", path)
	                && user.getRoles()
	                .stream()
	                .anyMatch(role -> Objects.equals(role.getRoleID(), TEACHER_ROLE_ID))) {
	            isNotAuthorized = false;
	        } else if (pathMatcher.match("/trackgenie/passenger/**", path)
	                && user.getRoles()
	                .stream()
	                .anyMatch(role -> Objects.equals(role.getRoleID(), PASSENGER_ROLE_ID))) {
	            isNotAuthorized = false;
	        } else if (pathMatcher.match("/trackgenie/driver/**", path)
	                && user.getRoles()
	                .stream()
	                .anyMatch(role -> Objects.equals(role.getRoleID(), DRIVER_ROLE_ID))) {
	            isNotAuthorized = false;
	        } else if (pathMatcher.match("/trackgenie/attendant/**", path)
	                && user.getRoles()
	                .stream()
	                .anyMatch(role -> Objects.equals(role.getRoleID(), ATTENDANT_ROLE_ID))) {
	            isNotAuthorized = false;
	        } else if (pathMatcher.match("/trackgenie/driverattendant/**", path)
	                && user.getRoles()
	                .stream()
	                .anyMatch(role -> (Objects.equals(role.getRoleID(), DRIVER_ROLE_ID) || Objects.equals(role.getRoleID(), ATTENDANT_ROLE_ID)))) {
	            isNotAuthorized = false;
	        } 
	        else if (pathMatcher.match("/trackgenie/admindriverattendant/**", path)
	                && user.getRoles()
	                .stream()
	                .anyMatch(role -> (Objects.equals(role.getRoleID(), DRIVER_ROLE_ID) || Objects.equals(role.getRoleID(), ATTENDANT_ROLE_ID) || Objects.equals(role.getRoleID(), ADMIN_ROLE_ID)))) {
	            isNotAuthorized = false;
	        }
	        else if (pathMatcher.match("/trackgenie/driverattendantpassenger/**", path)
	                && user.getRoles()
	                .stream()
	                .anyMatch(role -> (Objects.equals(role.getRoleID(), DRIVER_ROLE_ID) || Objects.equals(role.getRoleID(), ATTENDANT_ROLE_ID) || Objects.equals(role.getRoleID(), PASSENGER_ROLE_ID)))) {
	            isNotAuthorized = false;
	        }
	        else if (pathMatcher.match("/trackgenie/common/**", path)) {
	            isNotAuthorized = false;
	        }

	        return isNotAuthorized;


	       
	    }
}
