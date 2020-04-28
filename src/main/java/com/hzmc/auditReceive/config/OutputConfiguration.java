package com.hzmc.auditReceive.config;

import com.hzmc.auditReceive.constant.AuditType;
import com.hzmc.auditReceive.domain.AccessAudit;
import com.hzmc.auditReceive.domain.LogonAudit;
import com.hzmc.auditReceive.domain.SQLResult;
import com.hzmc.auditReceive.io.ExcelTemplate;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;

/**
 * receive
 * 2020/4/3 10:34
 * 输出配置
 *
 * @author lanhaifeng
 * @since
 **/
@Configuration
public class OutputConfiguration implements InitializingBean {

	@Value("${message.output.path}")
	private String outputPath;
	@Autowired
	private ReceiveConfiguration receiveConfiguration;
	@Autowired
	@Qualifier("taskExecutor")
	private TaskExecutor taskExecutor;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(receiveConfiguration, "receiveConfiguration is Null!");
	}

	@PostConstruct
	public void outputData() throws IOException {
		taskExecutor.execute(() -> {
			LogonAudit logonAudit;
			ExcelTemplate logonExcelTemplate = null;
			try {
				logonExcelTemplate = new ExcelTemplate(outputPath, LogonAudit.class, AuditType.LOGON);
			} catch (IOException e) {
				throw new RuntimeException("logonExcelTemplate构造失败，错误:" + ExceptionUtils.getFullStackTrace(e));
			}
			while (true){
				logonAudit = receiveConfiguration.getLogonMessage().poll();
				Optional.ofNullable(logonAudit).ifPresent(logonExcelTemplate::writeData);
			}
		});

		taskExecutor.execute(() -> {
			LogonAudit logonAudit;
			ExcelTemplate logoffExcelTemplate = null;
			try {
				logoffExcelTemplate = new ExcelTemplate(outputPath, LogonAudit.class, AuditType.LOGOFF);
			} catch (IOException e) {
				throw new RuntimeException("logoffExcelTemplate，错误:" + ExceptionUtils.getFullStackTrace(e));
			}
			while (true){
				logonAudit = receiveConfiguration.getLogonMessage().poll();
				Optional.ofNullable(logonAudit).ifPresent(logoffExcelTemplate::writeData);
			}
		});

		taskExecutor.execute(() -> {
			AccessAudit accessAudit;
			ExcelTemplate accessExcelTemplate = null;
			try {
				accessExcelTemplate = new ExcelTemplate(outputPath, AccessAudit.class, AuditType.ACCESS);
			} catch (IOException e) {
				throw new RuntimeException("logoffExcelTemplate，错误:" + ExceptionUtils.getFullStackTrace(e));
			}
			while (true){
				accessAudit = receiveConfiguration.getAccessMessage().poll();
				Optional.ofNullable(accessAudit).ifPresent(accessExcelTemplate::writeData);
			}
		});

		taskExecutor.execute(() -> {
			AccessAudit accessAudit;
			ExcelTemplate accessResultExcelTemplate = null;
			try {
				accessResultExcelTemplate = new ExcelTemplate(outputPath, AccessAudit.class, AuditType.ACCESS_RESULT);
			} catch (IOException e) {
				throw new RuntimeException("accessResultExcelTemplate，错误:" + ExceptionUtils.getFullStackTrace(e));
			}
			while (true){
				accessAudit = receiveConfiguration.getAccessMessage().poll();
				Optional.ofNullable(accessAudit).ifPresent(accessResultExcelTemplate::writeData);
			}
		});

		taskExecutor.execute(() -> {
			SQLResult sqlResult;
			ExcelTemplate sqlResultExcelTemplate = null;
			try {
				sqlResultExcelTemplate = new ExcelTemplate(outputPath, SQLResult.class, AuditType.SQL_RESULT);
			} catch (IOException e) {
				throw new RuntimeException("sqlResultExcelTemplate，错误:" + ExceptionUtils.getFullStackTrace(e));
			}
			while (true){
				sqlResult = receiveConfiguration.getSqlResultMessage().poll();
				Optional.ofNullable(sqlResult).ifPresent(sqlResultExcelTemplate::writeData);
			}
		});
	}
}
