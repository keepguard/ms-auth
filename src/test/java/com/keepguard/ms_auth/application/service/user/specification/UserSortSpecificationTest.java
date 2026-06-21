package com.keepguard.ms_auth.application.service.user.specification;

import com.keepguard.ms_auth.domain.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Sort Specification Tests")
class UserSortSpecificationTest {

    private UserSortSpecification userSortSpecification;

    @BeforeEach
    void setUp() {
        userSortSpecification = new UserSortSpecification();
    }

    @Test
    @DisplayName("Deve criar Specification de ordenação")
    void shouldCreateSortSpecification() {
        // When
        Specification<User> spec = userSortSpecification.createSortSpecification("username", "ASC");

        // Then
        assertNotNull(spec);
        // A specification retorna null por enquanto, mas deve existir
        assertNull(spec.toPredicate(null, null, null));
    }

    @Test
    @DisplayName("Deve criar Sort com campo e direção específicos")
    void shouldCreateSortWithSpecificFieldAndDirection() {
        // When
        Sort sort = userSortSpecification.createSort("username", "ASC");

        // Then
        assertNotNull(sort);
        assertEquals(1, sort.get().count());
        assertTrue(sort.get().anyMatch(order -> "username".equals(order.getProperty())));
        assertTrue(sort.get().anyMatch(order -> Sort.Direction.ASC.equals(order.getDirection())));
    }

    @Test
    @DisplayName("Deve criar Sort com direção DESC")
    void shouldCreateSortWithDescDirection() {
        // When
        Sort sort = userSortSpecification.createSort("email", "DESC");

        // Then
        assertNotNull(sort);
        assertEquals(1, sort.get().count());
        assertTrue(sort.get().anyMatch(order -> "email".equals(order.getProperty())));
        assertTrue(sort.get().anyMatch(order -> Sort.Direction.DESC.equals(order.getDirection())));
    }

    @Test
    @DisplayName("Deve criar Sort com direção padrão ASC quando direção inválida")
    void shouldCreateSortWithDefaultAscWhenInvalidDirection() {
        // When
        Sort sort = userSortSpecification.createSort("createdAt", "INVALID");

        // Then
        assertNotNull(sort);
        assertEquals(1, sort.get().count());
        assertTrue(sort.get().anyMatch(order -> "createdAt".equals(order.getProperty())));
        assertTrue(sort.get().anyMatch(order -> Sort.Direction.ASC.equals(order.getDirection())));
    }

    @Test
    @DisplayName("Deve criar Sort com campo padrão createdAt quando campo nulo")
    void shouldCreateSortWithDefaultFieldWhenFieldIsNull() {
        // When
        Sort sort = userSortSpecification.createSort(null, "ASC");

        // Then
        assertNotNull(sort);
        assertEquals(1, sort.get().count());
        assertTrue(sort.get().anyMatch(order -> "createdAt".equals(order.getProperty())));
        assertTrue(sort.get().anyMatch(order -> Sort.Direction.ASC.equals(order.getDirection())));
    }

    @Test
    @DisplayName("Deve criar Sort com campo padrão createdAt quando campo vazio")
    void shouldCreateSortWithDefaultFieldWhenFieldIsEmpty() {
        // When
        Sort sort = userSortSpecification.createSort("", "ASC");

        // Then
        assertNotNull(sort);
        assertEquals(1, sort.get().count());
        assertTrue(sort.get().anyMatch(order -> "createdAt".equals(order.getProperty())));
        assertTrue(sort.get().anyMatch(order -> Sort.Direction.ASC.equals(order.getDirection())));
    }

    @Test
    @DisplayName("Deve criar Sort com direção padrão ASC quando direção nula")
    void shouldCreateSortWithDefaultDirectionWhenDirectionIsNull() {
        // When
        Sort sort = userSortSpecification.createSort("username", null);

        // Then
        assertNotNull(sort);
        assertEquals(1, sort.get().count());
        assertTrue(sort.get().anyMatch(order -> "username".equals(order.getProperty())));
        assertTrue(sort.get().anyMatch(order -> Sort.Direction.ASC.equals(order.getDirection())));
    }

    @Test
    @DisplayName("Deve criar Sort com valores padrão quando ambos são nulos")
    void shouldCreateSortWithDefaultValuesWhenBothAreNull() {
        // When
        Sort sort = userSortSpecification.createSort(null, null);

        // Then
        assertNotNull(sort);
        assertEquals(1, sort.get().count());
        assertTrue(sort.get().anyMatch(order -> "createdAt".equals(order.getProperty())));
        assertTrue(sort.get().anyMatch(order -> Sort.Direction.ASC.equals(order.getDirection())));
    }

    @Test
    @DisplayName("Deve criar Sort com direção case insensitive")
    void shouldCreateSortWithCaseInsensitiveDirection() {
        // When
        Sort sort = userSortSpecification.createSort("username", "desc");

        // Then
        assertNotNull(sort);
        assertEquals(1, sort.get().count());
        assertTrue(sort.get().anyMatch(order -> "username".equals(order.getProperty())));
        assertTrue(sort.get().anyMatch(order -> Sort.Direction.DESC.equals(order.getDirection())));
    }

    @Test
    @DisplayName("Deve criar Sort com direção case insensitive para ASC")
    void shouldCreateSortWithCaseInsensitiveDirectionForAsc() {
        // When
        Sort sort = userSortSpecification.createSort("email", "asc");

        // Then
        assertNotNull(sort);
        assertEquals(1, sort.get().count());
        assertTrue(sort.get().anyMatch(order -> "email".equals(order.getProperty())));
        assertTrue(sort.get().anyMatch(order -> Sort.Direction.ASC.equals(order.getDirection())));
    }

    @Test
    @DisplayName("Deve criar Sort com diferentes campos de ordenação")
    void shouldCreateSortWithDifferentSortFields() {
        // Test cases for different sort fields
        String[] fields = {"username", "email", "createdAt", "updatedAt", "status"};
        
        for (String field : fields) {
            // When
            Sort sort = userSortSpecification.createSort(field, "ASC");
            
            // Then
            assertNotNull(sort);
            assertEquals(1, sort.get().count());
            assertTrue(sort.get().anyMatch(order -> field.equals(order.getProperty())));
            assertTrue(sort.get().anyMatch(order -> Sort.Direction.ASC.equals(order.getDirection())));
        }
    }
}
