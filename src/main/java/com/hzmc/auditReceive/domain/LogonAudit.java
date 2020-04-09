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
	@ExcelHeaderProperty(headerName = "证书会话id", isOutput = false)
	private String certsessionId;
	@ExcelHeaderProperty(headerName = "规则名称")
	private String ruleName;
	@ExcelHeaderProperty(headerName = "企业用户ID", isOutput = false)
	private String euserId;
	@ExcelHeaderProperty(headerName = "企业用户名称", isOutput = false)
	private String euser;
	@ExcelHeaderProperty(headerName = "数据库用户")
	private String dbuser;
	@ExcelHeaderProperty(headerName = "客户端IP")
	private String ipAddress;
	@ExcelHeaderProperty(headerName = "主机名")
	private String host;
	@ExcelHeaderProperty(headerName = "物理地址1")
	private String endMac;
	@ExcelHeaderProperty(headerName = "物理地址2")
	private String hostMac;
	@ExcelHeaderProperty(isOutput = false)
	private String endIp;
	@ExcelHeaderProperty(headerName = "操作系统用户")
	private String osUser;
	@ExcelHeaderProperty(headerName = "应用程序")
	private String appname;
	@ExcelHeaderProperty(isOutput = false)
	private String endApp;
	@ExcelHeaderProperty(headerName = "登录时间")
	private Long logonTime;
	@ExcelHeaderProperty(headerName = "退出时间")
	private Long logoffTime;
	@ExcelHeaderProperty(headerName = "操作类型", isOutput = false)
	private String cmdtype;
	@ExcelHeaderProperty(isOutput = false)
	private String what;
	@ExcelHeaderProperty(headerName = "数据库主机名")
	private String serverhost;
	@ExcelHeaderProperty(headerName = "保护对象名")
	private String dbname;
	@ExcelHeaderProperty(headerName = "数据库实例")
	private String instanceName;
	@ExcelHeaderProperty(headerName = "执行结果")
	private String actionLevel;
	@ExcelHeaderProperty(headerName = "审计级别")
	private String auditLevel;
	@ExcelHeaderProperty(isOutput = false)
	private String sid;
	@ExcelHeaderProperty(isOutput = false)
	private String serial;
	@ExcelHeaderProperty(isOutput = false)
	private String audsid;
	//服务端ip
	@ExcelHeaderProperty(headerName = "服务端IP")
	private String svrIp;
	@ExcelHeaderProperty(headerName = "服务端端口")
	private Integer svrPort;
//	private String dbType;
	@ExcelHeaderProperty(headerName = "客户端端口")
	private Integer cliPort;
	@ExcelHeaderProperty(headerName = "是否登录结果")
	private Boolean logonResult = false;

	public static LogonAudit from(ProtoActiveMQ.CapaaLogOff logOff) {
		LogonAudit auditSession = new LogonAudit();
		auditSession.setLogonResult(true);
		auditSession.setId(toUpperCase(logOff.getSessionId().toStringUtf8()));
		auditSession.setLogoffTime(logOff.getLogOffTime() / 1000);

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
		auditSession.setEndMac(toUpperCase(logOn.getEndMac().toStringUtf8()));
		auditSession.setHostMac(toUpperCase(logOn.getHostMac().toStringUtf8()));
		auditSession.setEndIp(toUpperCase(logOn.getEndIP().toStringUtf8()));
		auditSession.setOsUser(toUpperCase(logOn.getOsUser().toStringUtf8()));
		auditSession.setAppname(toUpperCase(logOn.getAppName().toStringUtf8()));
		auditSession.setEndApp(toUpperCase(logOn.getEndApp().toStringUtf8()));
		if (logOn.getLogonTime() == 0) {
			auditSession.setLogonTime(System.currentTimeMillis());
			log.warn("protobuf登录审计解析无登录时间，已用系统时间替代");
			log.warn(logOn);
		} else {
			auditSession.setLogonTime(logOn.getLogonTime() / 1000);
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
		String endAddress = auditSession.getEndMac();
		if (endAddress != null) {
			auditSession.setEndMac(endAddress.replaceAll(":", "-"));
		}

		String hostAddress = auditSession.getHostMac();
		if (endAddress != null) {
			auditSession.setHostMac(hostAddress.replaceAll(":", "-"));
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
