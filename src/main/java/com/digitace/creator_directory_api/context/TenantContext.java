package com.digitace.creator_directory_api.context;

import com.digitace.creator_directory_api.domain.RoleType;

import java.util.UUID;

public class TenantContext {

    // ThreadLocal ensures that every concurrent API request gets its own isolated memory space
    private static final ThreadLocal<UUID> CURRENT_AGENCY_ID = new ThreadLocal<>();
    private static final ThreadLocal<RoleType> CURRENT_ROLE = new ThreadLocal<>();

    public static void setAgencyId(UUID agencyId) {
        CURRENT_AGENCY_ID.set(agencyId);
    }

    public static UUID getAgencyId() {
        return CURRENT_AGENCY_ID.get();
    }

    public static void setRole(RoleType role) {
        CURRENT_ROLE.set(role);
    }

    public static RoleType getRole() {
        return CURRENT_ROLE.get();
    }

    public static void clear() {
        CURRENT_AGENCY_ID.remove();
        CURRENT_ROLE.remove();
    }
}