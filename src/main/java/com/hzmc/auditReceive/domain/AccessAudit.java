package com.hzmc.auditReceive.domain;

import com.google.protobuf.ByteString;
import com.hzmc.auditReceive.annotation.ExcelHeaderProperty;
import com.hzmc.auditReceive.protobuf.ProtoActiveMQ;
import com.hzmc.auditReceive.util.SqlIdUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.mockito.internal.util.reflection.Fields;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Arrays;
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
public class AccessAudit extends Audit implements Serializable {

	private static final long serialVersionUID = -1498844308140840041L;

	@ExcelHeaderProperty
	private String id;
	@ExcelHeaderProperty(headerName = "登录id")
	private String sessid;
	@ExcelHeaderProperty(headerName = "规则名称")
	private String ruleName;
	@ExcelHeaderProperty(headerName = "企业用户id", isOutput = false)
	private String euserId;
	@ExcelHeaderProperty(headerName = "企业用户名称", isOutput = false)
	private String euser;
	@ExcelHeaderProperty(headerName = "中间件会话信息", isOutput = false)
	private String sessioninfo;
	@ExcelHeaderProperty(headerName = "终端ip")
	private String endIp;
	@ExcelHeaderProperty(headerName = "访问时间")
	private Long optime;
	@ExcelHeaderProperty(headerName = "操作类型")
	private String cmdtype;
	@ExcelHeaderProperty(headerName = "资产对象")
	private String objectOwner;
	@ExcelHeaderProperty(headerName = "资产名")
	private String objectName;
	@ExcelHeaderProperty(headerName = "资产类型")
	private String objectType;
	@ExcelHeaderProperty
	private String sqlId;
	@ExcelHeaderProperty(headerName = "标准化SQL")
	private String sqltext;
	@ExcelHeaderProperty(headerName = "绑定变量")
	private String bindvalue;
	@ExcelHeaderProperty(headerName = "执行结果")
	private String actionLevel;
	@ExcelHeaderProperty(headerName = "审计级别")
	private String auditLevel;

	@ExcelHeaderProperty(headerName = "事务id", isOutput = false)
	private String txId;
	@ExcelHeaderProperty(isOutput = false)
	private Long scn;
	@ExcelHeaderProperty(isOutput = false)
	private Long cscn;
	@ExcelHeaderProperty(headerName = "数据库用户")
	private String dbuser;

	@ExcelHeaderProperty(headerName = "客户端主机名", isOutput = false)
	private String host;
	@ExcelHeaderProperty(headerName = "物理地址", isOutput = false)
	private String macAddress;
	@ExcelHeaderProperty(headerName = "客户端应用名称", isOutput = false)
	private String appname;

	@ExcelHeaderProperty(headerName = "保护对象名")
	private String dbname;
	@ExcelHeaderProperty(headerName = "返回行数")
	private Integer row_count;
	@ExcelHeaderProperty(headerName = "错误码")
	private Integer errCode;
	@ExcelHeaderProperty(headerName = "执行时长")
	private Long runTime;
	@ExcelHeaderProperty(headerName = "数据来源")
	private Integer dataSrcType;


	@ExcelHeaderProperty(headerName = "客户端IP")
	private String cliIp;
	@ExcelHeaderProperty(headerName = "客户端端口")
	private Integer cliPort;
	@ExcelHeaderProperty(headerName = "服务端IP")
	private String svrIp;
	@ExcelHeaderProperty(headerName = "服务端端口")
	private Integer svrPort;

	//@ExcelHeaderProperty(headerName = "数据库类型")
	private String dbType;

	@ExcelHeaderProperty(headerName = "是否访问审计执行结果")
	private Boolean accessResult = false;
	@ExcelHeaderProperty(headerName = "sql语法分析器类型")
	private Integer sqlParserType;

	//三层审计终端信息
	@ExcelHeaderProperty(headerName = "终端IP")
	private String end_ip;
	@ExcelHeaderProperty(headerName = "终端用户")
	private String end_user;
	@ExcelHeaderProperty(headerName = "终端应用")
	private String end_app;
	@ExcelHeaderProperty(headerName = "终端会话id", isOutput = false)
	private String end_session_id;

	//上报得到的原始sql
	@ExcelHeaderProperty(headerName = "原始SQL")
	private String originalSqlText;

	@Override
	public List<String> notFilterColumns() {
		return Arrays.asList(new String[]{"返回行数", "错误码", "执行时长"});
	}

	public static AccessAudit from(ProtoActiveMQ.CapaaAccessResult accessResult) {
		AccessAudit auditAccess = new AccessAudit();
		auditAccess.setAccessResult(true);
		auditAccess.setId(toUpperCase(accessResult.getAccessId().toStringUtf8()));
		auditAccess.setRow_count(accessResult.getRowCount());
		auditAccess.setErrCode(accessResult.getErrCode());

		// c++ 那边单位改成了微秒，需要除以1000转化成毫秒，四舍五入。
		long runTime = Math.round(((double)accessResult.getRunTime()) / 1000);
		auditAccess.setRunTime(runTime);
		return auditAccess;
	}

	public static AccessAudit from(ProtoActiveMQ.CapaaAccess access) {
		AccessAudit auditAccess = new AccessAudit();
		auditAccess.setAccessResult(false);
		auditAccess.setSqlParserType(access.getSqlParserTypeValue());
		auditAccess.setId(toUpperCase(access.getAccessID().toStringUtf8()));
		auditAccess.setSessid(toUpperCase(access.getSessionID().toStringUtf8()));
		auditAccess.setEuserId(toUpperCase(access.getEUserID().toStringUtf8()));
		auditAccess.setEuser(toUpperCase(access.getEUser().toStringUtf8()));
		auditAccess.setSessioninfo(toUpperCase(access.getSessionInfo().toStringUtf8()));
		auditAccess.setEndIp(toUpperCase(access.getEndIP().toStringUtf8()));
		auditAccess.setRuleName(toUpperCase(access.getRuleName().toStringUtf8()));
		if (access.getOpTime() == 0) {
			auditAccess.setOptime(System.currentTimeMillis());
		} else{
			auditAccess.setOptime(access.getOpTime() / 1000);
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
