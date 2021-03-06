package com.gauss.speex.encode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Speex 数据
 * 
 * @author mt
 * 
 */
public class SpeexData {
	private static Logger logger = LoggerFactory.getLogger(SpeexData.class);

	/** 获取文件结束 */
	public static final int GET_OVER = -1;
	/** 获取文件等待 */
	public static final int GET_WAIT = -2;

	/** 文件名称 */
	private String fileName = null;
	/** 读文件 */
	private FileInputStream is = null;
	/** 写文件 */
	private FileOutputStream os = null;

	public SpeexData(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void startSetData() {
		try {
			stopSetData();
			File file = new File(fileName);
			if (file.exists()) {
				file.delete();
			}
			os = new FileOutputStream(file);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void setFrame(byte[] data) {
		try {
			if (os != null) {
				os.write(data);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void stopSetData() {
		try {
			if (os != null) {
				os.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		os = null;
	}

	public void startGetData() {
		try {
			stopGetData();
			is = new FileInputStream(fileName);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public int getFrame(byte[] buffer) {
		int len = GET_OVER;
		try {
			len = is.read(buffer);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return len;
	}

	public void stopGetData() {
		try {
			if (is != null) {
				is.close();
			}
			is = null;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		return "SpeexData [fileName=" + fileName + "]";
	}
}
