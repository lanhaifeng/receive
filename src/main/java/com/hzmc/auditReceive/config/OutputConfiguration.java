package com.hzmc.auditReceive.config;

import com.hzmc.auditReceive.constant.AuditType;
import com.hzmc.auditReceive.domain.AccessAudit;
import com.hzmc.auditReceive.domain.LogonAudit;
import com.hzmc.auditReceive.domain.SQLResult;
import com.hzmc.auditReceive.io.ExcelTemplate;
import lombok.extern.log4j.Log4j;
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
import java.util.Objects;
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
@Log4j
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
			ExcelTemplate logoffExcelTemplate = null;
			try {
				logonExcelTemplate = new ExcelTemplate(outputPath, LogonAudit.class, AuditType.LOGON);
				logoffExcelTemplate = new ExcelTemplate(outputPath, LogonAudit.class, AuditType.LOGOFF);
			} catch (IOException e) {
				throw new RuntimeException("logonAudit write Template构造失败，错误:" + ExceptionUtils.getFullStackTrace(e));
			}
			long lastTime = System.currentTimeMillis();
			while (true){
				logonAudit = receiveConfiguration.getLogonMessage().poll();
				if(Objects.nonNull(logonAudit)){
					if(logonAudit.getLogonResult()){
						logoffExcelTemplate.writeData(logonAudit);
					}else {
						logonExcelTemplate.writeData(logonAudit);
					}
					lastTime = System.currentTimeMillis();
				}
				if(System.currentTimeMillis() - lastTime > 10000){
					try{
						Thread.sleep(1000l);
					}catch(Exception e){
					    log.error("睡眠失败" + ExceptionUtils.getFullStackTrace(e));
					}
				}
			}
		});

		taskExecutor.execute(() -> {
			AccessAudit accessAudit;
			ExcelTemplate accessExcelTemplate = null;
			ExcelTemplate accessResultExcelTemplate = null;
			try {
				accessExcelTemplate = new ExcelTemplate(outputPath, AccessAudit.class, AuditType.ACCESS);
				accessResultExcelTemplate = new ExcelTemplate(outputPath, AccessAudit.class, AuditType.ACCESS_RESULT);
			} catch (IOException e) {
				throw new RuntimeException("logoffExcelTemplate，错误:" + ExceptionUtils.getFullStackTrace(e));
			}
			long lastTime = System.currentTimeMillis();
			while (true){
				accessAudit = receiveConfiguration.getAccessMessage().poll();
				if(Objects.nonNull(accessAudit)) {
					if (accessAudit.getAccessResult()) {
						accessResultExcelTemplate.writeData(accessAudit);
					} else {
						accessExcelTemplate.writeData(accessAudit);
					}
					lastTime = System.currentTimeMillis();
				}
				if(System.currentTimeMillis() - lastTime > 10000){
					try{
						Thread.sleep(1000l);
					}catch(Exception e){
						log.error("睡眠失败" + ExceptionUtils.getFullStackTrace(e));
					}
				}
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
			long lastTime = System.currentTimeMillis();
			while (true){
				sqlResult = receiveConfiguration.getSqlResultMessage().poll();
				if(Objects.nonNull(sqlResult)) {
					sqlResultExcelTemplate.writeData(sqlResult);
					lastTime = System.currentTimeMillis();
				}
				if(System.currentTimeMillis() - lastTime > 10000){
					try{
						Thread.sleep(1000l);
					}catch(Exception e){
						log.error("睡眠失败" + ExceptionUtils.getFullStackTrace(e));
					}
				}
			}
		});
	}
}
