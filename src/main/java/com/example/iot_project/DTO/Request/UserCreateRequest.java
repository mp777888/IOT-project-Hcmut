package com.example.iot_project.DTO.Request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {
    String username;
    @Size(min = 6,message = "Password must be at least 6 characters")
    String password;
    String email;
    String firstname;
    String lastname;
}
