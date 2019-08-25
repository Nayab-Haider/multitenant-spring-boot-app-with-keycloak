package com.demo.api.configuration;

import com.demo.api.utils.TenantContextHolder;
import org.apache.catalina.connector.Response;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.keycloak.KeycloakSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class SimpleCORSFilter implements Filter, CurrentTenantIdentifierResolver {

    private static final String TENANT_HEADER_NAME = "X-TENANT-ID";

    private static final String DEFAULT_TENANT_ID = "tenant_1";

    @Autowired
    private KeycloakSecurityContext keycloakSecurityContext;

    private final Logger log = LoggerFactory.getLogger(SimpleCORSFilter.class);

    public SimpleCORSFilter() {
        log.info("SimpleCORSFilter init");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE,PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me,Authorization,realm,access-control-allow-origin,X-TENANT-ID");
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            response.setStatus(Response.SC_OK);
        }
        String tenantId = request.getHeader(TENANT_HEADER_NAME);
        try {
            TenantContextHolder.setTenantId(tenantId);
            chain.doFilter(req, res);
        } finally {
            // Otherwise when a previously used container thread is used, it will have the old tenant id set and
            // if for some reason this filter is skipped, tenantStore will hold an unreliable value
            TenantContextHolder.clear();
        }

    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContextHolder.getTenant();
        return StringUtils.isNotBlank(tenant) ? tenant : DEFAULT_TENANT_ID;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
