
package com.autismcentre.repository;

import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminLoginRequest;
import com.autismcentre.model.AdminLoginResponse;
import com.autismcentre.model.ParentLoginRequest;
import com.autismcentre.model.ParentLoginResponse;
import com.autismcentre.model.ParentRegisterRequest;
import com.autismcentre.model.ParentRegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;

public class AuthRepository {

    private final ApiService apiService;


    public AuthRepository(android.content.Context context) {
        ApiClient.initialize(context);
        apiService = ApiClient.getApiService();
    }


    // Parent Register API
    public void registerParent(
            ParentRegisterRequest request,
            Callback<ParentRegisterResponse> callback
    ) {
        Call<ParentRegisterResponse> call =
                apiService.registerParent(request);

        call.enqueue(callback);
    }

    // Parent Login API
    public void loginParent(
            ParentLoginRequest request,
            Callback<ParentLoginResponse> callback
    ) {
        Call<ParentLoginResponse> call =
                apiService.loginParent(request);

        call.enqueue(callback);
    }

    public void loginAdmin(
            AdminLoginRequest request,
            Callback<AdminLoginResponse> callback
    ) {
        apiService.loginAdmin(request).enqueue(callback);
    }
}
