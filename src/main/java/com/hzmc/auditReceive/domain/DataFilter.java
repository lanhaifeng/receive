package com.hzmc.auditReceive.domain;

import com.hzmc.auditReceive.constant.AuditType;
import com.hzmc.auditReceive.constant.CompareOperator;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * receive
 * 2020/5/7 14:03
 * 过滤器domain
 *
 * @author lanhaifeng
 * @since
 **/
public class DataFilter implements Serializable {

	private static final long serialVersionUID = 6873531743645742033L;

	private AuditType auditType;
	private String columnName;
	private CompareOperator operator;
	private String value;

	public DataFilter(String auditType, String columnName, String operator, String value) {
		if("access".equals(auditType)){
			this.auditType = AuditType.ACCESS;
		}
		if("logon".equals(auditType)){
			this.auditType = AuditType.LOGON;
		}
		this.columnName = columnName;
		this.operator = CompareOperator.valueOfStr(operator);
		this.value = value;
	}

	public boolean filter(Audit audit){
		boolean result = true;
		boolean typeEqual = auditType == AuditType.ACCESS && (audit instanceof AccessAudit)
				|| auditType == AuditType.LOGON && (audit instanceof LogonAudit);
		if(typeEqual && !audit.notFilterColumns().contains(columnName)){
			String realValue = audit.getValue(columnName);
			switch (operator){
				case EQ:
					result = StringUtils.isBlank(realValue) || realValue.equals(value);
					break;
				case NE:
					result = StringUtils.isBlank(realValue) || !realValue.equals(value);
					break;
			}
		}
		return result;
	}

}
