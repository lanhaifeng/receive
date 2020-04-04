package com.hzmc.auditReceive.protobuf;

import com.google.protobuf.CodedInputStream;
import com.hzmc.auditReceive.domain.AccessAudit;
import com.hzmc.auditReceive.domain.LogonAudit;
import com.hzmc.auditReceive.protobuf.ProtoActiveMQ.*;
import lombok.extern.log4j.Log4j;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j
public class ProtoActiveMQUtils {

	public static List parseData(Class clazz, Message message) {
		List list = new ArrayList();
		byte[] data = null;
		try {
			if (message instanceof ActiveMQBytesMessage) {
				ActiveMQBytesMessage bytesMessage = (ActiveMQBytesMessage) message;
				data = new byte[(int) bytesMessage.getBodyLength()];
				bytesMessage.readBytes(data);
			} else{
				log.error("protobuf解析 传入了不对的message类型");
				return list;
			}
		} catch (JMSException e) {
			log.error("JMSException error in ProtoActiveMQUtils protobuf"+e.getMessage(),e);
		}
		if(data !=null){
			list = parseDataDetail(clazz,data);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	private static List parseDataDetail(Class clazz, byte[] data) {
		List list = new ArrayList<>();
		int headLength = 7;
		for (int i = 0; i < data.length; ) {
			// 先解析前面的头，目前是7字节。
			BaseMessage baseMessage;
			try {
				CodedInputStream head = CodedInputStream.newInstance(data, i, headLength);
				baseMessage = BaseMessage.parseFrom(head);
			} catch (Exception e) {
				log.error("protobuf head 解析出错,将舍弃后续信息 " + clazz.getName() + e.getMessage(), e);
				if(log.isInfoEnabled()) {
					log.info("protobuf原始信息是");
					log.info(new String(Base64.encodeBase64(data)));
				}
				break;
			}
			try {
				CodedInputStream body = CodedInputStream.newInstance(data, i, baseMessage.getCmdLen());
				Object obj = null;
				switch (baseMessage.getCmdTypeValue()) {
					case MsgCmdType.CAPAALogOn_VALUE:
						CapaaLogOn logOn = CapaaLogOn.parseFrom(body);
						if(log.isDebugEnabled())
							log.debug(logOn);
						obj = LogonAudit.from(logOn);

						break;
					case MsgCmdType.CAPAALogOff_VALUE:
						CapaaLogOff logOff = CapaaLogOff.parseFrom(body);
						if(log.isDebugEnabled())
							log.debug(logOff);
						obj = LogonAudit.from(logOff);
						break;
					case MsgCmdType.CAPAAAccess_VALUE:
						CapaaAccess access = CapaaAccess.parseFrom(body);
						if(log.isDebugEnabled())
							log.debug(access);
						obj = AccessAudit.from(access);

                        //2018.01.08 排除sql语句为空白的审计
                        if (StringUtils.isBlank(access.getSqlText().toString().trim())) {
                            obj = null;
                            log.warn("protobuf 解析出现空语句");
                            if(log.isInfoEnabled()) {
                                log.info(access);
                                log.info("protobuf 当前信息是");
                                log.info(new String(Base64.encodeBase64(Arrays.copyOfRange(data, i, i + baseMessage.getCmdLen()))));
                            }
                        }

                        //排除审计id为空的情况
                        if(access.getAccessID() == null || access.getAccessID().equals("")){
                        	log.warn("access id 为空：" + access);
                        	obj = null;
						}
						break;
					case MsgCmdType.CAPAAAccessResult_VALUE:
						CapaaAccessResult accessResult = CapaaAccessResult.parseFrom(body);
						if(log.isDebugEnabled())
							log.debug(accessResult);
						obj = AccessAudit.from(accessResult);
						break;
					case MsgCmdType.CAPAAFlowInfo_VALUE:
						obj = CapaaFlowInfo.parseFrom(body);
						if(log.isDebugEnabled())
							log.debug(obj);
						break;
				    case MsgCmdType.CAPAADBStat_VALUE:
						obj = DBStats.parseFrom(body);
						if(log.isDebugEnabled())
							log.debug(obj);
						break;
					case MsgCmdType.CAPAADBResultset_VALUE:
						obj = DBResultset.parseFrom(body);
						break;
					default:
						log.error("protobuf body 解析出现未匹配的类型：" + baseMessage.getCmdTypeValue() + clazz.getName());
						if(log.isInfoEnabled()) {
							log.info("protobuf 当前信息是");
							log.info(new String(Base64.encodeBase64(Arrays.copyOfRange(data, i, i + baseMessage.getCmdLen()))));
						}
						break;
				}
				if (obj != null)
					list.add(obj);
			} catch (Exception e) {
				log.error("protobuf body 解析出错, 将继续解析后续信息 " + clazz.getName() + e.getMessage(), e);
				if(log.isInfoEnabled()) {
					log.info("protobuf 当前信息是");
					log.info(new String(Base64.encodeBase64(Arrays.copyOfRange(data, i, i + baseMessage.getCmdLen()))));
				}
			} finally {
				//继续解析下一条信息
				i += baseMessage.getCmdLen();
			}
		}
		return list;
	}

	public static void main(String[] args) throws IOException {
		byte[] data = new byte[]{13, -31, 0, 0, 0, 16, 3};
		int i = 0,headLength=7;
		CodedInputStream head = CodedInputStream.newInstance(data, i, headLength);
		BaseMessage baseMessage = BaseMessage.parseFrom(head);
		System.out.println(baseMessage.getCmdLen());
		System.out.println(baseMessage.getCmdType().getNumber());
	}

}
