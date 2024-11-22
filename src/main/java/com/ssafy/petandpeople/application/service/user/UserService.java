package com.ssafy.petandpeople.application.service.user;

import com.ssafy.petandpeople.application.converter.user.UserConverter;
import com.ssafy.petandpeople.application.converter.user.UserSecurityConverter;
import com.ssafy.petandpeople.application.dto.user.LoginDto;
import com.ssafy.petandpeople.application.dto.user.UserDto;
import com.ssafy.petandpeople.common.exception.user.InvalidSessionException;
import com.ssafy.petandpeople.common.exception.user.UserNotFoundException;
import com.ssafy.petandpeople.common.utils.UUIDGenerator;
import com.ssafy.petandpeople.domain.user.LoginAttempt;
import com.ssafy.petandpeople.domain.user.PasswordEncryptor;
import com.ssafy.petandpeople.domain.user.User;
import com.ssafy.petandpeople.infrastructure.persistence.entity.user.UserEntity;
import com.ssafy.petandpeople.infrastructure.persistence.entity.user.UserSecurityEntity;
import com.ssafy.petandpeople.infrastructure.persistence.repository.user.UserRepository;
import com.ssafy.petandpeople.infrastructure.persistence.repository.user.UserSecurityRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final PasswordEncryptor passwordEncryptor;
    private final LoginAttempt loginAttempt;
    private final UserRepository userRepository;
    private final UserSecurityRepository userSecurityRepository;

    public UserService(PasswordEncryptor passwordEncryptor, LoginAttempt loginAttempt, UserRepository userRepository, UserSecurityRepository userSecurityRepository) {
        this.passwordEncryptor = passwordEncryptor;
        this.loginAttempt = loginAttempt;
        this.userRepository = userRepository;
        this.userSecurityRepository = userSecurityRepository;
    }

    public Boolean signUp(UserDto userDto) {
        String salt = generateRandomSalt();
        String rawPassword = userDto.getUserPassword();
        String encryptedPassword = passwordEncryptor.encryptPassword(rawPassword, salt);

        User user = UserConverter.dtoToDomain(userDto, encryptedPassword);

        UserEntity userEntity = UserConverter.domainToEntity(user);
        UserSecurityEntity userSecurityEntity = UserSecurityConverter.toEntity(user, salt);

        userRepository.save(userEntity);
        userSecurityRepository.save(userSecurityEntity);

        return true;
    }

    public Boolean login(LoginDto loginDto, HttpServletRequest request) {
        String userId = loginDto.getUserId();

        loginAttempt.validateUserLock(userId);
        loginAttempt.increaseLoginAttempt(userId);

        UserEntity userEntity = findLoginUser(userId);
        validateLoginPassword(loginDto, userEntity);

        loginAttempt.resetLoginAttempt(userId);

        saveLoginUserInSession(userEntity.getUserKey(), request);

        return true;
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        validateSession(session);

        session.invalidate();
    }

    private static String generateRandomSalt() {
        return UUIDGenerator.generateUUIDtoString();
    }

    private UserEntity findLoginUser(String userId) {
        return userRepository.findByUserId(userId).orElseThrow(UserNotFoundException::new);
    }

    private void validateLoginPassword(LoginDto loginDto, UserEntity userEntity) {
        String rawLoginPassword = loginDto.getUserPassword();
        String salt = findSalt(userEntity.getUserId());
        String encryptedLoginPassword = passwordEncryptor.encryptPassword(rawLoginPassword, salt);

        User savedUser = UserConverter.entityToDomain(userEntity);

        savedUser.validatePasswordMatch(encryptedLoginPassword);
    }

    private String findSalt(String userId) {
        UserSecurityEntity userSecurityEntity = userSecurityRepository.findByUserId(userId);

        return userSecurityEntity.getSalt();
    }

    private void saveLoginUserInSession(Long userKey, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();

        HttpSession session = request.getSession();
        session.setAttribute("USER_KEY", userKey);
        session.setAttribute("IP_ADDRESS", ipAddress);
    }

    private void validateSession(HttpSession session) {
        if (session == null || session.getAttribute("USER_KEY") == null || session.getAttribute("IP_ADDRESS") == null) {
            throw new InvalidSessionException();
        }
    }

}