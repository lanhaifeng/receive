package com.hzmc.auditReceive.domain;

import com.hzmc.auditReceive.annotation.ExcelHeaderProperty;
import com.hzmc.auditReceive.protobuf.ProtoActiveMQ;
import com.hzmc.auditReceive.util.DateUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.io.Serializable;
import java.util.Date;

/**
 * receive
 * 2020/4/2 22:01
 * 登录审计
 *
 * @author lanhaifeng
 * @since
 **/
@Getter
@Setter
@Log4j
public class LogonAudit implements Serializable {


	private static final long serialVersionUID = 1134381738230624145L;
	@ExcelHeaderProperty
	private String id;
	private String certsessionId;
	@ExcelHeaderProperty(headerName = "规则名")
	private String ruleName;
	private String euserId;
	private String euser;
	private String dbuser;

	private String ipAddress;
	private String host;
	private String macAddress;
	private String endIp;
	private String osUser;
	private String appname;
	private String endApp;
	private Date logonTime;
	private Date logoffTime;
	private String cmdtype;
	private String what;
	private String serverhost;
	private String dbname;
	private String instanceName;
	private String actionLevel;
	private String auditLevel;
	private String sid;
	private String serial;
	private String audsid;
	//服务端ip
	private String svrIp;
	private int svrPort;
	private String dbType;
	private int cliPort;


	public static LogonAudit from(ProtoActiveMQ.CapaaLogOff logOff) {
		LogonAudit auditSession = new LogonAudit();
		auditSession.setId(toUpperCase(logOff.getSessionId().toStringUtf8()));
		auditSession.setLogoffTime(DateUtil.unixTime2Date(logOff.getLogOffTime()));

		return auditSession;
	}

	public static LogonAudit from(ProtoActiveMQ.CapaaLogOn logOn) {
		LogonAudit auditSession = new LogonAudit();
		auditSession.setId(toUpperCase(logOn.getSessionId().toStringUtf8()));
		if (logOn.getCertSessionId() == null || "".equals(logOn.getCertSessionId().toStringUtf8()))
			auditSession.setCertsessionId("0");
		else
			auditSession.setCertsessionId(toUpperCase(logOn.getCertSessionId().toStringUtf8()));
		auditSession.setRuleName(toUpperCase(logOn.getRuleName().toStringUtf8()));
		auditSession.setEuserId(toUpperCase(logOn.getEUserId().toStringUtf8()));
		auditSession.setEuser(toUpperCase(logOn.getEUser().toStringUtf8()));
		auditSession.setDbuser(toUpperCase(logOn.getDbUser().toStringUtf8()));
		auditSession.setIpAddress(toUpperCase(logOn.getIpAddr().toStringUtf8()));
		auditSession.setHost(toUpperCase(logOn.getEndHost().toStringUtf8()));
		auditSession.setMacAddress(toUpperCase(logOn.getEndMac().toStringUtf8()));
		if(logOn.getEndMac().isEmpty()&&!logOn.getHostMac().isEmpty()){
			auditSession.setMacAddress(toUpperCase(logOn.getHostMac().toStringUtf8()));
		}
		auditSession.setEndIp(toUpperCase(logOn.getEndIP().toStringUtf8()));
		auditSession.setOsUser(toUpperCase(logOn.getOsUser().toStringUtf8()));
		auditSession.setAppname(toUpperCase(logOn.getAppName().toStringUtf8()));
		auditSession.setEndApp(toUpperCase(logOn.getEndApp().toStringUtf8()));
		if (logOn.getLogonTime() == 0) {
			auditSession.setLogonTime((new Date()));
			log.warn("protobuf登录审计解析无登录时间，已用系统时间替代");
			log.warn(logOn);
		} else {
			auditSession.setLogonTime(DateUtil.unixTime2Date(logOn.getLogonTime()));
		}
		auditSession.setCmdtype(toUpperCase(logOn.getStrCmdType().toStringUtf8()));
		auditSession.setWhat(toUpperCase(logOn.getWhat().toStringUtf8()));
		auditSession.setServerhost(toUpperCase(logOn.getServerHost().toStringUtf8()));
		auditSession.setDbname(toUpperCase(logOn.getCapaaDisplayName().toStringUtf8()));
		auditSession.setInstanceName(toUpperCase(logOn.getInstanceName().toStringUtf8()));
		if (logOn.getActionLevel() == 0)
			auditSession.setActionLevel("0");
		else
			auditSession.setActionLevel(String.valueOf(logOn.getActionLevel()));
		if (logOn.getAuditLevel() == 0)
			auditSession.setAuditLevel("3");
		else
			auditSession.setAuditLevel(String.valueOf(logOn.getAuditLevel()));
		auditSession.setSid(toUpperCase(logOn.getSid().toStringUtf8()));
		auditSession.setSerial(toUpperCase(logOn.getSerial().toStringUtf8()));
		auditSession.setAudsid(toUpperCase(logOn.getAudSid().toStringUtf8()));
		auditSession.setSvrIp(toUpperCase(logOn.getSvrIp().toStringUtf8()));
		auditSession.setSvrPort(logOn.getSvrPort());

		//通过dbName从缓存中取
//		auditSession.setDbType(dbType);
		auditSession.setCliPort(logOn.getCliPort());
		String macAddress = auditSession.getMacAddress();
		if(macAddress != null){
			auditSession.setMacAddress(macAddress.replaceAll(":","-"));
		}
		if(log.isDebugEnabled()) {
			log.debug("logon audit - id:" + auditSession.getId()
					+ " cliIp:" + auditSession.getIpAddress()
					+ " svrIp:" + auditSession.getSvrIp()
					+ " dbuser:" + auditSession.getDbuser()
					+ " appname:" + auditSession.getAppname()
					+ " host:" + auditSession.getHost()
			);
		}


		return auditSession;
	}

	private static String toUpperCase(String str) {
		if (str == null) {
			return str;
		}
		return str.toUpperCase();
	}
}
