package com.consultores.optiplant.aptiplantback.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = authHeader.substring(BEARER_PREFIX.length());
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String username;
        try {
            username = jwtUtil.extractUsername(jwtToken);
        } catch (Exception ex) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtUtil.isTokenValid(jwtToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String rolClaim = jwtUtil.extractClaim(jwtToken, claims -> claims.get("rol", String.class));
        Object sucursalClaimValue = jwtUtil.extractClaim(jwtToken, claims -> claims.get("sucursalId"));
        Long sucursalIdClaim = (sucursalClaimValue instanceof Number numberValue)
            ? numberValue.longValue()
            : null;

        List<GrantedAuthority> authorities;
        if (rolClaim == null || rolClaim.isBlank()) {
            authorities = List.copyOf(userDetails.getAuthorities());
        } else {
            String authorityValue = rolClaim.startsWith("ROLE_") ? rolClaim : "ROLE_" + rolClaim;
            authorities = List.of(new SimpleGrantedAuthority(authorityValue));
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            authorities
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        request.setAttribute("jwt.email", username);
        request.setAttribute("jwt.rol", rolClaim);
        request.setAttribute("jwt.sucursalId", sucursalIdClaim);

        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}

