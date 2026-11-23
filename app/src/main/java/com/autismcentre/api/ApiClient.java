
package com.autismcentre.api;

import android.content.Context;

import com.autismcentre.utils.SessionManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL =
            "http://15.207.116.166:8080/";

    private static Retrofit retrofit;
    private static ApiService apiService;

    public static synchronized void initialize(Context context) {

        if (retrofit != null) {
            return;
        }

        SessionManager sessionManager =
                new SessionManager(context.getApplicationContext());

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(sessionManager))
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized Retrofit getRetrofitInstance() {

        if (retrofit == null) {
            throw new IllegalStateException(
                    "ApiClient is not initialized. Call ApiClient.initialize(context) first."
            );
        }

        return retrofit;
    }

    public static synchronized ApiService getApiService() {

        if (apiService == null) {
            throw new IllegalStateException(
                    "ApiClient is not initialized. Call ApiClient.initialize(context) first."
            );
        }

        return apiService;
    }
}
