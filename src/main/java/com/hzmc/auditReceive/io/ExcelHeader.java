package com.hzmc.auditReceive.io;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * receive
 * 2020/4/3 16:25
 * excel表头信息
 *
 * @author lanhaifeng
 * @since
 **/
@Setter
@Getter
@RequiredArgsConstructor
public class ExcelHeader implements Serializable {
	private static final long serialVersionUID = 8183152623739074767L;

	//头名
	@NonNull
	private String headerName;
	//头取值方法名
	@NonNull
	private String headerMethodName;
	//排序
	@NonNull
	private int order;
}
