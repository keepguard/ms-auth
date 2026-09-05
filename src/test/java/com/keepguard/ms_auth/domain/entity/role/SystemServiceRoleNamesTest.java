package com.keepguard.ms_auth.domain.entity.role;

import com.keepguard.ms_auth.domain.entity.authority.SystemAuthorityNames;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SystemServiceRoleNames Tests")
class SystemServiceRoleNamesTest {

    @Test
    @DisplayName("templates incluem collector, bff-core, analyst e roles LLM")
    void templatesIncludeCollectorAndLlmServiceRoles() {
        assertTrue(SystemServiceRoleNames.SERVICE_TEMPLATES.contains(SystemServiceRoleNames.ROLE_SERVICE_COLLECTOR));
        assertTrue(SystemServiceRoleNames.SERVICE_TEMPLATES.contains(SystemServiceRoleNames.ROLE_SERVICE_BFF_CORE));
        assertTrue(SystemServiceRoleNames.SERVICE_TEMPLATES.contains(SystemServiceRoleNames.ROLE_SERVICE_ANALYST_FINANCE));
        assertTrue(SystemServiceRoleNames.SERVICE_TEMPLATES.contains(SystemServiceRoleNames.ROLE_SERVICE_MS_AI_GUARDIAN));
        assertTrue(SystemServiceRoleNames.SERVICE_TEMPLATES.contains(SystemServiceRoleNames.ROLE_SERVICE_MS_KNOWLEDGE));
        assertTrue(SystemServiceRoleNames.SERVICE_TEMPLATES.contains(SystemServiceRoleNames.ROLE_SERVICE_MS_ANALYST_FINANCE));
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
    @DisplayName("analyst-finance tem knowledge:read e llm read/write")
    void analystFinanceHasKnowledgeAndLlm() {
        assertEquals(
                java.util.List.of(
                        SystemAuthorityNames.KNOWLEDGE_READ,
                        SystemAuthorityNames.LLM_READ,
                        SystemAuthorityNames.LLM_WRITE),
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

    @Test
    @DisplayName("ms-ai-guardian tem llm:read e llm:write")
    void guardianHasLlmReadWrite() {
        assertEquals(
                java.util.List.of(SystemAuthorityNames.LLM_READ, SystemAuthorityNames.LLM_WRITE),
                SystemServiceRoleNames.authoritiesFor(SystemServiceRoleNames.ROLE_SERVICE_MS_AI_GUARDIAN)
        );
    }

    @Test
    @DisplayName("ms-knowledge tem llm:read e llm:write")
    void knowledgeHasLlmReadWrite() {
        assertEquals(
                java.util.List.of(SystemAuthorityNames.LLM_READ, SystemAuthorityNames.LLM_WRITE),
                SystemServiceRoleNames.authoritiesFor(SystemServiceRoleNames.ROLE_SERVICE_MS_KNOWLEDGE)
        );
    }

    @Test
    @DisplayName("ms-analyst-finance tem knowledge:read e llm read/write")
    void msAnalystFinanceHasKnowledgeAndLlm() {
        assertEquals(
                java.util.List.of(
                        SystemAuthorityNames.KNOWLEDGE_READ,
                        SystemAuthorityNames.LLM_READ,
                        SystemAuthorityNames.LLM_WRITE),
                SystemServiceRoleNames.authoritiesFor(SystemServiceRoleNames.ROLE_SERVICE_MS_ANALYST_FINANCE)
        );
    }
}
