package com.keepguard.ms_auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para a classe principal MsAuthApplication
 * Teste simples para cobertura sem usar H2 ou carregar contexto completo
 */
@DisplayName("Ms Auth Application Tests")
class MsAuthApplicationTest {
    
    @Test
    @DisplayName("Deve verificar se a classe principal existe")
    void shouldVerifyMainApplicationClassExists() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        
        // When & Then
        assertNotNull(applicationClass, "Classe MsAuthApplication deve existir");
        assertEquals("MsAuthApplication", applicationClass.getSimpleName(), "Nome da classe deve ser correto");
        assertEquals("com.keepguard.ms_auth", applicationClass.getPackageName(), "Package deve ser correto");
    }
    
    @Test
    @DisplayName("Deve verificar se a classe principal tem método main")
    void shouldVerifyMainMethodExists() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        
        try {
            // When
            applicationClass.getDeclaredMethod("main", String[].class);
            
            // Then
            assertTrue(true, "Método main encontrado na classe principal");
        } catch (NoSuchMethodException e) {
            fail("Método main não encontrado na classe principal");
        }
    }
    
    @Test
    @DisplayName("Deve verificar anotações da classe principal")
    void shouldVerifyMainApplicationAnnotations() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        
        // When & Then
        assertTrue(applicationClass.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class),
                "Classe deve ter anotação @SpringBootApplication");
        
        assertTrue(applicationClass.isAnnotationPresent(org.springframework.context.annotation.Import.class),
                "Classe deve ter anotação @Import");
    }
    
    @Test
    @DisplayName("Deve verificar configuração do SpringBootApplication")
    void shouldVerifySpringBootApplicationConfiguration() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        org.springframework.boot.autoconfigure.SpringBootApplication springBootAnnotation = 
            applicationClass.getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
        
        // When & Then
        assertNotNull(springBootAnnotation, "Anotação @SpringBootApplication deve existir");
        
        String[] scanBasePackages = springBootAnnotation.scanBasePackages();
        assertNotNull(scanBasePackages, "scanBasePackages deve estar configurado");
        assertEquals(2, scanBasePackages.length, "Deve ter 2 packages configurados");
        assertEquals("com.keepguard.ms_auth", scanBasePackages[0], "Primeiro package deve ser com.keepguard.ms_auth");
        assertEquals("com.keepguard.lib_common", scanBasePackages[1], "Segundo package deve ser com.keepguard.lib_common");
    }
    
    @Test
    @DisplayName("Deve verificar configuração do Import")
    void shouldVerifyImportConfiguration() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        org.springframework.context.annotation.Import importAnnotation = 
            applicationClass.getAnnotation(org.springframework.context.annotation.Import.class);
        
        // When & Then
        assertNotNull(importAnnotation, "Anotação @Import deve existir");
        
        Class<?>[] value = importAnnotation.value();
        assertNotNull(value, "value deve estar configurado");
        assertEquals(1, value.length, "Deve ter 1 classe importada");
        assertEquals(com.keepguard.lib_common.config.MetricsConfig.class, value[0], 
                "Deve importar MetricsConfig");
    }
    
    @Test
    @DisplayName("Deve verificar se a classe principal é pública")
    void shouldVerifyMainApplicationClassIsPublic() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        
        // When & Then
        assertTrue(java.lang.reflect.Modifier.isPublic(applicationClass.getModifiers()),
                "Classe principal deve ser pública");
    }
    
    @Test
    @DisplayName("Deve verificar se o método main é público e estático")
    void shouldVerifyMainMethodIsPublicAndStatic() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        
        try {
            // When
            java.lang.reflect.Method mainMethod = applicationClass.getDeclaredMethod("main", String[].class);
            
            // Then
            assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()),
                    "Método main deve ser público");
            assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()),
                    "Método main deve ser estático");
        } catch (NoSuchMethodException e) {
            fail("Método main não encontrado na classe principal");
        }
    }
    
    @Test
    @DisplayName("Deve verificar se a classe principal não é abstrata")
    void shouldVerifyMainApplicationClassIsNotAbstract() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        
        // When & Then
        assertFalse(java.lang.reflect.Modifier.isAbstract(applicationClass.getModifiers()),
                "Classe principal não deve ser abstrata");
    }
    
    @Test
    @DisplayName("Deve verificar se a classe principal não é final")
    void shouldVerifyMainApplicationClassIsNotFinal() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        
        // When & Then
        assertFalse(java.lang.reflect.Modifier.isFinal(applicationClass.getModifiers()),
                "Classe principal não deve ser final");
    }
    
    @Test
    @DisplayName("Deve verificar se a classe principal não é interface")
    void shouldVerifyMainApplicationClassIsNotInterface() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        
        // When & Then
        assertFalse(applicationClass.isInterface(), "Classe principal não deve ser interface");
    }
    
    @Test
    @DisplayName("Deve verificar se a classe principal não é enum")
    void shouldVerifyMainApplicationClassIsNotEnum() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        
        // When & Then
        assertFalse(applicationClass.isEnum(), "Classe principal não deve ser enum");
    }
    
    @Test
    @DisplayName("Deve verificar se a classe principal não é anotação")
    void shouldVerifyMainApplicationClassIsNotAnnotation() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        
        // When & Then
        assertFalse(applicationClass.isAnnotation(), "Classe principal não deve ser anotação");
    }
    
    @Test
    @DisplayName("Deve verificar se a classe principal tem construtor padrão")
    void shouldVerifyMainApplicationClassHasDefaultConstructor() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        
        try {
            // When
            applicationClass.getDeclaredConstructor();
            
            // Then
            assertTrue(true, "Construtor padrão encontrado na classe principal");
        } catch (NoSuchMethodException e) {
            fail("Construtor padrão não encontrado na classe principal");
        }
    }
    
    @Test
    @DisplayName("Deve verificar se a classe principal herda de Object")
    void shouldVerifyMainApplicationClassExtendsObject() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        
        // When & Then
        assertEquals(Object.class, applicationClass.getSuperclass(),
                "Classe principal deve herdar de Object");
    }
    
    @Test
    @DisplayName("Deve verificar se a classe principal não implementa interfaces")
    void shouldVerifyMainApplicationClassImplementsNoInterfaces() {
        // Given
        Class<?> applicationClass = MsAuthApplication.class;
        
        // When
        Class<?>[] interfaces = applicationClass.getInterfaces();
        
        // Then
        assertEquals(0, interfaces.length, "Classe principal não deve implementar interfaces");
    }
}
