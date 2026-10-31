package com.tiendasebas.tiendasebas.config;

/** El tenant se establece exclusivamente en JwtAuthenticationFilter. */
public final class TenantContext {
    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();
    private TenantContext() {}
    public static void setCurrentTenant(Long id) {
        if (id == null) throw new IllegalArgumentException("Tenant obligatorio");
        CURRENT.set(id);
    }
    public static Long getCurrentTenant() {
        Long id = CURRENT.get();
        if (id == null) throw new IllegalStateException("Tenant no resuelto");
        return id;
    }
    public static void clear() { CURRENT.remove(); }
}
