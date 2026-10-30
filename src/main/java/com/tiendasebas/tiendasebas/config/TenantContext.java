package com.tiendasebas.config.tenant;

public class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    public static void setCurrentTenant(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long getCurrentTenant() {
        Long tenantId = CURRENT_TENANT.get();
        return (tenantId != null) ? tenantId : 1L; // Fallback predeterminado: Sebas Collection (1)
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}