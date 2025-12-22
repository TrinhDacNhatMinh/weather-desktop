package com.nhom.weatherdesktop.session;

import com.nhom.weatherdesktop.dto.response.LoginResponse;

public final class SessionContext {

    private static LoginResponse currentUser;

    private SessionContext() {}

    public static void set(LoginResponse loginResponse) {
        currentUser = loginResponse;
    }

    public static LoginResponse get() {
        return currentUser;
    }

    public static String accessToken() {
        return currentUser == null ? null : currentUser.accessToken();
    }

    public static boolean isAuthenticated() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }
}
