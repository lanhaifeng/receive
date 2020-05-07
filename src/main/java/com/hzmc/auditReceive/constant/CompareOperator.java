package com.hzmc.auditReceive.constant;

/**
 * receive
 * 2020/5/7 14:06
 * 比较操作符
 *
 * @author lanhaifeng
 * @since
 **/
public enum CompareOperator {
	EQ("eq"),
	NE("ne");

	private String operator;

	public String getOperator() {
		return operator;
	}

	private CompareOperator(String operator) {
		this.operator = operator;
	}

	public static CompareOperator valueOfStr(String operator){
		for (CompareOperator compareOperator : values()) {
			if(compareOperator.getOperator().equalsIgnoreCase(operator))
				return compareOperator;
		}
		return null;
	}
}
