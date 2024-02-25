package com.stundb.core.logging;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestLogger implements MethodInterceptor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        logger.info(
                "[{}::{}] Request received",
                invocation.getMethod().getDeclaringClass().getSimpleName(),
                invocation.getMethod().getName());
        return invocation.proceed();
    }
}
