package com.keepguard.ms_auth.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = NoForbiddenWordsValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface NoForbiddenWords {
    String message() default "Username contém palavras proibidas";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


