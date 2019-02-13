package com.spDeveloper.hongpajee.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import com.spDeveloper.hongpajee.aop.annotation.Accumulated;
import com.spDeveloper.hongpajee.util.map.AccumulatorMap;

@Aspect
@Component
public class AccumulatorAspect {
	@Autowired
	AccumulatorMap accumulatorMap;
	
	@Pointcut("@annotation(accumulated)")
	public void callAt(Accumulated accumulated) {
	}

	@Around("callAt(accumulated)")
	public Object around(ProceedingJoinPoint pjp, Accumulated accumulated) throws Throwable {
		
		String uuid = pjp.getArgs()[0].toString();
		accumulatorMap.incrementAndGet(uuid);
		
		return pjp.proceed();
	}
}