package com.digitace.creator_directory_api.controller;

import com.digitace.creator_directory_api.domain.User;
import com.digitace.creator_directory_api.dto.InviteUserDto;
import com.digitace.creator_directory_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @PostMapping
    public ResponseEntity<?> inviteUser(@Valid @RequestBody InviteUserDto dto) {
        User created = userService.inviteUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "User invited successfully.",
                        "id", created.getId(),
                        "email", created.getEmail(),
                        "role", created.getRole()
                ));
    }
}