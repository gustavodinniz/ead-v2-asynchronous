package com.ead.authuser.controllers;

import com.ead.authuser.configs.security.JwtProvider;
import com.ead.authuser.dtos.JwtDTO;
import com.ead.authuser.dtos.LoginDTO;
import com.ead.authuser.dtos.UserDTO;
import com.ead.authuser.enums.RoleType;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.RoleModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.RoleService;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity<Object> registerUser(@RequestBody @Validated(UserDTO.UserView.RegistrationPost.class)
                                               @JsonView(UserDTO.UserView.RegistrationPost.class) UserDTO userDTO) {
        log.debug("POST registerUser userDTO received {} ", userDTO.toString());
        if (userService.existsByUsername(userDTO.getUsername())) {
            log.warn("Username {} is Already Taken ", userDTO.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Username is Already Taken!");
        }

        if (userService.existsByEmail(userDTO.getEmail())) {
            log.warn("Email {} is Already Taken ", userDTO.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Email is Already Taken!");
        }

        RoleModel roleModel = roleService.findByRoleName(RoleType.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Error: Role is Not Found"));

        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        var userModel = new UserModel();
        BeanUtils.copyProperties(userDTO, userModel);
        userModel.setUserStatus(UserStatus.ACTIVE);
        userModel.setUserType(UserType.STUDENT);
        userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.getRoles().add(roleModel);
        userService.saveUser(userModel);
        log.debug("POST registerUser userId saved {} ", userModel.getUserId());
        log.info("User saved successfully userId {} ", userModel.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(userModel);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtDTO> authenticateUser(@Valid @RequestBody LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateJwt(authentication);
        return ResponseEntity.ok(new JwtDTO(jwt));
    }

    @GetMapping("/")
    public String index() {
        log.trace("TRACE");
        log.debug("DEBUG");
        log.info("INFO");
        log.warn("WARN");
        log.error("ERROR");
        return "Logging Spring Boot...";
    }

}
