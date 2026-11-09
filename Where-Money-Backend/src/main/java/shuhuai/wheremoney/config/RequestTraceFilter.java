package shuhuai.wheremoney.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import shuhuai.wheremoney.utils.TokenValidator;
import java.io.IOException;
import java.util.UUID;

@Component
public class RequestTraceFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        MDC.put("requestId", UUID.randomUUID().toString());
        response.setHeader("X-Request-Id", MDC.get("requestId"));
        try { chain.doFilter(request, response); }
        finally { TokenValidator.removeUser(); MDC.clear(); }
    }
}
