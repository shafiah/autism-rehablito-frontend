
package com.autismcentre.repository;

import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminChildCreateRequest;
import com.autismcentre.model.AdminChildResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import okhttp3.MultipartBody;

public class AdminChildRepository {

    private final ApiService apiService;

    public AdminChildRepository() {
        apiService = ApiClient.getApiService();
    }

    // Create a new child
    // Create a new child
    public void createAdminChild(
            AdminChildCreateRequest request,
            Callback<AdminChildResponse> callback) {

        Call<AdminChildResponse> call =
                apiService.createAdminChild(request);

        call.enqueue(callback);
    }

    // Upload child profile image
    public void uploadAdminChildImage(
            Long childId,
            MultipartBody.Part image,
            Callback<AdminChildResponse> callback) {

        Call<AdminChildResponse> call =
                apiService.uploadAdminChildImage(childId, image);

        call.enqueue(callback);
    }
    // Fetch all children for Admin Child Management
    public void getAdminChildren(
            Callback<List<AdminChildResponse>> callback) {

        Call<List<AdminChildResponse>> call =
                apiService.getAdminChildren();

        call.enqueue(callback);
    }

    public void getAdminChildById(
            Long childId,
            Callback<AdminChildResponse> callback
    ) {
        Call<AdminChildResponse> call =
                apiService.getAdminChildById(childId);

        call.enqueue(callback);
    }
}
