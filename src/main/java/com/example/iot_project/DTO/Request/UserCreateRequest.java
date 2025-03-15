package com.example.iot_project.DTO.Request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {
    @Size(min = 6,message = "USER_NAME_INVALID")
    String username;
    @Size(min = 6,message = "PASSWORD_INVALID")
    String password;
    String email;
    String firstname;
    String lastname;
    String gender;
    String phonenum;
    LocalDate dob;
}
