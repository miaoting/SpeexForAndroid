package com.gauss.speex.encode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 数据转换工具类
 * 
 * @author mt
 *
 */
public class DataUtil {

	private static Logger logger = LoggerFactory.getLogger(DataUtil.class);

	private static final String CHARACTER_ENCODING = "GBK";

	public static final String UTF8_ENCODING = "UTF-8";

	public static final String GBK_ENCODING = "GBK";

	private static String[] binaryArray = { "0000", "0001", "0010", "0011",
			"0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
			"1100", "1101", "1110", "1111" };

	/** 16进制数字字符�? */
	private static String hexString = "0123456789ABCDEF ";

	/**
	 * 通过ASCII码将十进制的字节数组格式化为十六进制字符�?
	 * 
	 * @see 该方法会将字节数组中的所有字节均格式化为字符�?
	 * @see 使用说明详见<code>formatToHexStringWithASCII(byte[], int, int)</code>方法
	 */
	public static String formatToHexStringWithASCII(byte[] data) {
		return formatToHexStringWithASCII(data, 0, data.length);
	}

	/**
	 * 通过ASCII码将十进制的字节数组格式化为十六进制字符�?
	 * 
	 * @see 该方法常用于字符串的十六进制打印,打印时左侧为十六进制数�?,右侧为对应的字符串原�?
	 * @see 在构造右侧的字符串原文时,该方法内部使用的是平台的默认字符�?来解码byte[]数组
	 * @see 该方法在将字节转为十六进制时,默认使用的是<code>java.util.Locale.getDefault()</code>
	 * @see 详见String.format(String, Object...)方法和new String(byte[], int,
	 *      int)构�?方法
	 * @param data
	 *            十进制的字节数组
	 * @param offset
	 *            数组下标,标记从数组的第几个字节开始格式化输出
	 * @param length
	 *            格式长度,其不得大于数组长�?否则抛出java.lang.ArrayIndexOutOfBoundsException
	 * @return 格式化后的十六进制字符串
	 */
	public static String formatToHexStringWithASCII(byte[] data, int offset,
			int length) {
		int end = offset + length;
		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		sb.append("\r\n------------------------------------------------------------------------");
		boolean chineseCutFlag = false;
		for (int i = offset; i < end; i += 16) {
			sb.append(String.format("\r\n%04X: ", i - offset)); // X或x表示将结果格式化为十六进制整�?
			sb2.setLength(0);
			for (int j = i; j < i + 16; j++) {
				if (j < end) {
					byte b = data[j];
					if (b >= 0) { // ENG ASCII
						sb.append(String.format("%02X ", b));
						if (b < 32 || b > 126) { // 不可见字�?
							sb2.append(" ");
						} else {
							sb2.append((char) b);
						}
					} else { // CHA ASCII
						if (j == i + 15) { // 汉字前半个字�?
							sb.append(String.format("%02X ", data[j]));
							chineseCutFlag = true;
							String s = new String(data, j, 2);
							sb2.append(s);
						} else if (j == i && chineseCutFlag) { // 后半个字�?
							sb.append(String.format("%02X ", data[j]));
							chineseCutFlag = false;
							String s = new String(data, j, 1);
							sb2.append(s);
						} else {
							sb.append(String.format("%02X %02X ", data[j],
									data[j + 1]));
							String s = new String(data, j, 2);
							sb2.append(s);
							j++;
						}
					}
				} else {
					sb.append("   ");
				}
			}
			sb.append("| ");
			sb.append(sb2.toString());
		}
		sb.append("\r\n------------------------------------------------------------------------");
		return sb.toString();
	}

	/**
	 * 用于建立十六进制字符的输出的小写字符数组
	 */
	private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	/**
	 * 用于建立十六进制字符的输出的大写字符数组
	 */
	private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/**
	 * 将字节数组转换为十六进制字符数组
	 * 
	 * @param data
	 *            byte[]
	 * @return 十六进制char[]
	 */
	public static char[] encodeHex(byte[] data) {
		return encodeHex(data, true);
	}

	/**
	 * 将字节数组转换为十六进制字符数组
	 * 
	 * @param data
	 *            byte[]
	 * @param toLowerCase
	 *            <code>true</code> 传换成小写格�?�?<code>false</code> 传换成大写格�?
	 * @return 十六进制char[]
	 */
	public static char[] encodeHex(byte[] data, boolean toLowerCase) {
		return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
	}

	/**
	 * 将字节数组转换为十六进制字符数组
	 * 
	 * @param data
	 *            byte[]
	 * @param toDigits
	 *            用于控制输出的char[]
	 * @return 十六进制char[]
	 */
	protected static char[] encodeHex(byte[] data, char[] toDigits) {
		int l = data.length;
		char[] out = new char[l << 1];
		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
			out[j++] = toDigits[0x0F & data[i]];
		}
		return out;
	}

	/**
	 * 将字节数组转换为十六进制字符�?
	 * 
	 * @param data
	 *            byte[]
	 * @return 十六进制String
	 */
	public static String encodeHexStr(byte[] data) {
		return encodeHexStr(data, true);
	}

	/**
	 * 将字节数组转换为十六进制字符�?
	 * 
	 * @param data
	 *            byte[]
	 * @param toLowerCase
	 *            <code>true</code> 传换成小写格�?�?<code>false</code> 传换成大写格�?
	 * @return 十六进制String
	 */
	public static String encodeHexStr(byte[] data, boolean toLowerCase) {
		return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
	}

	/**
	 * 将字节数组转换为十六进制字符�?
	 * 
	 * @param data
	 *            byte[]
	 * @param toDigits
	 *            用于控制输出的char[]
	 * @return 十六进制String
	 */
	protected static String encodeHexStr(byte[] data, char[] toDigits) {
		return new String(encodeHex(data, toDigits));
	}

	/**
	 * 将十六进制字符数组转换为字节数组
	 * 
	 * @param data
	 *            十六进制char[]
	 * @return byte[]
	 * @throws RuntimeException
	 *             如果源十六进制字符数组是�?��奇�?的长度，将抛出运行时异常
	 */
	public static byte[] decodeHex(char[] data) {
		int len = data.length;
		if ((len & 0x01) != 0) {
			throw new RuntimeException("Odd number of characters.");
		}
		byte[] out = new byte[len >> 1];
		// two characters form the hex value.
		for (int i = 0, j = 0; j < len; i++) {
			int f = toDigit(data[j], j) << 4;
			j++;
			f = f | toDigit(data[j], j);
			j++;
			out[i] = (byte) (f & 0xFF);
		}
		return out;
	}

	/**
	 * 将十六进制字符转换成�?��整数
	 * 
	 * @param ch
	 *            十六进制char
	 * @param index
	 *            十六进制字符在字符数组中的位�?
	 * @return �?��整数
	 * @throws RuntimeException
	 *             当ch不是�?��合法的十六进制字符时，抛出运行时异常
	 */
	protected static int toDigit(char ch, int index) {
		int digit = Character.digit(ch, 16);
		if (digit == -1) {
			throw new RuntimeException("Illegal hexadecimal character " + ch
					+ " at index " + index);
		}
		return digit;
	}

	/**
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		return output.toByteArray();
	}

	/**
	 * 字符串转字节数组
	 * 
	 * @param targetStr
	 * @param len
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] StringToBytes(String targetStr, int len) {
		StringBuffer sb = new StringBuffer();
		if (targetStr == null) {
			return null;
		}
		// 获取当前字符串长�?
		byte[] curStrByte;
		try {
			curStrByte = targetStr.getBytes(GBK_ENCODING);
			int curLen = curStrByte.length;
			if (curLen > len) {
				// 如果超过长度字符串进行截�?
				targetStr = subStringByte(targetStr, len);
				curLen = targetStr.getBytes(GBK_ENCODING).length;
			}

			// 获取�?��补的空格�?
			int cutLength = len - curLen;
			// 添加空格
			for (int i = 0; i < cutLength; i++) {
				sb.append(' ');
			}
			// 加上原字符串
			sb.append(targetStr);
			return sb.toString().getBytes(GBK_ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 截取字符�?
	 * 
	 * @param targetStr
	 * @param strLength
	 * @return
	 */
	public static String subStringByte(String targetStr, int strLength) {
		while (targetStr.getBytes().length > strLength) {
			targetStr = targetStr.substring(0, targetStr.length() - 1);
		}
		return targetStr;
	}

	/**
	 * 字符串转字节数组
	 * 
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static byte[] StringToBytes(String str) {
		return StringToBytes(str, CHARACTER_ENCODING);
	}

	/**
	 * 字符串转字节数组，指定编�?
	 * 
	 * @param str
	 * @param encoding
	 * @return
	 */
	public static byte[] StringToBytes(String str, String encoding) {
		byte[] b = null;
		if (str != null) {
			try {
				b = str.getBytes(encoding);
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return b;
	}

	/**
	 * 字节数组转字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String BytesToString(byte[] b) {
		return BytesToString(b, CHARACTER_ENCODING);
	}

	/**
	 * 字节数组转字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String BytesToString(byte[] b, String encode) {
		String s = null;
		try {
			s = new String(b, encode);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return s;
	}

	/**
	 * 字符串编码为指定格式
	 * 
	 * @param string
	 * @param encoding
	 * @return
	 */
	public static String decodeString(String string, String encoding) {
		try {
			byte[] data = string2Bytes(string);
			return new String(data, encoding);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 字符串转字节数组
	 * 
	 * @param str
	 * @return
	 */
	private static byte[] string2Bytes(String str) {
		int blen = str.length() / 2;
		byte[] data = new byte[blen];
		for (int i = 0; i < blen; i++) {
			String bStr = str.substring(2 * i, 2 * (i + 1));
			data[i] = (byte) Integer.parseInt(bStr, 16);
		}
		return data;
	}

	/**
	 * 将字符串编码�?6进制数字,适用于所有字符（包括中文�?
	 * 
	 * @param str
	 * @return
	 */
	public static String encode(String str) {
		// 根据默认编码获取字节数组
		byte[] bytes = str.getBytes();
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		// 将字节数组中每个字节拆解�?�?6进制整数
		for (int i = 0; i < bytes.length; i++) {
			sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
			sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
		}
		return sb.toString();
	}

	/**
	 * 字节数组转十六进制字符串
	 * 
	 * @param src
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString().toUpperCase();
	}

	
	/**
	 * 字节数组转十六进制字符串
	 * 
	 * @param src
	 * @return
	 */
	public static String bytesToHexStringNew(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append("0x" + hv + ",");
		}
		return stringBuilder.toString().toUpperCase();
	}

	/**
	 * 短整型转十六进制字符�?
	 * 
	 * @param src
	 * @return
	 */
	public static String shortToHexString(short src) {
		byte[] b = shortToByteForBE(src);
		return bytesToHexString(b);
	}

	/**
	 * 整型转十六进制字符串
	 * 
	 * @param src
	 * @return
	 */
	public static String intToHexString(int src) {
		byte[] b = intToByteForBE(src);
		return bytesToHexString(b);
	}

	/**
	 * 长整型转十六进制字符�?
	 * 
	 * @param src
	 * @return
	 */
	public static String longToHexString(long src) {
		byte[] b = longToByteForBE(src);
		return bytesToHexString(b);
	}

	/**
	 * 十六进制字符串转字节数组
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase().replaceAll(" ", "");
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * 字符转字�?
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * �?6进制数字解码成字符串,适用于所有字符（包括中文�?
	 * 
	 * @param bytes
	 * @return
	 */
	public static String decode(String bytes) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
				bytes.length() / 2);
		// 将每2�?6进制整数组装成一个字�?
		for (int i = 0; i < bytes.length(); i += 2)
			baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
					.indexOf(bytes.charAt(i + 1))));
		return new String(baos.toByteArray());
	}

	/**
	 * 将字符串转成16进制字符�?
	 * 
	 * @param s
	 * @param encode
	 * @return
	 */
	public static String toStringHex(String s, String encode) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(
						s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			s = new String(baKeyword, encode);// UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	/**
	 * 将字符串转成16进制字符�?
	 * 
	 * @param s
	 * @return
	 */
	public static String toStringHex(String s) {
		return toStringHex(s, CHARACTER_ENCODING);
	}

	/**
	 * 十六进制字符串装十进�?
	 * 
	 * @param hex
	 *            十六进制字符�?
	 * @return 十进制数�?
	 */
	public static int hexStringToAlgorism(String hex) {
		hex = hex.toUpperCase();
		int max = hex.length();
		int result = 0;
		for (int i = max; i > 0; i--) {
			char c = hex.charAt(i - 1);
			int algorism = 0;
			if (c >= '0' && c <= '9') {
				algorism = c - '0';
			} else {
				algorism = c - 55;
			}
			result += Math.pow(16, max - i) * algorism;
		}
		return result;
	}

	/**
	 * 十六转二进制
	 * 
	 * @param hex
	 *            十六进制字符�?
	 * @return 二进制字符串
	 */
	public static String hexStringToBinary(String hex) {
		hex = hex.toUpperCase();
		String result = "";
		int max = hex.length();
		for (int i = 0; i < max; i++) {
			char c = hex.charAt(i);
			switch (c) {
			case '0':
				result += "0000";
				break;
			case '1':
				result += "0001";
				break;
			case '2':
				result += "0010";
				break;
			case '3':
				result += "0011";
				break;
			case '4':
				result += "0100";
				break;
			case '5':
				result += "0101";
				break;
			case '6':
				result += "0110";
				break;
			case '7':
				result += "0111";
				break;
			case '8':
				result += "1000";
				break;
			case '9':
				result += "1001";
				break;
			case 'A':
				result += "1010";
				break;
			case 'B':
				result += "1011";
				break;
			case 'C':
				result += "1100";
				break;
			case 'D':
				result += "1101";
				break;
			case 'E':
				result += "1110";
				break;
			case 'F':
				result += "1111";
				break;
			}
		}
		return result;
	}

	/**
	 * 将十进制转换为指定长度的十六进制字符�?
	 * 
	 * @param algorism
	 *            int 十进制数�?
	 * @param maxLength
	 *            int 转换后的十六进制字符串长�?
	 * @return String 转换后的十六进制字符�?
	 */
	public static String algorismToHEXString(int algorism, int maxLength) {
		String result = "";
		result = Integer.toHexString(algorism);

		if (result.length() % 2 == 1) {
			result = "0" + result;
		}
		return patchHexString(result.toUpperCase(), maxLength);
	}

	/**
	 * HEX字符串前�?，主要用于长度位数不足�?
	 * 
	 * @param str
	 *            String �?��补充长度的十六进制字符串
	 * @param maxLength
	 *            int 补充后十六进制字符串的长�?
	 * @return 补充结果
	 */
	public static String patchHexString(String str, int maxLength) {
		String temp = "";
		for (int i = 0; i < maxLength - str.length(); i++) {
			temp = "0" + temp;
		}
		str = (temp + str).substring(0, maxLength);
		return str;
	}

	/**
	 * 字符串前�?转成字节数组
	 * 
	 * @param str
	 * @param len
	 * @return
	 */
	public static byte[] StringToPatchBytes(String str, int len) {
		byte[] byteStr = StringToBytes(str);
		String hexStr = bytesToHexString(byteStr);
		String patchHexStr = patchHexString(hexStr, len * 8);
		byte[] val = hexStringToBytes(patchHexStr);
		if (val.length <= len) {
			return val;
		}
		return null;
	}

	/**
	 * 字节转二进制字符�?
	 * 
	 * @param b
	 * @return
	 */
	public static String byte2bits(byte b) {

		int z = b;
		z |= 256;
		String str = Integer.toBinaryString(z);
		int len = str.length();
		return str.substring(len - 8, len);
	}

	/**
	 * 字节数组转二进制字符�?
	 * 
	 * @param bArray
	 * @return
	 */
	public static String bytes2BinaryStr(byte[] bArray) {

		String outStr = "";
		int pos = 0;
		for (byte b : bArray) {
			// 高四�?
			pos = (b & 0xF0) >> 4;
			outStr += binaryArray[pos];
			// 低四�?
			pos = b & 0x0F;
			outStr += binaryArray[pos];
		}
		return outStr;

	}

	/**
	 * 字节数组转整�?
	 * 
	 * @param bRefArr
	 * @return
	 */
	public static int byteToInt(byte bRef) {
		return bRef & 0xFF;
	}

	/**
	 * 字节数组转整�?
	 * 
	 * @param bRefArr
	 * @return
	 */
	public static int byteToInt(byte[] bRefArr) {
		int iOutcome = 0;
		byte bLoop;

		for (int i = 0; i < bRefArr.length; i++) {
			bLoop = bRefArr[i];
			iOutcome += (bLoop & 0xFF) << (8 * i);
		}
		return iOutcome;
	}

	/**
	 * 字节数组转整型（高字节序�?
	 * 
	 * @param b
	 * @return
	 */
	public static int byteToIntForBE(byte[] b) {
		int s = 0;
		int s0 = b[0] & 0xff; // �?���?
		int s1 = b[1] & 0xff;
		int s2 = b[2] & 0xff;
		int s3 = b[3] & 0xff; // �?���?

		s0 <<= 8 * 3;
		s1 <<= 8 * 2;
		s2 <<= 8 * 1;

		s = s0 | s1 | s2 | s3;
		return s;
	}

	/**
	 * 整型转字节数�?
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] intToByte(int number) {
		int temp = number;
		byte[] b = new byte[4];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
			temp = temp >> 8; // 向右�?�?
		}
		return b;
	}

	/**
	 * 整型转字节数组（高字节序�?
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] intToByteForBE(int number) {
		byte[] b = new byte[4];

		b[0] = (byte) ((number & 0xff000000) >> 24);
		b[1] = (byte) ((number & 0x00ff0000) >> 16);
		b[2] = (byte) ((number & 0x0000ff00) >> 8);
		b[3] = (byte) (number & 0xff);

		return b;
	}

	/**
	 * 短整型转字节
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] shortToByte(short number) {
		int temp = number;
		byte[] b = new byte[2];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Integer(temp & 0xff).byteValue(); // 将最低位保存在最低位
			temp = temp >> 8; // 向右�?�?
		}
		return b;
	}

	/**
	 * 短整型转字节（高字节序）
	 * 
	 * @param a
	 * @return
	 */
	public static byte[] shortToByteForBE(short a) {
		byte[] b = new byte[2];

		b[0] = (byte) (a >> 8);
		b[1] = (byte) (a);

		return b;
	}

	/**
	 * 字节转短整型
	 * 
	 * @param b
	 * @return
	 */
	public static short byteToShort(byte[] b) {
		short s = 0;
		short s0 = (short) (b[0] & 0xff);// �?���?
		short s1 = (short) (b[1] & 0xff);
		s1 <<= 8;
		s = (short) (s0 | s1);
		return s;
	}

	/**
	 * 字节转短整型（高字节序）
	 * 
	 * @param b
	 * @return
	 */
	public static short byteToShortForBE(byte[] b) {
		short s = 0;
		short s0 = (short) (b[0] & 0xff);// �?���?
		short s1 = (short) (b[1] & 0xff);
		s0 <<= 8;
		s = (short) (s0 | s1);
		return s;
	}

	/**
	 * 长整型类型转成字节数�?
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] longToByte(long number) {
		long temp = number;
		byte[] b = new byte[8];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Long(temp & 0xff).byteValue();// 将最低位保存在最低位
			temp = temp >> 8; // 向右�?�?
		}
		return b;
	}

	/**
	 * 长整型转字节数组（高字节序）
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] longToByteForBE(long number) {
		byte[] b = new byte[8];

		b[0] = (byte) ((number & 0xff00000000000000l) >> (8 * 7));
		b[1] = (byte) ((number & 0x00ff000000000000l) >> (8 * 6));
		b[2] = (byte) ((number & 0x0000ff0000000000l) >> (8 * 5));
		b[3] = (byte) ((number & 0x000000ff00000000l) >> (8 * 4));
		b[4] = (byte) ((number & 0xff000000) >> (8 * 3));
		b[5] = (byte) ((number & 0x00ff0000) >> (8 * 2));
		b[6] = (byte) ((number & 0x0000ff00) >> (8 * 1));
		b[7] = (byte) (number & 0xff);

		return b;
	}

	/**
	 * 字节数组转成长整�?
	 * 
	 * @param b
	 * @return
	 */
	public static long byteToLong(byte[] b) {
		long s = 0;
		long s0 = b[0] & 0xff;// �?���?
		long s1 = b[1] & 0xff;
		long s2 = b[2] & 0xff;
		long s3 = b[3] & 0xff;
		long s4 = b[4] & 0xff;// �?���?
		long s5 = b[5] & 0xff;
		long s6 = b[6] & 0xff;
		long s7 = b[7] & 0xff;

		// s0不变
		s1 <<= 8;
		s2 <<= 16;
		s3 <<= 24;
		s4 <<= 8 * 4;
		s5 <<= 8 * 5;
		s6 <<= 8 * 6;
		s7 <<= 8 * 7;
		s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
		return s;
	}

	/**
	 * 字节数组转成长整型（高字节序�?
	 * 
	 * @param b
	 * @return
	 */
	public static long byteToLongForBE(byte[] b) {
		long s = 0;
		long s0 = b[0] & 0xff; // �?���?
		long s1 = b[1] & 0xff;
		long s2 = b[2] & 0xff;
		long s3 = b[3] & 0xff;
		long s4 = b[4] & 0xff;
		long s5 = b[5] & 0xff;
		long s6 = b[6] & 0xff;
		long s7 = b[7] & 0xff; // �?���?

		s0 <<= 8 * 7;
		s1 <<= 8 * 6;
		s2 <<= 8 * 5;
		s3 <<= 8 * 4;
		s4 <<= 8 * 3;
		s5 <<= 8 * 2;
		s6 <<= 8 * 1;

		s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
		return s;
	}

	/**
	 * IP转字节数�?
	 * 
	 * @param strIP
	 * @return
	 */
	public static byte[] ipToBytes(String strIP) {
		long ip = ipToLong(strIP);
		return longToByteForBE(ip);
	}

	/**
	 * IP地址转长整型
	 * 
	 * @param strIP
	 * @return
	 */
	public static long ipToLong(String strIP) {
		long[] ip = new long[4];
		int position1 = strIP.indexOf(".");
		int position2 = strIP.indexOf(".", position1 + 1);
		int position3 = strIP.indexOf(".", position2 + 1);
		ip[0] = Long.parseLong(strIP.substring(0, position1));
		ip[1] = Long.parseLong(strIP.substring(position1 + 1, position2));
		ip[2] = Long.parseLong(strIP.substring(position2 + 1, position3));
		ip[3] = Long.parseLong(strIP.substring(position3 + 1));
		return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
	}

	/**
	 * 字节数组转IP地址
	 * 
	 * @param b
	 * @return
	 */
	public static String bytesToIp(byte[] b) {
		if (b == null || b.length != 8) {
			return null;
		}
		long ip = byteToLongForBE(b);
		return longToIP(ip);
	}

	/**
	 * 长整型转IP地址
	 * 
	 * @param longIP
	 * @return
	 */
	public static String longToIP(long longIP) {
		StringBuffer sb = new StringBuffer();
		sb.append(String.valueOf(longIP >>> 24));// 直接右移24�?
		sb.append(".");
		sb.append(String.valueOf((longIP & 0x00FFFFFF) >>> 16)); // 将高8位置0，然后右�?6�?
		sb.append(".");
		sb.append(String.valueOf((longIP & 0x0000FFFF) >>> 8));
		sb.append(".");
		sb.append(String.valueOf(longIP & 0x000000FF));
		// sb.append(".");
		return sb.toString();
	}

	/**
	 * 获取字符串的长度（任意的编码�?
	 * 
	 * @param str
	 *            输入的字符串
	 * @param encoding
	 *            编码
	 * @return
	 */
	public static int getStrLen(String str, String encoding) {
		byte[] curStrByte;
		int curLen = 0;
		try {
			curStrByte = str.getBytes(encoding);
			curLen = curStrByte.length;
		} catch (UnsupportedEncodingException e) {
			curLen = -1;
			logger.error(e.getMessage(), e);
		}
		return curLen;
	}

	/**
	 * 生成校验�?
	 * 
	 * @param dataStr
	 * @return
	 */
	public static String generateSumVrf(String dataStr) {
		byte[] dataBytes = hexStringToBytes(dataStr);
		return generateSumVrf(dataBytes);
	}

	/**
	 * 生成校验�?
	 * 
	 * @param dataByte
	 * @return
	 */
	public static String generateSumVrf(byte[] dataByte) {
		int r = 0;
		for (byte b : dataByte) {
			int i = (int) ((char) b & 0xFF);
			r += i;
		}
		byte[] b = new byte[2];
		b[0] = (byte) ((r & 0xff00) >>> 8);
		b[1] = (byte) (r & 0xff);
		return bytesToHexString(b);
	}
	
	
	/**
	 * int数组转byte数组
	 * @param voice
	 * @return
	 */
	public byte[] toByte(int[] voice) {
		byte[] toByte = new byte[voice.length];

		for (int i = 0; i < voice.length; i++) {
			int a = voice[i];
			byte b = (byte) (a & 0xFF);
			toByte[i] = b;
		}
		return toByte;
	}

	public static void main(String[] args) {
		System.out.println(DataUtil.byteToIntForBE("1122334455667788"
				.getBytes()));

		String s = "55AA000A00002089848FD97F0065000C05010105000820203132333435360F";
		byte[] b = DataUtil.hexStringToBytes(s);
		System.out.println(DataUtil.bytesToHexString(b));
	}
}
