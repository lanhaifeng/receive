package com.hzmc.auditReceive.io;

import com.hzmc.auditReceive.domain.LogonAudit;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ExcelTemplateTest {

	@Test
	void writeData() throws IOException {
		ExcelTemplate excelTemplate = new ExcelTemplate("/ouput/", LogonAudit.class);

		IntStream.range(0, 200).forEach(i -> {
			LogonAudit logonAudit = new LogonAudit();
			logonAudit.setId(UUID.randomUUID().toString());
			logonAudit.setRuleName("test" + i);
			excelTemplate.writeData(logonAudit);
		});
		excelTemplate.flush(true);
	}
}