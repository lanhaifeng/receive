package com.hzmc.auditReceive.constant;

/**
 * receive
 * 2020/4/26 17:31
 * 审计类型枚举
 *
 * @author lanhaifeng
 * @since
 **/
public enum AuditType {
	ACCESS("AccessAudit"),
	ACCESS_RESULT("AccessResultAudit"),
	LOGON("LogonAudit"),
	LOGOFF("LogoffAudit"),
	SQL_RESULT("SqlResult"),
	;
	private String type;

	public String getType() {
		return type;
	}

	private AuditType(String type) {
		this.type = type;
	}
}
