package com.hzmc.auditReceive.domain;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.hzmc.auditReceive.annotation.ExcelHeaderProperty;
import com.hzmc.auditReceive.protobuf.ProtoActiveMQ;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Log4j
public class SQLResult implements Serializable{

	@ExcelHeaderProperty
    private long id;
	@ExcelHeaderProperty(headerName = "访问id")
    private String accessId;
	@ExcelHeaderProperty(headerName = "字段描述")
    private String fields;
	@ExcelHeaderProperty(headerName = "行数据")
    private String rowData;
	@ExcelHeaderProperty(headerName = "最后修改时间", isOutput = false)
    private long lastModifyTime;

    public static List<SQLResult> from(ProtoActiveMQ.DBResultset rs) {
        List<SQLResult> list = new ArrayList<>();
        if(rs.getAccessId() == null) {
            if(log.isDebugEnabled()) {
				log.debug("DBResultset.accessId is null.");
            }
            return list;
        }
        String accessId = rs.getAccessId().toStringUtf8();
        List<String> fields = new ArrayList<>();
        List<ByteString> rsFields = rs.getFieldsList();
        for(ByteString field: rsFields) {
            fields.add(field.toStringUtf8());
        }
        String fieldsJson = JSON.toJSONString(fields);

        List<ProtoActiveMQ.RowData> rsRows = rs.getRsList();
        for(ProtoActiveMQ.RowData rowData: rsRows) {
            SQLResult result = new SQLResult();
            result.setAccessId(accessId);

            result.setFields(fieldsJson);
            List<String> row = new ArrayList<>();
            List<ByteString> rowFieldVals = rowData.getDataList();

            for(ByteString fieldVal: rowFieldVals) {
                row.add(fieldVal.toStringUtf8());
            }
            result.setRowData(JSON.toJSONString(row));
            result.setLastModifyTime(System.currentTimeMillis());

            list.add(result);
        }

        return list;
    }
}
