package com.example.iot_project.Controller;

import com.example.iot_project.DTO.Request.UserCreateRequest;
import com.example.iot_project.DTO.Request.UserUpdateRequest;
import com.example.iot_project.DTO.Response.UserResponse;
import com.example.iot_project.Exception.ApiResponse;
import com.example.iot_project.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;


    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request){
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping("/profile")
    public ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.getUserProfile())
                .build();
    }

    @PutMapping
    public ApiResponse<UserResponse> updateUser(@RequestBody UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.updateProfile(request))
                .build();
    }

}
