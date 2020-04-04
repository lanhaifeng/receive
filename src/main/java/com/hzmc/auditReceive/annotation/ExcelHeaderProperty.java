package com.hzmc.auditReceive.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelHeaderProperty {

	//头名
	String headerName() default "";
	//属性方法名
	String valueMethodName() default "";
	//排序
	int order() default 0;
}
