package com.hzmc.auditReceive.config;

import com.hzmc.auditReceive.domain.AccessAudit;
import com.hzmc.auditReceive.domain.LogonAudit;
import com.hzmc.auditReceive.domain.SQLResult;
import com.hzmc.auditReceive.protobuf.ProtoActiveMQ;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * receive
 * 2020/4/2 15:13
 * 配置类
 *
 * @author lanhaifeng
 * @since
 **/
@Configuration
@Setter
@Getter
@EnableAsync
public class ReceiveConfiguration implements InitializingBean {
	@Value("${message.cache.receive-pool}")
	private int receivePool;

	private LinkedBlockingQueue<LogonAudit> logonMessage;
	private LinkedBlockingQueue<AccessAudit> accessMessage;
	private LinkedBlockingQueue<SQLResult> sqlResultMessage;

	@Bean("taskExecutor")
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// 设置核心线程数
		executor.setCorePoolSize(5);
		// 设置最大线程数
		executor.setMaxPoolSize(10);
		// 设置队列容量
		executor.setQueueCapacity(20);
		// 设置线程活跃时间（秒）
		executor.setKeepAliveSeconds(60);
		// 设置默认线程名称
		executor.setThreadNamePrefix("receive-");
		// 设置拒绝策略
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		// 等待所有任务结束后再关闭线程池
		executor.setWaitForTasksToCompleteOnShutdown(true);
		return executor;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		logonMessage = new LinkedBlockingQueue<>(receivePool);
		accessMessage = new LinkedBlockingQueue<>(receivePool);
		sqlResultMessage = new LinkedBlockingQueue<>(receivePool);
	}
}
