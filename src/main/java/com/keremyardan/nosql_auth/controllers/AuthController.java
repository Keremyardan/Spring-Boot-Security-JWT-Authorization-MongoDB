package com.keremyardan.nosql_auth.controllers;

import com.keremyardan.nosql_auth.Entity.ERole;
import com.keremyardan.nosql_auth.Entity.Role;
import com.keremyardan.nosql_auth.Entity.User;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
            return ResponseEntity.
                    badRequest().body(new MessageResponse("Username is already taken!"));
        }
        if(userRepository.existByEmail(signUprequest.getEmail())) {
            return ResponseEntity
                    .badRequest().body(new MessageResponse("Email is already in use!"));
        }
        User user = new User(signUprequest.getEmail(), signUprequest.getUsername(),
                passwordEncoder.encode(signUprequest.getPassword()));

        Set<String> strRoles = signUprequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Role has not found!"));
            roles.add(userRole);
        }else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin" :
                        Role adminrole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Role has not found!"));
                        roles.add(adminrole);
                        break;
                    case "mod" : Role modrole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                            .orElseThrow(() -> new RuntimeException("Role has not found"));
                        roles.add(modrole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Role has not found!"));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
