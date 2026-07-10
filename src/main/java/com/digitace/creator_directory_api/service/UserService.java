package com.digitace.creator_directory_api.service;

import com.digitace.creator_directory_api.context.TenantContext;
import com.digitace.creator_directory_api.domain.Agency;
import com.digitace.creator_directory_api.domain.RoleType;
import com.digitace.creator_directory_api.domain.User;
import com.digitace.creator_directory_api.dto.InviteUserDto;
import com.digitace.creator_directory_api.repository.AgencyRepository;
import com.digitace.creator_directory_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;

    @Transactional(readOnly = true)
    public List<User> listUsers() {
        UUID agencyId = TenantContext.getAgencyId();
        return userRepository.findAllByAgencyId(agencyId);
    }

    @Transactional
    public User inviteUser(InviteUserDto dto) {
        RoleType callerRole = TenantContext.getRole();

        if (callerRole == RoleType.MEMBER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Members cannot invite users");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A user with this email already exists");
        }

        Agency agency = agencyRepository.findById(TenantContext.getAgencyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agency not found"));

        User newUser = User.builder()
                .agency(agency)
                .email(dto.getEmail())
                .role(dto.getRole())
                .build();

        return userRepository.save(newUser);
    }
}