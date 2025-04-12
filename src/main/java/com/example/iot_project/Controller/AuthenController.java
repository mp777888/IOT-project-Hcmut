package com.example.iot_project.Controller;

import com.example.iot_project.DTO.Request.AuthenRequest;
import com.example.iot_project.DTO.Response.AuthenResponse;
import com.example.iot_project.Service.AuthenticateService;
import com.example.iot_project.Exception.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthenController {

    @Autowired
    private AuthenticateService authenticateService;

    @PostMapping("/token")
    public ApiResponse<AuthenResponse> authenticate(@RequestBody AuthenRequest request) {
        return ApiResponse.<AuthenResponse>builder()
                .code(200)
                .result(authenticateService.login(request))
                .build();
    }


    @GetMapping("/callback")
    public ApiResponse<AuthenResponse> googleLogin(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            String email = oidcUser.getEmail(); // Lấy email từ thông tin Google
//            String name = oidcUser.getFullName(); // Lấy tên từ thông tin Google
//            String id = oidcUser.getSubject(); // Lấy openid từ thông tin Google

            // Gọi service để tạo token JWT nội bộ
            AuthenResponse authenResponse = authenticateService.authenticateByGoogle(email);

            return ApiResponse.<AuthenResponse>builder()
                    .code(200)
                    .message("Login successful")
                    .result(authenResponse)
                    .build();
        }
        return ApiResponse.<AuthenResponse>builder()
                .code(401)
                .message("Authentication failed")
                .result(null)
                .build();
    }


}