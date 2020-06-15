/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.domain.auditlog;

import com.rabobank.argos.service.domain.util.reflection.ReflectionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j(topic = "argos.AuditLog")
@RequiredArgsConstructor
@Order(value = 2)
public class AuditLogAdvisor {
    private final ApplicationContext applicationContext;

    private final ReflectionHelper reflectionHelper;

    @Pointcut("@annotation(auditLog)")
    public void auditLogPointCut(AuditLog auditLog) {
        //This is an AspectJ pointcut implemented as method
    }

    @AfterReturning(value = "auditLogPointCut(auditLog)", argNames = "joinPoint,auditLog,returnValue", returning = "returnValue")
    public void auditLog(JoinPoint joinPoint, AuditLog auditLog, Object returnValue) {
        ArgumentSerializer argumentSerializer = applicationContext
                .getBean(auditLog.argumentSerializerBeanName(), ArgumentSerializer.class);
        Object[] argumentvalues = joinPoint.getArgs();
        String serializedReturnValue = serializeValue(returnValue, argumentSerializer);
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Map<String, String> argumentValues = reflectionHelper.getParameterDataByAnnotation(method, AuditParam.class, argumentvalues).collect(Collectors
                .toMap(p -> p.getAnnotation().value(),
                        p -> serializeValue(p.getValue(), argumentSerializer)
                )
        );
        AuditLogData auditLogData = AuditLogData.builder()
                .argumentData(argumentValues)
                .methodName(method.getName())
                .path(MDC.get("path"))
                .returnValue(serializedReturnValue)
                .build();
        log.info("AuditLog: {}", argumentSerializer.serialize(auditLogData));
    }

    private String serializeValue(Object argumentValue, ArgumentSerializer argumentSerializer) {
        if (argumentValue instanceof String) {
            return (String) argumentValue;
        } else {
            return argumentSerializer.serialize(argumentValue);

        }
    }
}
