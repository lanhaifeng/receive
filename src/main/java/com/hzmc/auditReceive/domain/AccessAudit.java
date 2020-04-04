package com.hzmc.auditReceive.domain;

import com.google.protobuf.ByteString;
import com.hzmc.auditReceive.annotation.ExcelHeaderProperty;
import com.hzmc.auditReceive.protobuf.ProtoActiveMQ;
import com.hzmc.auditReceive.util.DateUtil;
import com.hzmc.auditReceive.util.SqlIdUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * receive
 * 2020/4/2 22:00
 * 访问审计
 *
 * @author lanhaifeng
 * @since
 **/
@Getter
@Setter
@Log4j
public class AccessAudit implements Serializable {

	private static final long serialVersionUID = -1498844308140840041L;

	@ExcelHeaderProperty
	private String id;
	@ExcelHeaderProperty(headerName = "会话id")
	private String sessid;
	@ExcelHeaderProperty(headerName = "规则名")
	private String ruleName;
	@ExcelHeaderProperty(headerName = "应用用户")
	private String appuser;
	private String euserId;
	@ExcelHeaderProperty(headerName = "企业用户名称")
	private String euser;
	private String sessioninfo;
	private String endIp;
	private Date optime;
	private Long optimeStamp;
	private String cmdtype;
	private String objectOwner;
	private String objectName;
	private String objectType;
	private String sqlId;
	private String sqltext;
	private String bindvalue;
	private String actionLevel;
	private String auditLevel;


	private String txId;
	private long scn;
	private long cscn;
	private String dbuser;

	private String ipAddress;
	private String host;
	private String macAddress;
	private String appname;
	private Date logonTime;
	private Date logoffTime;
	private String dbname;
	private String instanceName;
	private LogonAudit logonAudit;
	private String subRule; //订阅规则
	private String alertLevel; //订阅警告级别
	private String realSql;
	private int row_count;//返回行 新的改成int类型
	private int errCode;//错误码

	private long runTime;//执行时间

	private int dataSrcType;//数据来源

	private String cliIp;//源端ip

	//源端端口
	private int cliPort;

	//目标端ip
	private String svrIp;

	//目标端端口
	private int svrPort;

//	private String dbType;

	private boolean isAccessResult = false;  //标识是不是数据库访问结果

	private boolean sqlParser;

	//三层审计终端信息--2019-01-02 章合全
	private String end_ip;
	private String end_user;
	private String end_app;
	private String end_session_id;

	//上报得到的原始sql
	private String originalSqlText;

	public static AccessAudit from(ProtoActiveMQ.CapaaAccessResult accessResult) {
		AccessAudit auditAccess = new AccessAudit();
		auditAccess.setAccessResult(true);
		auditAccess.setId(toUpperCase(accessResult.getAccessId().toStringUtf8()));
		auditAccess.setRow_count(accessResult.getRowCount());
		auditAccess.setErrCode(accessResult.getErrCode());

		// c++ 那边单位改成了微秒，需要除以1000转化成毫秒，四舍五入。
		long runTime = Math.round(((double)accessResult.getRunTime()) / 1000);

		if(log.isDebugEnabled()) {
			log.debug("access result - ID:" + accessResult.getAccessId().toStringUtf8()
					+ " rowcount:" + accessResult.getRowCount()
					+ " errcode:" + accessResult.getErrCode()
					+ " origin_runtime(us):" + accessResult.getRunTime()
					+ " runtime(ms):" + runTime);
		}
		auditAccess.setRunTime(runTime);
		return auditAccess;
	}

	public static AccessAudit from(ProtoActiveMQ.CapaaAccess access) {
		AccessAudit auditAccess = new AccessAudit();
		auditAccess.setAccessResult(false);
		if (access.getSqlParserType() == ProtoActiveMQ.SqlParserType.SQL_PROTOCOL_PARSER){
			auditAccess.setSqlParser(true);
		}else {
			auditAccess.setSqlParser(false);
		}
		auditAccess.setId(toUpperCase(access.getAccessID().toStringUtf8()));
		auditAccess.setSessid(toUpperCase(access.getSessionID().toStringUtf8()));
		auditAccess.setAppuser(toUpperCase(access.getAppUser().toStringUtf8()));
		auditAccess.setEuserId(toUpperCase(access.getEUserID().toStringUtf8()));
		auditAccess.setEuser(toUpperCase(access.getEUser().toStringUtf8()));
		auditAccess.setSessioninfo(toUpperCase(access.getSessionInfo().toStringUtf8()));
		auditAccess.setEndIp(toUpperCase(access.getEndIP().toStringUtf8()));
		auditAccess.setRuleName(toUpperCase(access.getRuleName().toStringUtf8()));
		if (access.getOpTime() == 0) {
			Date now = new Date();
			auditAccess.setOptimeStamp(now.getTime());
			auditAccess.setOptime(now);
			log.warn("access record don't have op time, using system time. id:" + access.getAccessID()
					+ " cliIp:" + access.getCliIp() + " cliPort:" + access.getCliIp()
					+ " svrIp:" + access.getSvrIp() + " svrPort:" + access.getSvrPort());
		} else{
			auditAccess.setOptimeStamp(access.getOpTime());
			auditAccess.setOptime(DateUtil.unixTime2Date(access.getOpTime()));
		}
		auditAccess.setCmdtype(toUpperCase(access.getStrCmdType().toStringUtf8()));
		auditAccess.setObjectOwner(toUpperCase(handleByteString(access.getSqlParserTypeValue(), access.getObjectOwner())));
		auditAccess.setObjectName(toUpperCase(handleByteString(access.getSqlParserTypeValue(), access.getObjectName())));
		auditAccess.setObjectType(toUpperCase(access.getObjectType().toStringUtf8()));
		auditAccess.setActionLevel(String.valueOf(access.getActionLevel()));
		auditAccess.setDataSrcType(access.getDataSrcValue());
		auditAccess.setCliIp(access.getCliIp().toStringUtf8());
		auditAccess.setSvrIp(access.getSvrIp().toStringUtf8());
		auditAccess.setCliPort(access.getCliPort());
		auditAccess.setSvrPort(access.getSvrPort());

		// 访问审计中增加冗余字段
		auditAccess.setAppname(access.getAppName().toStringUtf8());
		auditAccess.setDbuser(access.getDbUser().toStringUtf8());
		auditAccess.setHost(access.getEndHost().toStringUtf8());

		//通过dbName从缓存中取
		//auditAccess.setDbType(dbType);

		String dbName = access.getCapaaDisplayName().toStringUtf8();
		auditAccess.setDbname(dbName);

		if (access.getAuditLevel() == 0) {
			auditAccess.setAuditLevel("3");
		} else{
			auditAccess.setAuditLevel(String.valueOf(access.getAuditLevel()));
		}
		auditAccess.setTxId(toUpperCase(access.getTxID().toStringUtf8()));
		if (access.getScn() != null && !"".equals(access.getScn().toStringUtf8())) {
			auditAccess.setScn(new Long(access.getScn().toStringUtf8()));
		}
		if (access.getCscn() != null && !"".equals(access.getCscn().toStringUtf8())) {
			auditAccess.setCscn(new Long(access.getCscn().toStringUtf8()));
		}


		//把绑定变量转为json数组
		String bindvalueString = bindValueList2Json(access.getSqlParserTypeValue(), access.getBindValueList());

		auditAccess.setBindvalue(bindvalueString);
		auditAccess.setMacAddress(toUpperCase(access.getSrcMac().toStringUtf8()));
		// 以下处理sqltext
		//将ByteString类型的sql语句处理为原先的String类型的sql语句
		String sqltext = handleByteSQLString(access);						//标准化sql，从protobuf的sqltext中取
		//LOG.info("----receive sqltext： " + sqltext);
		String originalSqlText = handleByteOriginalSqlString(access);	//原始sql，从protobuf的originalSqlText中取
		//LOG.info("----receive originalSqlText： " + originalSqlText);

		if (auditAccess.getRuleName().equalsIgnoreCase("NETWORK")) {// 网络审计

		} else {// 核心和内存审计
			auditAccess.setId(toUpperCase(UUID.randomUUID().toString()));
		}
		//设置sqlid, 用标准化后的sql计算ID
		auditAccess.setSqlId(SqlIdUtils.getSqlId(sqltext.toUpperCase()));
		//塞入标准化后的sql
		auditAccess.setSqltext(sqltext);
		//如果有原始sql，那么直接取原始sql，并设置到访问审计对象中
		if(StringUtils.isNotEmpty(originalSqlText)){
			auditAccess.setOriginalSqlText(originalSqlText);
		}

		//三层审计相关信息--2019-01-02-章合全
		auditAccess.setEnd_app(access.getEndAppName().toStringUtf8());
		auditAccess.setEnd_ip(access.getEndIP().toStringUtf8());
		auditAccess.setEnd_user(access.getAppUser().toStringUtf8());
		auditAccess.setEnd_session_id(access.getAppSessionID().toStringUtf8());


		if(log.isDebugEnabled()) {
			log.debug("id:" + auditAccess.getId() + " cliIp:" + auditAccess.getCliIp() + " cliPort:" + auditAccess.getCliIp()
					+ " dbName" + auditAccess.getDbname()
					+ " svrIp:" + auditAccess.getSvrIp() + " svrPort:" + auditAccess.getSvrPort()
					+ " dbuser:" + auditAccess.dbuser + " appname:" + auditAccess.appname
					+ " host:" + auditAccess.host
					+ " sqlTxt:" + auditAccess.getRealSql()
					+ " originalSqlTxt:" + auditAccess.getOriginalSqlText()
					+ " opTime:" + auditAccess.getOptime());
		}



		return auditAccess;
	}

	private static String toUpperCase(String str) {
		if (str == null) {
			return str;
		}
		return str.toUpperCase();
	}

	private static String handleByteString(int sqlParserType, ByteString byteStr) {
		String str = "";
		switch (sqlParserType) {
			// 如果是协议解析出来的，直接转换成UTF-8返回就可以了
			case ProtoActiveMQ.SqlParserType.SQL_PROTOCOL_PARSER_VALUE:
				str = byteStr.toStringUtf8();
				break;
			//如果是通过正则匹配解析出来的，需要特殊处理
			case ProtoActiveMQ.SqlParserType.SQL_REGEX_VALUE:
				String charset = "UTF-8";

				try {
					str = byteStr.toString(charset);
				} catch (UnsupportedEncodingException e) {
					log.error(e.getMessage(), e);
				}
				break;
			default:
				log.warn("protobuf访问审计未指定SqlParserType:" + sqlParserType);
				str = byteStr.toStringUtf8();
		}

		return str;
	}

	/**
	 *  从protobuf的sqlTextTemplate中取
	 * @param access
	 * @return
	 */
	private static String handleByteSQLString(ProtoActiveMQ.CapaaAccess access) {
		return handleByteString(access.getSqlParserTypeValue(), access.getSqlText()).trim();
	}

	private static String bindValueList2Json(int sqlParserType, List<ProtoActiveMQ.BindValue> bindValueList) {
		String jsonStr = "";
		if(bindValueList != null && bindValueList.size() > 0) {
			JSONArray array = new JSONArray();
			for(ProtoActiveMQ.BindValue bindValue: bindValueList) {
				JSONArray innerArray = new JSONArray();
				for(ByteString item: bindValue.getValueList()) {
					innerArray.add(handleByteString(sqlParserType, item));
				}
				if(innerArray.size() > 0) {
					array.add(innerArray);
				}
			}
			if(array.size() > 0) {
				jsonStr = array.toString();
			}
		}
		return jsonStr;
	}

	/**
	 *  原始sql，从protobuf的sqltext中取
	 * @param access
	 * @return
	 */
	private static String handleByteOriginalSqlString(ProtoActiveMQ.CapaaAccess access) {
		return handleByteString(access.getSqlParserTypeValue(), access.getOriginalSqlText());
	}
}
