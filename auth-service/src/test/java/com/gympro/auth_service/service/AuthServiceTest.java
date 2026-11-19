package com.gympro.auth_service.service;

import com.gympro.auth_service.dto.AuthResponse;
import com.gympro.auth_service.dto.LoginRequest;
import com.gympro.auth_service.dto.RegisterRequest;
import com.gympro.auth_service.entity.Role;
import com.gympro.auth_service.entity.User;
import com.gympro.auth_service.repository.UserRepository;
import com.gympro.auth_service.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest() {
        RegisterRequest r = new RegisterRequest();
        r.setName("Teja");
        r.setEmail("teja@gmail.com");
        r.setPassword("123456");
        return r;                       // role is null, so it should become MEMBER
    }

    @Test
    void register_savesUserWithEncodedPasswordAndDefaultRoleMember() {
        when(userRepository.existsByEmail("teja@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("ENCODED");

        String result = authService.register(registerRequest());

        assertEquals("User registered successfully", result);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("ENCODED", captor.getValue().getPassword());   // never the plain password
        assertEquals(Role.MEMBER, captor.getValue().getRole());
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmail("teja@gmail.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(registerRequest()));

        assertEquals(409, ex.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_correctPassword_returnsToken() {
        User user = User.builder().email("teja@gmail.com").password("ENCODED").role(Role.ADMIN).build();
        when(userRepository.findByEmail("teja@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "ENCODED")).thenReturn(true);
        when(jwtUtil.generateToken("teja@gmail.com", "ADMIN")).thenReturn("TOKEN123");

        LoginRequest req = new LoginRequest();
        req.setEmail("teja@gmail.com");
        req.setPassword("123456");

        AuthResponse res = authService.login(req);

        assertEquals("TOKEN123", res.getToken());
        assertEquals("ADMIN", res.getRole());
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        User user = User.builder().email("teja@gmail.com").password("ENCODED").role(Role.MEMBER).build();
        when(userRepository.findByEmail("teja@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "ENCODED")).thenReturn(false);

        LoginRequest req = new LoginRequest();
        req.setEmail("teja@gmail.com");
        req.setPassword("wrong");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(req));

        assertEquals(401, ex.getStatusCode().value());
        verifyNoInteractions(jwtUtil);   // no token must be created
    }

    @Test
    void login_unknownEmail_throwsUnauthorized() {
        when(userRepository.findByEmail("nobody@gmail.com")).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        req.setEmail("nobody@gmail.com");
        req.setPassword("123456");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(req));

        assertEquals(401, ex.getStatusCode().value());
    }
}