package com.example.iot_project.Service;

import com.example.iot_project.DTO.Request.UserCreateRequest;
import com.example.iot_project.DTO.Request.UserUpdateRequest;
import com.example.iot_project.DTO.Response.UserResponse;
import com.example.iot_project.Entity.User;
import com.example.iot_project.Exception.AppException;
import com.example.iot_project.Exception.ErrorCode;
import com.example.iot_project.Mapper.UserMapper;
import com.example.iot_project.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

//    public void updateProfile(UserUpdateRequest request){
//
//    }
    public UserResponse createUser(UserCreateRequest request){
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        try{
            user = userRepository.save(user);
        }
        catch(DataIntegrityViolationException e){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        return userMapper.toUserResponse(user);
    }


    public UserResponse getUserProfile(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        log.info("Getting user profile: {}",name);
        User finduser = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(finduser);
    }

    public UserResponse updateProfile(UserUpdateRequest request){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        log.info("Updating user profile: {}",name);
        User finduser = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(finduser,request);
        try{
            finduser = userRepository.save(finduser);
        }
        catch(DataIntegrityViolationException e){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        return userMapper.toUserResponse(finduser);
    }


}
