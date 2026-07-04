package com.digitace.creator_directory_api.context;

import java.util.UUID;

public class TenantContext {

    // ThreadLocal ensures that every concurrent API request gets its own isolated memory space
    private static final ThreadLocal<UUID> CURRENT_AGENCY_ID = new ThreadLocal<>();

    public static void setAgencyId(UUID agencyId) {
        CURRENT_AGENCY_ID.set(agencyId);
    }

    public static UUID getAgencyId() {
        return CURRENT_AGENCY_ID.get();
    }

    public static void clear() {
        CURRENT_AGENCY_ID.remove();
    }
}
