package com.example.iot_project.Service;

import com.example.iot_project.DTO.Request.AuthenRequest;
import com.example.iot_project.DTO.Request.IntrospectRequest;
import com.example.iot_project.DTO.Response.AuthenResponse;
import com.example.iot_project.DTO.Response.IntrospectResponse;
import com.example.iot_project.Enity.User;
import com.example.iot_project.Exception.AppException;
import com.example.iot_project.Exception.ErrorCode;
import com.example.iot_project.Repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AuthenticateService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;


    public AuthenResponse login(AuthenRequest requests) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        //can check email instead of username?
        User user = userRepository.findByUsername(requests.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(requests.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
        }
        String token = generateToken(requests.getUsername());
        return AuthenResponse.builder()
                .token(token)
                .build();
    }

    public AuthenResponse authenticateByGoogle(String email) {
        if(userRepository.findByEmail(email).isEmpty()){
            User user = User.builder()
                    .email(email)
                    .username(email)
                    .password(passwordEncoder.encode("123456"))
                    .build();
            userRepository.save(user);
        }

        String token = generateToken(email);
        return AuthenResponse.builder()
                .token(token)
                .build();
    }



    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token = request.getToken();

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        return IntrospectResponse.builder()
                .valid(verified && expirationTime.after(new Date()))
                .build();
    }

    private String generateToken(String name) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(name) // Sử dụng email làm subject (có thể thay bằng ID người dùng)
                .issuer("iot.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .claim("email", name) // Thêm thông tin email
                .claim("custom", "demo")
                .build();
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Error when generate token", e);
            throw new RuntimeException(e);
        }

    }
}
