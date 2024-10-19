package com.keremyardan.nosql_auth.controllers;

import com.keremyardan.nosql_auth.dto.request.LoginRequest;
import com.keremyardan.nosql_auth.dto.request.SignUprequest;
import com.keremyardan.nosql_auth.dto.response.JwtResponse;
import com.keremyardan.nosql_auth.dto.response.MessageResponse;
import com.keremyardan.nosql_auth.repository.RoleRepository;
import com.keremyardan.nosql_auth.repository.UserRepository;
import com.keremyardan.nosql_auth.security.jwt.JwtUtils;
import com.keremyardan.nosql_auth.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "+", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

       return ResponseEntity.ok(new JwtResponse(
               jwt,
               userDetails.getId(),
               userDetails.getUsername(),
               userDetails.getEmail(),
               roles
       ));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUprequest signUprequest) {
        if (userRepository.existsByUserName(signUprequest.getUsername())){
            return ResponseEntity.badRequest().body(new MessageResponse("Username is already taken!"));
        }
    }
}
