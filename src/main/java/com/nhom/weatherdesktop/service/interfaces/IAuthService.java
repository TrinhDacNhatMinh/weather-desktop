package com.nhom.weatherdesktop.service.interfaces;

import com.nhom.weatherdesktop.dto.request.LoginRequest;
import com.nhom.weatherdesktop.dto.request.RegisterRequest;
import com.nhom.weatherdesktop.dto.response.LoginResponse;
import com.nhom.weatherdesktop.dto.response.RegisterResponse;

public interface IAuthService {

    LoginResponse login(LoginRequest request);
    
    RegisterResponse register(RegisterRequest request);
    
    void logout();
}
