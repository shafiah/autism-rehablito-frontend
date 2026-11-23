
package com.autismcentre.api;

import com.autismcentre.utils.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request originalRequest = chain.request();

        String path = originalRequest.url().encodedPath();

        // Authentication endpoints must not use an old JWT.
        if (path.startsWith("/api/parent/auth/")
                || path.startsWith("/api/therapist/auth/")
                || path.startsWith("/api/admin/auth/")) {

            return chain.proceed(originalRequest);
        }

        String token = sessionManager.getToken();

        if (token == null || token.trim().isEmpty()) {
            return chain.proceed(originalRequest);
        }

        Request authenticatedRequest =
                originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();

        return chain.proceed(authenticatedRequest);
    }
}
