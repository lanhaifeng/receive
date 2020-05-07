package com.hzmc.auditReceive.domain;

import com.hzmc.auditReceive.annotation.ExcelHeaderProperty;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * receive
 * 2020/5/7 14:40
 *
 * @author lanhaifeng
 * @since
 **/
@Log4j
public abstract class Audit {

	public String getValue(String column) {
		try {
			Field[] fields = this.getClass().getDeclaredFields();
			for (Field field : fields) {
				ExcelHeaderProperty clsExcelHeader = field.getAnnotation(ExcelHeaderProperty.class);
				if (clsExcelHeader != null && clsExcelHeader.headerName().equals(column)) {
					field.setAccessible(true);
					return field.get(this).toString();
				}
			}
		} catch (Exception e) {
			log.error("获取值失败：" + ExceptionUtils.getFullStackTrace(e));
		}
		return "";
	}

	public abstract List<String> notFilterColumns();
}
