package org.lucas.arbackend.util;

import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

public class TenantContextDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        // This comes from the current Web thread
        Long tenantId = TenantContext.getCurrentTenant();

        return () -> {
            try {
                // Pass on the tenantId to any new threads
                TenantContext.setCurrentTenant(tenantId);
                runnable.run();
            } finally {
                TenantContext.clear();
            }
        };
    }
}
