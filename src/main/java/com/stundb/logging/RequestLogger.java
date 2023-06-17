package com.stundb.logging;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestLogger implements MethodInterceptor {

    private final Logger logger = LoggerFactory.getLogger(RequestLogger.class);

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        logger.info("[" + invocation.getMethod().getDeclaringClass().getSimpleName() + "::" + invocation.getMethod().getName() + "] Request received");
        return invocation.proceed();
    }
}
