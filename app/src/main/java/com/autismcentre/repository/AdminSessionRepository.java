package com.autismcentre.repository;

import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminSessionCreateRequest;
import com.autismcentre.model.AdminSessionResponse;
import com.autismcentre.model.AdminSessionUpdateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class AdminSessionRepository {

    private final ApiService apiService;

    public AdminSessionRepository() {
        apiService = ApiClient.getApiService();

    }

    // Create a new session
    public void createSession(
            AdminSessionCreateRequest request,
            Callback<AdminSessionResponse> callback) {

        apiService.createAdminSession(request)
                .enqueue(callback);
    }

    // Get all sessions
    public void getAllSessions(
            Callback<List<AdminSessionResponse>> callback) {

        apiService.getAdminSessions(null)
                .enqueue(callback);
    }

    // Get sessions by branch
    public void getSessionsByBranch(
            Long branchId,
            Callback<List<AdminSessionResponse>> callback) {

        apiService.getAdminSessions(branchId)
                .enqueue(callback);
    }

    // Get session details
    public void getSessionById(
            Long sessionId,
            Callback<AdminSessionResponse> callback) {

        apiService.getAdminSessionById(sessionId)
                .enqueue(callback);
    }

    // Update session
    public void updateSession(
            Long sessionId,
            AdminSessionUpdateRequest request,
            Callback<AdminSessionResponse> callback) {

        apiService.updateAdminSession(sessionId, request)
                .enqueue(callback);
    }

    // Cancel session
    public void cancelSession(
            Long sessionId,
            Callback<AdminSessionResponse> callback) {

        apiService.cancelAdminSession(sessionId)
                .enqueue(callback);
    }
}