package com.example.iot_project.Enity;


import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    String id ;
    @Indexed(unique = true)
    String username;
    String password;
    String firstname;
    String lastname;
    String gender;
    String phonenum;
    @Indexed(unique = true)
    String email;
    LocalDate dob;
}
