package com.nhom.weatherdesktop.service;

import com.nhom.weatherdesktop.dto.request.LoginRequest;
import com.nhom.weatherdesktop.dto.response.LoginResponse;

public interface IAuthService {

    LoginResponse login(LoginRequest request);

}
