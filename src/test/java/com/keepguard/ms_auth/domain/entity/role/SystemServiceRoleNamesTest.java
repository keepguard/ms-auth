package com.keepguard.ms_auth.domain.entity.role;

import com.keepguard.ms_auth.domain.entity.authority.SystemAuthorityNames;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SystemServiceRoleNames Tests")
class SystemServiceRoleNamesTest {

    @Test
    @DisplayName("templates incluem collector, bff-core e analyst-finance")
    void templatesIncludeCollectorAndBffCore() {
        assertTrue(SystemServiceRoleNames.SERVICE_TEMPLATES.contains(SystemServiceRoleNames.ROLE_SERVICE_COLLECTOR));
        assertTrue(SystemServiceRoleNames.SERVICE_TEMPLATES.contains(SystemServiceRoleNames.ROLE_SERVICE_BFF_CORE));
        assertTrue(SystemServiceRoleNames.SERVICE_TEMPLATES.contains(SystemServiceRoleNames.ROLE_SERVICE_ANALYST_FINANCE));
    }

    @Test
    @DisplayName("BFF Core só tem knowledge:read")
    void bffCoreIsReadOnly() {
        assertEquals(
                java.util.List.of(SystemAuthorityNames.KNOWLEDGE_READ),
                SystemServiceRoleNames.authoritiesFor(SystemServiceRoleNames.ROLE_SERVICE_BFF_CORE)
        );
    }

    @Test
    @DisplayName("analyst-finance só tem knowledge:read")
    void analystFinanceIsReadOnly() {
        assertEquals(
                java.util.List.of(SystemAuthorityNames.KNOWLEDGE_READ),
                SystemServiceRoleNames.authoritiesFor(SystemServiceRoleNames.ROLE_SERVICE_ANALYST_FINANCE)
        );
    }

    @Test
    @DisplayName("collector tem knowledge:write e knowledge:read")
    void collectorHasReadAndWrite() {
        assertEquals(
                java.util.List.of(SystemAuthorityNames.KNOWLEDGE_WRITE, SystemAuthorityNames.KNOWLEDGE_READ),
                SystemServiceRoleNames.authoritiesFor(SystemServiceRoleNames.ROLE_SERVICE_COLLECTOR)
        );
    }
}
