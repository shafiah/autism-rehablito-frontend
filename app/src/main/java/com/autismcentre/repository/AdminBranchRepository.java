
package com.autismcentre.repository;

import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminBranchResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class AdminBranchRepository {

    private final ApiService apiService;

    public AdminBranchRepository() {
        apiService = ApiClient.getApiService();
    }

    public void getAdminBranches(
            Callback<List<AdminBranchResponse>> callback) {

        Call<List<AdminBranchResponse>> call =
                apiService.getAdminBranches();

        call.enqueue(callback);
    }
}
