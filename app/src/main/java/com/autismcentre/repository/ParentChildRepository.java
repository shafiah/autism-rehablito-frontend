
package com.autismcentre.repository;

import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.model.ParentChildTherapistResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class ParentChildRepository {

    private final ApiService apiService;

    public ParentChildRepository() {
        apiService = ApiClient.getApiService();
    }

    // Fetch children linked to the currently authenticated parent
    public void getMyChildren(
            Callback<List<ParentChildResponse>> callback
    ) {
        Call<List<ParentChildResponse>> call =
                apiService.getMyChildren();

        call.enqueue(callback);
    }

    // Fetch a specific child's profile
    public void getMyChildById(
            Long childId,
            Callback<ParentChildResponse> callback
    ) {
        Call<ParentChildResponse> call =
                apiService.getMyChildById(childId);

        call.enqueue(callback);
    }

    // Fetch therapists assigned to a specific child
    public void getMyChildTherapists(
            Long childId,
            Callback<List<ParentChildTherapistResponse>> callback
    ) {
        Call<List<ParentChildTherapistResponse>> call =
                apiService.getMyChildTherapists(childId);

        call.enqueue(callback);
    }
}
