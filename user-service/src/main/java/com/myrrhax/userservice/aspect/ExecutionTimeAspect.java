package com.myrrhax.userservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Profile("dev")
public class ExecutionTimeAspect {
    @Pointcut("execution(* com.myrrhax.userservice.controller.*.*(..))")
    public void executionControllerMethod() {}

    @Around("executionControllerMethod()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long end = System.currentTimeMillis();
            long elapsedTime = end - startTime;
            String signature = joinPoint.getSignature().toString();
            log.info("Controller method {} executed in {} ms", signature, elapsedTime);
        }
    }
}
