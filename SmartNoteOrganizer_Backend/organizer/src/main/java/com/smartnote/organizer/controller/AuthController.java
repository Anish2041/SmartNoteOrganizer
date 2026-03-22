package com.smartnote.organizer.controller;

import com.smartnote.organizer.model.User;
import com.smartnote.organizer.repository.UserRepository;
import com.smartnote.organizer.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();

        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            response.put("message", "Username already exists");
            return response;
        }

        userRepository.save(user);
        response.put("message", "User registered successfully");
        return response;
    }

    @PostMapping("/login")
public Map<String, String> login(@RequestBody User user) {
    Map<String, String> response = new HashMap<>();

    try {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser.isEmpty()) {
            response.put("message", "User not found");
            return response;
        }

        if (!existingUser.get().getPassword().equals(user.getPassword())) {
            response.put("message", "Invalid password");
            return response;
        }

        String token = jwtUtil.generateToken(user.getUsername());

        response.put("message", "Login successful");
        response.put("token", token);
        return response;

    } catch (Exception e) {
        e.printStackTrace();
        response.put("message", "Login failed: " + e.getMessage());
        return response;
    }
}
}