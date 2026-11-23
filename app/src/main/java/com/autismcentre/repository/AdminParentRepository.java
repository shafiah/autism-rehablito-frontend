
package com.autismcentre.repository;

import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminParentOptionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class AdminParentRepository {

    private final ApiService apiService;

    public AdminParentRepository() {
        apiService = ApiClient.getApiService();
    }

    // Existing API - keep for other screens
    public void getAdminParents(
            Callback<List<AdminParentOptionResponse>> callback) {

        Call<List<AdminParentOptionResponse>> call =
                apiService.getAdminParents();

        call.enqueue(callback);
    }

    // New API - get parents by selected branch
    public void getAdminParentsByBranch(
            Long branchId,
            Callback<List<AdminParentOptionResponse>> callback) {

        Call<List<AdminParentOptionResponse>> call =
                apiService.getAdminParentsByBranch(branchId);

        call.enqueue(callback);
    }
}
