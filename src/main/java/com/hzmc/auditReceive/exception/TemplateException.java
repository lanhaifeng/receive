package com.hzmc.auditReceive.exception;

/**
 * receive
 * 2020/4/3 10:58
 * 模板异常类
 *
 * @author lanhaifeng
 * @since
 **/
public class TemplateException extends RuntimeException {

	public TemplateException() {
		super();
	}

	public TemplateException(String message) {
		super(message);
	}

	public TemplateException(String message, Throwable cause) {
		super(message, cause);
	}

	public TemplateException(Throwable cause) {
		super(cause);
	}
}
