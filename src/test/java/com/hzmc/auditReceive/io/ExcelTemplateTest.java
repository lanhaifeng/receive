package com.hzmc.auditReceive.io;

import com.hzmc.auditReceive.constant.AuditType;
import com.hzmc.auditReceive.domain.LogonAudit;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

class ExcelTemplateTest {

	@Test
	@Ignore
	void writeData() throws IOException {
		ExcelTemplate excelTemplate = new ExcelTemplate("/ouput/", LogonAudit.class, AuditType.LOGON);

		IntStream.range(0, 200).forEach(i -> {
			LogonAudit logonAudit = new LogonAudit();
			logonAudit.setId(UUID.randomUUID().toString());
			logonAudit.setRuleName("test" + i);
			excelTemplate.writeData(logonAudit);
		});
		excelTemplate.flush(true);
	}

}