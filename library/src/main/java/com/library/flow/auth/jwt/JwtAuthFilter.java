package com.library.flow.auth.jwt;

import com.library.flow.auth.service.JwtService;
import com.library.flow.auth.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        String header = req.getHeader("Authorization");

        try {
            if (!hasBearerToken(header) || SecurityContextHolder.getContext().getAuthentication() != null) {
                chain.doFilter(req, res);
                return;
            }

            String token = extractToken(header);
            Jws<Claims> jws = jwtService.parse(token);
            Claims claims = jws.getPayload();

            String email = safeLower(claims.getSubject());
            if (!StringUtils.hasText(email)) {
                chain.doFilter(req, res);
                return;
            }

            UserDetails user = userService.loadUserByUsername(email);

            Object uidClaim = claims.get("uid");
            if (uidClaim != null) {
                    req.setAttribute("uid", UUID.fromString(uidClaim.toString()));
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
            log.debug("JWT rejected: {}", ex.getMessage());
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            log.error("Cannot set user authentication: {}", ex.getMessage(), ex);
        }

        chain.doFilter(req, res);
    }

    private boolean hasBearerToken(String header) {
        return StringUtils.hasText(header) && header.startsWith("Bearer ");
    }

    private String extractToken(String header) {
        return header.substring(7).trim();
    }

    private String safeLower(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }
}
