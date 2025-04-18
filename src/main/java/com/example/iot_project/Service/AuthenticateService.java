package com.example.iot_project.Service;

import com.example.iot_project.DTO.Request.AuthenRequest;
import com.example.iot_project.DTO.Request.IntrospectRequest;
import com.example.iot_project.DTO.Request.LogOutRequest;
import com.example.iot_project.DTO.Request.RefreshRequest;
import com.example.iot_project.DTO.Response.AuthenResponse;
import com.example.iot_project.DTO.Response.IntrospectResponse;
import com.example.iot_project.Entity.User;
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
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AuthenticateService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    TokenService tokenService;

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

        User user = userRepository.findByUsername(requests.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(requests.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
        }

        String accessToken = generateToken(user);
        String refreshToken = generateRefreshToken(user);

        // Store refresh token in Redis
        try {
            var jti = SignedJWT.parse(refreshToken).getJWTClaimsSet().getJWTID();
            var expiry = SignedJWT.parse(refreshToken).getJWTClaimsSet().getExpirationTime();
            long ttlSeconds = (expiry.getTime() - System.currentTimeMillis()) / 1000;
            tokenService.storeRefreshToken(jti, ttlSeconds);
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing refresh token", e);
        }

        return AuthenResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenResponse authenticateByGoogle(String email) {
        User user;
        if(!userRepository.findByEmail(email).isPresent()){
            user = User.builder()
                    .email(email)
                    .username(email)
                    .password(passwordEncoder.encode("123456"))
                    .build();
            userRepository.save(user);
        } else {
            user = userRepository.findByEmail(email).get();
        }

        String accessToken = generateToken(user);
        String refreshToken = generateRefreshToken(user);

        // Store refresh token in Redis
        try {
            var jti = SignedJWT.parse(refreshToken).getJWTClaimsSet().getJWTID();
            var expiry = SignedJWT.parse(refreshToken).getJWTClaimsSet().getExpirationTime();
            long ttlSeconds = (expiry.getTime() - System.currentTimeMillis()) / 1000;
            tokenService.storeRefreshToken(jti, ttlSeconds);
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing refresh token", e);
        }

        return AuthenResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }



    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String type = (String) signedJWT.getJWTClaimsSet().getClaim("type");

            // Chỉ chấp nhận token có type là "access"
            if (!"access".equals(type)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
            }
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
            SignedJWT signedJWT = SignedJWT.parse(request.getToken());
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryDate = signedJWT.getJWTClaimsSet().getExpirationTime();

            // Invalidate the access token
            tokenService.invalidateToken(jti, expiryDate);

            // Also invalidate the refresh token if provided
            if (request.getRefreshToken() != null) {
                SignedJWT refreshJWT = SignedJWT.parse(request.getRefreshToken());
                String refreshJti = refreshJWT.getJWTClaimsSet().getJWTID();
                tokenService.invalidateRefreshToken(refreshJti);
            }
        } catch (Exception e) {
            log.error("Error during logout", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
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

        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        if (jti != null && tokenService.isTokenInvalidated(jti)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
        }

        return signedJWT;
    }


    public AuthenResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(),true);
        var jti = signedJWT.getJWTClaimsSet().getJWTID();

        // Validate refresh token
        if (!tokenService.isRefreshTokenValid(jti)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
        }

        tokenService.invalidateRefreshToken(jti);

        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String newAccessToken = generateToken(user);
        String newRefreshToken = generateRefreshToken(user);

        // Store the new refresh token in Redis
        var newJti = SignedJWT.parse(newRefreshToken).getJWTClaimsSet().getJWTID();
        var refreshExpiry = SignedJWT.parse(newRefreshToken).getJWTClaimsSet().getExpirationTime();
        long ttlSeconds = (refreshExpiry.getTime() - System.currentTimeMillis()) / 1000;
        tokenService.storeRefreshToken(newJti, ttlSeconds);

        return AuthenResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
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
                .claim("type", "access")
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

    private String generateRefreshToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("iot.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(REFRESH_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .claim("type", "refresh")
                .jwtID(UUID.randomUUID().toString())
                .build();
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Error when generating refresh token", e);
            throw new RuntimeException(e);
        }
    }

}
