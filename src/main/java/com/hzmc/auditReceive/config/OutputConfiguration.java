package com.hzmc.auditReceive.config;

import com.hzmc.auditReceive.domain.AccessAudit;
import com.hzmc.auditReceive.domain.LogonAudit;
import com.hzmc.auditReceive.io.ExcelTemplate;
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
		ExcelTemplate logonExcelTemplate = new ExcelTemplate(outputPath, LogonAudit.class);
		taskExecutor.execute(() -> {
			LogonAudit logonAudit;
			while (true){
				logonAudit = receiveConfiguration.getLogonMessage().poll();
				Optional.ofNullable(logonAudit).ifPresent(logonExcelTemplate::writeData);
			}
		});

		ExcelTemplate accessExcelTemplate = new ExcelTemplate(outputPath, AccessAudit.class);
		taskExecutor.execute(() -> {
			AccessAudit accessAudit;
			while (true){
				accessAudit = receiveConfiguration.getAccessMessage().poll();
				Optional.ofNullable(accessAudit).ifPresent(accessExcelTemplate::writeData);
			}
		});
	}
}
