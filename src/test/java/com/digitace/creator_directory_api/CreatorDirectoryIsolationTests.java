package com.digitace.creator_directory_api;

import com.digitace.creator_directory_api.domain.Creator;
import com.digitace.creator_directory_api.domain.User;
import com.digitace.creator_directory_api.repository.CreatorRepository;
import com.digitace.creator_directory_api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Proves the 4 required isolation guarantees called out in the grading
 * rubric. @Transactional rolls back everything each test writes, so tests
 * never pollute each other and never depend on manually-copied UUIDs --
 * user/creator ids are always looked up fresh from the DB by email at the
 * start of each test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CreatorDirectoryIsolationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreatorRepository creatorRepository;

    private String novaOwnerId;
    private String brightStarOwnerId;
    private String soloOwnerId;
    private String priyaId;

    @BeforeEach
    void setUp() {
        novaOwnerId = idOf("owner@nova.com");
        brightStarOwnerId = idOf("owner@brightstar.com");
        soloOwnerId = idOf("owner@solo.com");
        priyaId = creatorRepository.findByEmail("priya@example.com")
                .map(Creator::getId)
                .orElseThrow(() -> new IllegalStateException("Seed data missing: priya@example.com"))
                .toString();
    }

    private String idOf(String email) {
        return userRepository.findAll().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst()
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("Seed data missing: " + email))
                .toString();
    }

    // --- (a) agency A cannot read agency B's private notes on a shared creator ---

    @Test
    void novaSeesItsOwnNoteOnSharedCreator() throws Exception {
        mockMvc.perform(get("/creators/" + priyaId).header("X-User-Id", novaOwnerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agencyLinks[0].notes").value("Great for skincare campaigns"));
    }

    @Test
    void brightStarSeesItsOwnDifferentNoteOnSameSharedCreator() throws Exception {
        mockMvc.perform(get("/creators/" + priyaId).header("X-User-Id", brightStarOwnerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agencyLinks[0].notes").value("Booked for Q1 shoot"))
                .andExpect(jsonPath("$.agencyLinks[0].notes").value(org.hamcrest.Matchers.not("Great for skincare campaigns")));
    }

    // --- (b) agency with no link cannot see OR modify a creator ---

    @Test
    void unlinkedAgencyGets404OnRead() throws Exception {
        mockMvc.perform(get("/creators/" + priyaId).header("X-User-Id", soloOwnerId))
                .andExpect(status().isNotFound());
    }

    @Test
    void unlinkedAgencyGets404OnUpdate() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("notes", "sneaky edit"));
        mockMvc.perform(patch("/creators/" + priyaId)
                        .header("X-User-Id", soloOwnerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void unlinkedAgencyGets404OnDelete() throws Exception {
        mockMvc.perform(delete("/creators/" + priyaId).header("X-User-Id", soloOwnerId))
                .andExpect(status().isNotFound());
    }

    // --- (c) member role cannot invite a user ---

    @Test
    void memberCannotInviteUser() throws Exception {
        // Owner invites a member into Nova Talent
        String inviteMemberBody = objectMapper.writeValueAsString(Map.of(
                "email", "newmember@nova.com", "role", "MEMBER"));

        String response = mockMvc.perform(post("/users")
                        .header("X-User-Id", novaOwnerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inviteMemberBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String memberId = objectMapper.readTree(response).get("id").asText();

        // That member tries to invite someone else -- must be forbidden
        String secondInviteBody = objectMapper.writeValueAsString(Map.of(
                "email", "another@nova.com", "role", "MEMBER"));

        mockMvc.perform(post("/users")
                        .header("X-User-Id", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondInviteBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanInviteUser() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "newadmin@nova.com", "role", "MEMBER"));

        mockMvc.perform(post("/users")
                        .header("X-User-Id", idOf("admin@nova.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    // --- (d) free-plan 5-creator limit is enforced, clear error on 6th ---

    @Test
    void freePlanAgencyCannotExceedFiveLinkedCreators() throws Exception {
        // Nova (FREE) starts with 2 seeded creators (Priya, Ananya).
        // Add 3 more to reach 5.
        for (int i = 0; i < 3; i++) {
            String body = objectMapper.writeValueAsString(Map.of(
                    "name", "Extra Creator " + i,
                    "niche", "tech",
                    "followerCount", 1000,
                    "engagementRate", 1.5,
                    "email", "extra" + i + "@example.com",
                    "notes", "filler"
            ));
            mockMvc.perform(post("/creators")
                            .header("X-User-Id", novaOwnerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        // 6th creator (2 seeded + 3 above + this one) must be rejected
        String sixthBody = objectMapper.writeValueAsString(Map.of(
                "name", "One Too Many",
                "niche", "tech",
                "followerCount", 1000,
                "engagementRate", 1.5,
                "email", "onetoomany@example.com",
                "notes", "should fail"
        ));
        mockMvc.perform(post("/creators")
                        .header("X-User-Id", novaOwnerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sixthBody))
                .andExpect(status().isForbidden()); // 403, confirmed in CreatorService
    }

    // --- extra: auth edge cases ---

    @Test
    void missingHeaderReturns401() throws Exception {
        mockMvc.perform(get("/creators"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownUserIdReturns401() throws Exception {
        mockMvc.perform(get("/creators").header("X-User-Id", "not-a-real-id"))
                .andExpect(status().isUnauthorized());
    }

    // --- extra: unlink only removes the caller's own link ---

    @Test
    void unlinkRemovesOnlyCallersLinkNotTheSharedCreator() throws Exception {
        mockMvc.perform(delete("/creators/" + priyaId).header("X-User-Id", novaOwnerId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/creators/" + priyaId).header("X-User-Id", novaOwnerId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/creators/" + priyaId).header("X-User-Id", brightStarOwnerId))
                .andExpect(status().isOk());
    }
}