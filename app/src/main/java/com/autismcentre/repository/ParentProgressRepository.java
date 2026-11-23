
package com.autismcentre.repository;

import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.ProgressRecordResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class ParentProgressRepository {

    private final ApiService apiService;

    public ParentProgressRepository() {
        apiService = ApiClient.getApiService();
    }

    // Fetch progress records for a specific child
    public void getMyChildProgress(
            Long childId,
            Callback<List<ProgressRecordResponse>> callback) {

        Call<List<ProgressRecordResponse>> call =
                apiService.getMyChildProgress(childId);

        call.enqueue(callback);
    }
}
