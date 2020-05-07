package com.hzmc.auditReceive.config;

import com.hzmc.auditReceive.constant.SubscribeMode;
import com.hzmc.auditReceive.domain.*;
import com.hzmc.auditReceive.protobuf.ProtoActiveMQ;
import com.hzmc.auditReceive.protobuf.ProtoActiveMQUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

	@Value("${message.filters:NONE}")
	private String filters;

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
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(usrName, password, brokerUrl);
		activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy());
		return activeMQConnectionFactory;
	}

	@Bean
	public RedeliveryPolicy redeliveryPolicy(){
		RedeliveryPolicy  redeliveryPolicy=   new RedeliveryPolicy();
		//是否在每次尝试重新发送失败后,增长这个等待时间
		redeliveryPolicy.setUseExponentialBackOff(true);
		//重发次数,默认为6次   这里设置为10次
		redeliveryPolicy.setMaximumRedeliveries(10);
		//重发时间间隔,默认为1秒
		redeliveryPolicy.setInitialRedeliveryDelay(1);
		//第一次失败后重新发送之前等待500毫秒,第二次失败再等待500 * 2毫秒,这里的2就是value
		redeliveryPolicy.setBackOffMultiplier(2);
		//是否避免消息碰撞
		redeliveryPolicy.setUseCollisionAvoidance(false);
		//设置重发最大拖延时间-1 表示没有拖延只有UseExponentialBackOff(true)为true时生效
		redeliveryPolicy.setMaximumRedeliveryDelay(-1);
		return redeliveryPolicy;
	}

	@PostConstruct
	public void initActivemqConsumer() throws JMSException {
		ActiveMQConnectionFactory connectionFactory = connectionFactory();
		Connection conn = connectionFactory.createConnection();
		conn.start();
		Session session = conn.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
		List<DataFilter> dataFilters = new ArrayList<>();
		if(StringUtils.isNotBlank(filters) && !"NONE".equals(filters)){
			String all_filters[] = filters.split(",");
			for (String filterStr : all_filters) {
				String[] filter = filterStr.split(" ");
				String[] need_filter = new String[4];
				int i = 0;
				for (String s : filter) {
					if(StringUtils.isNotBlank(s)){
						need_filter[i] = s;
						i++;
					}
					if(i == 4){
						dataFilters.add(new DataFilter(need_filter[0], need_filter[1], need_filter[2], need_filter[3]));
					}
				}
			}
		}
		subscribeAccessAudit(session, dataFilters);
		subscribeLogonAudit(session, dataFilters);
		subscribeSqlResult(session);
	}

	private boolean filter(List<DataFilter> filters, Audit audit){
		boolean result = true;
		if(filters == null || filters.isEmpty()) return result;
		for (DataFilter filter : filters) {
			result = filter.filter(audit);
			if(!result){
				break;
			}
		}
		return result;
	}

	private void subscribeLogonAudit(Session session, List<DataFilter> filters) {
		for (int i = 1; i <= logonNum; i++) {
			String subscribeName = logonName + i;
			taskExecutor.execute(() -> {
				try {
					MessageConsumer messConsumer = session.createConsumer(getDestination(session, subscribeName));
					messConsumer.setMessageListener((message) -> {
								List<LogonAudit> logonAudits = ProtoActiveMQUtils.parseData(ProtoActiveMQ.CapaaLogOn.class, message);
								receiveConfiguration.getLogonMessage().addAll(logonAudits.stream().filter(logonAudit -> filter(filters, logonAudit)).collect(Collectors.toList()));
							}
					);
				} catch (Exception e) {
					log.error("订阅访问失败：" + ExceptionUtils.getFullStackTrace(e));
				}
			});
		}
	}

	private void subscribeAccessAudit(Session session, List<DataFilter> filters) {
		for (int i = 1; i <= accessNum; i++) {
			String subscribeName = accessName + i;
			taskExecutor.execute(() -> {
				try {
					MessageConsumer messConsumer = session.createConsumer(getDestination(session, subscribeName));
					messConsumer.setMessageListener((message) -> {
								List<AccessAudit> accessAudits = ProtoActiveMQUtils.parseData(ProtoActiveMQ.CapaaAccess.class, message);
								receiveConfiguration.getAccessMessage().addAll(accessAudits.stream().filter(accessAudit -> filter(filters, accessAudit)).collect(Collectors.toList()));
							}
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
