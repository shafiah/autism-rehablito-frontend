
package com.autismcentre.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME =
            "autism_centre_session";

    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PARENT_ID = "parent_id";
    private static final String KEY_THERAPIST_ID = "therapist_id";
    private static final String KEY_BRANCH_ID = "branch_id";
    private static final String KEY_BRANCH_NAME = "branch_name";
    private static final String KEY_THERAPY_TYPE = "therapy_type";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );
    }

    public void saveSession(
            String token,
            Long userId,
            Long parentId,
            String name,
            String email,
            String role
    ) {
        preferences.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID,
                        userId != null ? userId : -1L)
                .putLong(KEY_PARENT_ID,
                        parentId != null ? parentId : -1L)
                .putLong(KEY_THERAPIST_ID, -1L)
                .putLong(KEY_BRANCH_ID, -1L)
                .remove(KEY_BRANCH_NAME)
                .remove(KEY_THERAPY_TYPE)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public void saveTherapistSession(
            String token,
            Long userId,
            Long therapistId,
            Long branchId,
            String branchName,
            String therapyType,
            String name,
            String email,
            String role
    ) {
        preferences.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID,
                        userId != null ? userId : -1L)
                .putLong(KEY_PARENT_ID, -1L)
                .putLong(KEY_THERAPIST_ID,
                        therapistId != null ? therapistId : -1L)
                .putLong(KEY_BRANCH_ID,
                        branchId != null ? branchId : -1L)
                .putString(KEY_BRANCH_NAME, branchName)
                .putString(KEY_THERAPY_TYPE, therapyType)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1L);
    }

    public long getParentId() {
        return preferences.getLong(KEY_PARENT_ID, -1L);
    }

    public long getTherapistId() {
        return preferences.getLong(KEY_THERAPIST_ID, -1L);
    }

    public long getBranchId() {
        return preferences.getLong(KEY_BRANCH_ID, -1L);
    }

    public String getBranchName() {
        return preferences.getString(KEY_BRANCH_NAME, null);
    }

    public String getTherapyType() {
        return preferences.getString(KEY_THERAPY_TYPE, null);
    }

    public String getName() {
        return preferences.getString(KEY_NAME, null);
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, null);
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, null);
    }

    public boolean isLoggedIn() {
        String token = getToken();

        return token != null
                && !token.trim().isEmpty()
                && getUserId() > 0;
    }

    public boolean hasValidSessionData() {
        return hasValidSessionForRole("PARENT")
                && getParentId() > 0;
    }

    public boolean hasValidSessionForRole(String expectedRole) {
        String token = getToken();
        String role = getRole();

        return token != null
                && !token.trim().isEmpty()
                && getUserId() > 0
                && role != null
                && expectedRole != null
                && expectedRole.equalsIgnoreCase(role);
    }

    public boolean hasValidTherapistSession() {
        return hasValidSessionForRole("THERAPIST")
                && getTherapistId() > 0
                && getBranchId() > 0;
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }
}
