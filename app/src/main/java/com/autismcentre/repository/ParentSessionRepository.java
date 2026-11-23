
package com.autismcentre.repository;

import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.ParentSessionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class ParentSessionRepository {

    private final ApiService apiService;

    public ParentSessionRepository() {
        apiService = ApiClient.getApiService();
    }

    // Fetch all sessions for a specific child
    public void getMyChildSessions(
            Long childId,
            Callback<List<ParentSessionResponse>> callback
    ) {
        Call<List<ParentSessionResponse>> call =
                apiService.getMyChildSessions(childId);

        call.enqueue(callback);
    }

    // Fetch details of a specific session
    public void getMyChildSessionById(
            Long childId,
            Long sessionId,
            Callback<ParentSessionResponse> callback
    ) {
        Call<ParentSessionResponse> call =
                apiService.getMyChildSessionById(
                        childId,
                        sessionId
                );

        call.enqueue(callback);
    }
}
