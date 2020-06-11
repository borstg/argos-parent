package com.rabobank.argos.service.domain.auditlog;

import com.codepoetics.protonpack.StreamUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogAdvisor {
    private final ApplicationContext applicationContext;

    @Pointcut("@annotation(auditLog)")
    public void auditLogPointCut(AuditLog auditLog) {
        //This is an AspectJ pointcut implemented as method
    }

    @Before(value = "auditLogPointCut(auditLog)", argNames = "joinPoint,auditLog")
    public void auditLog(JoinPoint joinPoint, AuditLog auditLog) {
        Object[] argumentvalues = joinPoint.getArgs();
        Parameter[] parameters = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameters();
        //filter list with excluded parameters
        List<ParameterData> parameterDataList = createParameterData(argumentvalues, parameters, auditLog.excludedParameters());
        Map<String, String> serializedParameters = createSerializedParameters(parameterDataList, auditLog);

    }

    private Map<String, String> createSerializedParameters(List<ParameterData> parameterDataList, AuditLog auditLog) {
        //ArgumentBodySerializer argumentBodySerializer=  applicationContext.getBean(auditLog.bodyArgumentSerializerBeanName());
        return null;
    }

    private List<ParameterData> createParameterData(Object[] argumentvalues,
                                                    Parameter[] parameters,
                                                    String[] excludedParameters) {
        return StreamUtils
                .zipWithIndex(Arrays.stream(parameters))
                .filter(p -> !asList(excludedParameters)
                        .contains(p.getValue().getName()))
                .map(p -> ParameterData.builder()
                        .parameter(p.getValue())
                        .value(argumentvalues[(int) p.getIndex()])
                        .build()).collect(Collectors.toList());
    }


    @Builder
    @Getter
    private static class ParameterData {
        private Parameter parameter;
        private Object value;
    }

}
