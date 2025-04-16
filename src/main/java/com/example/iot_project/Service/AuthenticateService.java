package com.example.iot_project.Service;

import com.example.iot_project.DTO.Request.AuthenRequest;
import com.example.iot_project.DTO.Request.IntrospectRequest;
import com.example.iot_project.DTO.Request.LogOutRequest;
import com.example.iot_project.DTO.Request.RefreshRequest;
import com.example.iot_project.DTO.Response.AuthenResponse;
import com.example.iot_project.DTO.Response.IntrospectResponse;
import com.example.iot_project.Entity.InvalidToken;
import com.example.iot_project.Entity.User;
import com.example.iot_project.Exception.AppException;
import com.example.iot_project.Exception.ErrorCode;
import com.example.iot_project.Repository.InvalidatedTokenRepository;
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
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AuthenticateService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refresh-duration}")
    protected long REFRESH_DURATION;


    public AuthenResponse login(AuthenRequest requests) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        //can check email instead of username?
        User user = userRepository.findByUsername(requests.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(requests.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
        }
        String token = generateToken(user);
        return AuthenResponse.builder()
                .token(token)
                .build();
    }

    public AuthenResponse authenticateByGoogle(String email) {
        if(userRepository.findByEmail(email).isPresent()){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        var user = User.builder()
                .email(email)
                .username(email)
                .password(passwordEncoder.encode("123456"))
                .build();
        userRepository.save(user);
        String token = generateToken(user);
        return AuthenResponse.builder()
                .token(token)
                .build();
    }



    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token,false);

        }catch(AppException e){
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public void logout(LogOutRequest request) throws ParseException, JOSEException {

        try {
            var signToken = verifyToken(request.getToken(),true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date ex = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidToken invalidToken = InvalidToken.builder()
                    .id(jit)
                    .expiredDate(ex)
                    .build();
            invalidatedTokenRepository.save(invalidToken);
        } catch(AppException e){
            log.error("Error when logout", e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expirationTime = isRefresh
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESH_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        if (!(verified && expirationTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
        }

        if(invalidatedTokenRepository
                .existsById(signedJWT.getJWTClaimsSet().getJWTID())){
            throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
        }

        return signedJWT;
    }


    public AuthenResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(),true);
        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var ex = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidToken invalidToken = InvalidToken.builder()
                .id(jit)
                .expiredDate(ex)
                .build();
        invalidatedTokenRepository.save(invalidToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String token = generateToken(user);
        return AuthenResponse.builder()
                .token(token)
                .build();
    }


    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("iot.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("name", user.getUsername()) // Thêm thông tin email
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
