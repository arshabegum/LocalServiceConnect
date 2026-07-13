package com.LocalServe.LocalServe.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            // Not logged in, redirect to login page
            response.sendRedirect("/login?error=Please login to access this page");
            return false;
        }

        return true;
    }
}
