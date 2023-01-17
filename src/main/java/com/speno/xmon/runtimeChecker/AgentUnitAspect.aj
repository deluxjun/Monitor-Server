package com.speno.xmon.runtimeChecker;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AgentUnitAspect {	
	@Pointcut("execution(* *(..))")
	public void start() {}
	@Around("AgentUnitAspect.start()")
	public Object start(ProceedingJoinPoint joinPoint) throws Throwable {			
		Object object = joinPoint.proceed();		
		return object;
	}
}