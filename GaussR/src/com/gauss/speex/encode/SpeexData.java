package com.gauss.speex.encode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Speex ����
 * 
 * @author mt
 * 
 */
public class SpeexData {
	private static Logger logger = LoggerFactory.getLogger(SpeexData.class);

	/** ��ȡ�ļ����� */
	public static final int GET_OVER = -1;
	/** ��ȡ�ļ��ȴ� */
	public static final int GET_WAIT = -2;

	/** �ļ����� */
	private String fileName = null;
	/** ���ļ� */
	private FileInputStream is = null;
	/** д�ļ� */
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