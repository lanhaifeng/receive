package com.hzmc.auditReceive.config;

import com.hzmc.auditReceive.constant.SubscribeMode;
import com.hzmc.auditReceive.domain.SQLResult;
import com.hzmc.auditReceive.protobuf.ProtoActiveMQ;
import com.hzmc.auditReceive.protobuf.ProtoActiveMQUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.jms.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * receive
 * 2020/4/2 18:24
 * activemq配置类
 *
 * @author lanhaifeng
 * @since
 **/
@Configuration
@Setter
@Getter
@ConditionalOnProperty(name = "message.type", havingValue = "activemq")
@Log4j
public class ActiveMqConfiguration implements InitializingBean {

	@Value("${spring.activemq.user}")
	private String usrName;

	@Value("${spring.activemq.password}")
	private String password;

	@Value("${spring.activemq.broker-url}")
	private String brokerUrl;

	@Value("${message.subscribe.mode}")
	private String subscribeMode;

	@Value("${message.subscribe.access-name}")
	private String accessName;

	@Value("${message.subscribe.access-num}")
	private Integer accessNum;

	@Value("${message.subscribe.logon-name}")
	private String logonName;

	@Value("${message.subscribe.logon-num}")
	private Integer logonNum;

	@Value("${message.subscribe.sql-result-name}")
	private String sqlResultName;

	@Value("${message.subscribe.sql-result-num}")
	private Integer sqlResultNum;

	@Autowired
	private ReceiveConfiguration receiveConfiguration;
	@Autowired
	@Qualifier("taskExecutor")
	private TaskExecutor taskExecutor;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.state(StringUtils.isNotBlank(usrName), "activemq userName is Null!");
		Assert.state(StringUtils.isNotBlank(password), "activemq password is Null!");
		Assert.state(StringUtils.isNotBlank(brokerUrl), "activemq brokerUrl is Null!");
		Assert.state(StringUtils.isNotBlank(subscribeMode), "activemq subscribe mode is Null!");
		Assert.state(StringUtils.isNotBlank(accessName), "activemq access audit subscribe name is Null!");
		Assert.state(accessNum > 0, String.format("accessNum:%s is illegal!", accessNum));
		Assert.state(StringUtils.isNotBlank(logonName), "activemq logon audit subscribe name is Null!");
		Assert.state(logonNum > 0, String.format("logonNum:%s is illegal!", logonNum));
		Assert.notNull(receiveConfiguration, "receiveConfiguration is Null!");
		Assert.notNull(taskExecutor, "taskExecutor is Null!");
	}

	@Bean
	public ActiveMQConnectionFactory connectionFactory() {
		return new ActiveMQConnectionFactory(usrName, password, brokerUrl);
	}

	@PostConstruct
	public void initActivemqConsumer() throws JMSException {
		ActiveMQConnectionFactory connectionFactory = connectionFactory();
		Connection conn = connectionFactory.createConnection();
		conn.start();
		Session session = conn.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
		subscribeAccessAudit(session);
		subscribeLogonAudit(session);
		subscribeSqlResult(session);
	}

	private void subscribeLogonAudit(Session session) {
		for (int i = 1; i <= logonNum; i++) {
			String subscribeName = logonName + i;
			taskExecutor.execute(() -> {
				try {
					MessageConsumer messConsumer = session.createConsumer(getDestination(session, subscribeName));
					messConsumer.setMessageListener((message) ->
							receiveConfiguration.getLogonMessage().addAll(ProtoActiveMQUtils.parseData(ProtoActiveMQ.CapaaLogOn.class, message))
					);
				} catch (Exception e) {
					log.error("订阅访问失败：" + ExceptionUtils.getFullStackTrace(e));
				}
			});
		}
	}

	private void subscribeAccessAudit(Session session) {
		for (int i = 1; i <= accessNum; i++) {
			String subscribeName = accessName + i;
			taskExecutor.execute(() -> {
				try {
					MessageConsumer messConsumer = session.createConsumer(getDestination(session, subscribeName));
					messConsumer.setMessageListener((message) ->
							receiveConfiguration.getAccessMessage().addAll(ProtoActiveMQUtils.parseData(ProtoActiveMQ.CapaaAccess.class, message))
					);
				} catch (Exception e) {
					log.error("订阅访问失败：" + ExceptionUtils.getFullStackTrace(e));
				}
			});
		}
	}

	private void subscribeSqlResult(Session session) {
		for (int i = 1; i <= sqlResultNum; i++) {
			String subscribeName = sqlResultName + i;
			taskExecutor.execute(() -> {
				try {
					MessageConsumer messConsumer = session.createConsumer(getDestination(session, subscribeName));
					messConsumer.setMessageListener((message) -> {
						List sqlResults = ProtoActiveMQUtils.parseData(ProtoActiveMQ.DBResultset.class, message);
						Optional.ofNullable(sqlResults).ifPresent(datas ->
								datas.forEach(data ->
										Optional.ofNullable(data).ifPresent(
												sqlResult -> receiveConfiguration.getSqlResultMessage().addAll(
														SQLResult.from((ProtoActiveMQ.DBResultset) sqlResult)))));
							}
					);
				} catch (Exception e) {
					log.error("订阅访问失败：" + ExceptionUtils.getFullStackTrace(e));
				}
			});
		}
	}

	private Destination getDestination(Session session, String subscribeName) throws JMSException {
		Destination destination = null;
		switch (SubscribeMode.valueOf(subscribeMode.toUpperCase())){
			case TOPIC:
				destination = session.createTopic(subscribeName);
				break;
			case QUEUE:
				destination = session.createQueue(subscribeName);
				break;
		}

		return destination;
	}

}
