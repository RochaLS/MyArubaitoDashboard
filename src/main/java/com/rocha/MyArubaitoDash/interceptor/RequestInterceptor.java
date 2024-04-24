package com.rocha.MyArubaitoDash.interceptor;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class RequestInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler)
            throws Exception {
        // Get the session
        HttpSession session = request.getSession();

        // Get the user ID from the session
        String sessionUserId = (String) session.getAttribute("userId");

        // Get the user ID from the request parameters
        String userId = request.getParameter("userId");

        // Compare the session user ID with the request parameter user ID
        if (sessionUserId != null && userId != null && sessionUserId.equals(userId)) {
            return true; // User ID matches session user ID
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Unauthorized
            return false; // User ID does not match session user ID
        }
    }
}
