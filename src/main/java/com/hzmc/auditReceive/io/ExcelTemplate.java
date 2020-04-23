package com.hzmc.auditReceive.io;

import com.hzmc.auditReceive.annotation.ExcelHeaderProperty;
import com.hzmc.auditReceive.exception.TemplateException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * receive
 * 2020/4/3 10:48
 * excel模板类
 *
 * @author lanhaifeng
 * @since
 **/
@Getter
@Setter
@RequiredArgsConstructor
@Log4j
public class ExcelTemplate implements Serializable {

	private static final long serialVersionUID = -8947617886162926937L;

	//间隔flush时间，毫秒
	private static final long FLUSH_TIME_STAMP = 1000;
	//间隔flush次数
	private static final long FLUSH_TIMES = 1000;
	//每个sheet最大数据条数
	private static final int SHEET_MAX_SIZE = 10000;
	/** date format : yyyy-MM-dd_HH:mm:ss.S*/
	private static DateFormat DATEFORMAT_MILLISECOND = new SimpleDateFormat("yyyy-MM-dd_HHmmss.S");

	//输出路径
	private String outputPath;
	//文件名
	private String fileName;
	//创建时间
	private long createTime;
	//上次刷新数据时间
	private long lastFlushTime;
	//未刷新数量
	private long lastFlushTimes;
	//头
	private List<ExcelHeader> headers;
	//当前页，数据索引，从0开始
	private int rowIndex;
	//总共的数据条数
	private int totalNum;
	//每页最大数量
	private int maxPageSize = SHEET_MAX_SIZE;
	//excel对象
	private HSSFWorkbook hssfWorkbook;
	//当前标签页
	private HSSFSheet currentSheet;
	//是否分页true分页，false不分页
	private Boolean page;
	//实体类定义对象
	private Class  cls;

	public ExcelTemplate(String outputPath, Class  cls) throws IOException {
		this.outputPath = outputPath;
		this.cls = cls;
		init();
	}

	private void init() throws IOException {
		buildHSSFWorkbook();
		validate();
	}

	private void buildHSSFWorkbook() throws IOException {
		page = false;
		buildFileName();
		buildHeader();
		File output = new File(outputPath);
		if(!output.exists()){
			output.mkdirs();
		}
		hssfWorkbook = new HSSFWorkbook();
		buildCurrentSheet();
	}

	public void flush(boolean force) {
		try {
			long now = System.currentTimeMillis();
			if (force || now - lastFlushTime >= FLUSH_TIME_STAMP || lastFlushTimes >= FLUSH_TIMES) {
				FileOutputStream fo = new FileOutputStream(outputPath + fileName);
				hssfWorkbook.write(fo);
				lastFlushTime = now;
				lastFlushTimes = 0;
			}
		} catch (Exception e) {
			log.error("刷新数据失败，错误：" + ExceptionUtils.getFullStackTrace(e));
		}
	}

	private void validate(){
		Assert.state(StringUtils.isNotBlank(outputPath), "输出文件路径不能为空");
		Assert.state(StringUtils.isNotBlank(fileName), "文件名不能为空");
		Assert.notNull(cls, "实体类描述对象不能为空");
		Assert.notNull(hssfWorkbook, "excel文件对象不能为空");
		Assert.notNull(currentSheet, "excel当前页对象不能为空");
		Assert.state(Objects.nonNull(headers) && !headers.isEmpty(), "模板头不能为空");
		Assert.state(createTime > 0 , "创建时间非法");
		Assert.state(rowIndex >= 0, "当前标签页索引非法");
	}

	private void buildFileName(){
		createTime = System.currentTimeMillis();
		lastFlushTime = createTime;
		fileName = cls.getSimpleName() + "_" + DATEFORMAT_MILLISECOND.format(createTime) + ".xls";
	}

	private void buildHeader(){
		if(Objects.isNull(headers)){
			headers = new ArrayList<>();
		}
		ExcelHeaderProperty clsExcelHeader = (ExcelHeaderProperty)cls.getAnnotation(ExcelHeaderProperty.class);
		Field[] fields = cls.getDeclaredFields();
		Stream.of(fields).forEach(field -> {
			ExcelHeaderProperty fieldExcelHeader = field.getAnnotation(ExcelHeaderProperty.class);
			String fieldName = field.getName();
			if(!Objects.isNull(fieldExcelHeader) && fieldExcelHeader.isOutput()){
				String headName = StringUtils.isNotBlank(fieldExcelHeader.headerName()) ? fieldExcelHeader.headerName() : fieldName;
				String valueMethodName = StringUtils.isNotBlank(fieldExcelHeader.valueMethodName()) ? fieldExcelHeader.valueMethodName() :
						"get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
				int order = fieldExcelHeader.order();
				headers.add(new ExcelHeader(headName, valueMethodName, order));
			}else {
				if(!Objects.isNull(clsExcelHeader) && clsExcelHeader.isOutput()){
					String headName = StringUtils.isNotBlank(clsExcelHeader.headerName()) ? clsExcelHeader.headerName() : fieldName;
					String valueMethodName = StringUtils.isNotBlank(clsExcelHeader.valueMethodName()) ? clsExcelHeader.valueMethodName() :
							"get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
					int order = clsExcelHeader.order();
					headers.add(new ExcelHeader(headName, valueMethodName, order));
				}
			}
		});
	}

	private void buildCurrentSheet(){
		BiPredicate<Integer, Integer> predicate = (rowNum, maxPageSize) -> rowNum >= maxPageSize;
		if(Objects.isNull(currentSheet) || (page && predicate.test(rowIndex, maxPageSize))){
			flush(true);
			currentSheet = hssfWorkbook.createSheet();
			rowIndex = 0;
			writeHeader();
		}
	}

	public void writeData(Object audit){
		Class cls = audit.getClass();
		Function<String,TemplateException> templateExceptionFunction = TemplateException::new;
		if(cls != this.cls) throw templateExceptionFunction.apply(String.format("写入的数据与模板不匹配，数据类型：%s,模板类型：%s", cls, this.cls));
		Row row = currentSheet.createRow(rowIndex);
		IntStream.range(0, headers.size()).forEach(index->{
			try {
				Method method = cls.getMethod(headers.get(index).getHeaderMethodName());
				method.invoke(audit, null);
				// 创建一个单元格
				Cell cell = row.createCell(index);
				// 将数据转换为字符串
				Object propertyValue = method.invoke(audit, null);
				HSSFRichTextString text = new HSSFRichTextString(Optional.ofNullable(propertyValue).map(Object::toString).orElse(""));
				// 将数据放入进去
				cell.setCellValue(text);
			} catch (Exception e) {
				log.error("写入数据失败，错误：" + ExceptionUtils.getFullStackTrace(e));
			}
		});
		totalNum++;
		rowIndex++;
		lastFlushTimes++;
		flush(false);
		buildCurrentSheet();
	}

	public void writeDatas(List datas){
		if(datas != null && !datas.isEmpty()){
			datas.forEach(data -> writeData(data));
		}
	}

	public void writeHeader(){
		Row row = currentSheet.createRow(rowIndex);
		// 将标题放入进去
		IntStream.range(0, headers.size()).forEach(index -> {
			// 创建一个单元格
			Cell cell = row.createCell(index);
			// 将数据转换为字符串
			HSSFRichTextString text = new HSSFRichTextString(headers.get(index).getHeaderName());
			// 将数据放入进去
			cell.setCellValue(text);
		});
		rowIndex++;
		flush(true);
	}

}
