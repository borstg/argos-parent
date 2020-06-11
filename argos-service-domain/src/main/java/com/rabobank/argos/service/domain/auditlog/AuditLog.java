package com.rabobank.argos.service.domain.auditlog;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    String[] excludedParameters() default {};

    String[] serializerArgumentNames() default {};

    String bodyArgumentSerializerBeanName() default "JsonArgumentSerializer";
}
