package org.lucas.arbackend.util;

// Used in conjunction with JWT Tokens Claims
public class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    public static void setCurrentTenant(Long currentTenant) {
        TenantContext.CURRENT_TENANT.set(currentTenant);
    }

    public static Long getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
