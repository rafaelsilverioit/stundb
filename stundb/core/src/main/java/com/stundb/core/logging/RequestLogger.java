package com.stundb.core.logging;

import lombok.extern.slf4j.Slf4j;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

@Slf4j
public class RequestLogger implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        log.info(
                "[{}::{}] Request received",
                invocation.getMethod().getDeclaringClass().getSimpleName(),
                invocation.getMethod().getName());
        return invocation.proceed();
    }
}
