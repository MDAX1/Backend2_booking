package com.backend1.backend1.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

/**
 * Läser token från Authorization-headern om den finns (t.ex. framtida API-klienter),
 * annars faller vi tillbaka på token som sparats i webbläsarens session vid inloggning.
 * Detta behövs eftersom vanliga HTML-formulär inte kan skicka en Authorization-header.
 */
public class SessionBearerTokenResolver implements BearerTokenResolver {

    public static final String SESSION_TOKEN_KEY = "AUTH_TOKEN";

    private final BearerTokenResolver headerResolver = new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest request) {
        String headerToken = headerResolver.resolve(request);
        if (headerToken != null) {
            return headerToken;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute(SESSION_TOKEN_KEY);
    }
}