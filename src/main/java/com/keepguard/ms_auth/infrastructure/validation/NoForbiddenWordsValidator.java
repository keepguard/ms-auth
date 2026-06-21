package com.keepguard.ms_auth.infrastructure.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParser;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

@Slf4j
public class NoForbiddenWordsValidator implements ConstraintValidator<NoForbiddenWords, String> {

    private static final String FORBIDDEN_WORDS_PATH = "validation/forbidden-words.json";
    private final Set<String> forbiddenWords = new HashSet<>();

    @Override
    public void initialize(NoForbiddenWords constraintAnnotation) {
        try {
            ClassPathResource resource = new ClassPathResource(FORBIDDEN_WORDS_PATH);
            try (InputStream is = resource.getInputStream()) {
                String json = new Scanner(is, StandardCharsets.UTF_8).useDelimiter("\\A").next();
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
                JsonNode root = mapper.readTree(json);
                JsonNode array = root.get("forbiddenWords");
                if (array != null && array.isArray()) {
                    Iterator<JsonNode> it = array.elements();
                    while (it.hasNext()) {
                        String word = it.next().asText();
                        if (word != null && !word.isBlank()) {
                            forbiddenWords.add(word.toLowerCase());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Não foi possível carregar forbidden-words.json: {}", e.getMessage());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // outras validações (NotBlank) tratam vazio
        }

        String candidate = value.toLowerCase();

        for (String word : forbiddenWords) {
            if (candidate.contains(word)) {
                return false;
            }
        }
        return true;
    }
}


