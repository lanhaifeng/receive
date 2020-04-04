package com.hzmc.auditReceive.util;

import java.security.MessageDigest;

/**
 * receive
 * 2020/4/3 9:59
 * sql工具类
 *
 * @author lanhaifeng
 * @since
 **/
public class SqlIdUtils {
	private static final char[] ALPHABET = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

	protected SqlIdUtils() {
	}

	public static String getSqlId(String sql) {
		byte[] md52 = md5Hash(formatSql(sql) + "\u0000");
		byte[] submd52 = new byte[]{md52[11], md52[10], md52[9], md52[8], md52[15], md52[14], md52[13], md52[12]};
		return encode(submd52);
	}

	private static byte[] md5Hash(String pwd) {
		try {
			byte[] btInput = pwd.getBytes("UTF-8");
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			return mdInst.digest();
		} catch (Exception var3) {
			var3.printStackTrace();
			return null;
		}
	}

	private static String encode(byte[] data) {
		StringBuilder source = new StringBuilder();
		StringBuilder returnstr = new StringBuilder();
		byte[] var6 = data;
		int var5 = data.length;

		for(int var4 = 0; var4 < var5; ++var4) {
			byte a = var6[var4];
			String binstr = Integer.toBinaryString(a);
			if (a < 0) {
				source.append(binstr.substring(24));
			} else {
				for(int i = 0; i < 8 - binstr.length(); ++i) {
					source.append("0");
				}

				source.append(binstr);
			}
		}

		returnstr.append(getCode(source.substring(0, 4)));

		for(int i = 1; i <= 12; ++i) {
			returnstr.append(getCode(source.substring(5 * i - 1, 5 * i + 4)));
		}

		return returnstr.toString();
	}

	private static char getCode(String bin) {
		Integer index = getIntByBinary(bin);
		return ALPHABET[index];
	}

	private static Integer getIntByBinary(String bin) {
		int result = 0;
		char[] chars = bin.toCharArray();

		for(int i = 0; i < chars.length; ++i) {
			char temp = chars[i];
			if (temp == '1') {
				result += (int)Math.pow(2.0D, (double)(chars.length - i - 1));
			}
		}

		return result;
	}

	private static String formatSql(String sql) {
		return sql.endsWith(" ") ? rtrim(sql) + "\n" : sql;
	}

	private static String rtrim(String str) {
		int num = str.length();
		if (str.endsWith(" ")) {
			str = str.substring(0, num - 1);
			return rtrim(str);
		} else {
			return str;
		}
	}
}
