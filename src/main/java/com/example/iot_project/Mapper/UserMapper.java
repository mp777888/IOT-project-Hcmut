package com.example.iot_project.Mapper;

import com.example.iot_project.DTO.Request.UserCreateRequest;
import com.example.iot_project.DTO.Request.UserUpdateRequest;
import com.example.iot_project.DTO.Response.UserResponse;
import com.example.iot_project.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest request);

    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
