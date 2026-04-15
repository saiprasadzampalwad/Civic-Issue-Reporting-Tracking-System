package com.civicissues.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CsrfUtil.java - Per-session CSRF token generation and validation.
 *
 * Usage in Servlet (validate before processing POST):
 *   if (!CsrfUtil.isValid(request)) { response.sendError(403); return; }
 *
 * Usage in JSP (embed hidden field inside every form):
 *   <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"/>
 */
public class CsrfUtil {

    private static final String SESSION_KEY  = "csrfToken";
    private static final String FORM_PARAM   = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Get existing token from session, or generate + store a new one. */
    public static String getOrCreate(HttpSession session) {
        String token = (String) session.getAttribute(SESSION_KEY);
        if (token == null) {
            byte[] bytes = new byte[24];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(SESSION_KEY, token);
        }
        return token;
    }

    /** Validate the CSRF token submitted with a POST request. */
    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        String sessionToken = (String) session.getAttribute(SESSION_KEY);
        String formToken    = request.getParameter(FORM_PARAM);
        return sessionToken != null && sessionToken.equals(formToken);
    }
}
