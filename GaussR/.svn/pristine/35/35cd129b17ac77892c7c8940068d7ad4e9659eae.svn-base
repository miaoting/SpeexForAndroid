package com.gauss.speex.encode;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Speex编码音频录制
 * 
 * 
 * @author mt
 * 
 */
public class SpeexRecorder {
	private static Logger logger = LoggerFactory.getLogger(SpeexRecorder.class);

	private boolean isRecording;
	private final Object mutex = new Object();
	private static final int frequency = 8000;
	private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	public static int packagesize = 160;

	private SpeexData speexData = null;
	/** 编码线程 */
	private SpeexEncoder encoder = null;

	private Thread th = null;

	private Speex speex = new Speex();
	public static int encoder_packagesize = 1024;
	private byte[] processedData = new byte[encoder_packagesize];

	List<ReadData> list = null;

	public SpeexRecorder() {
		super();
	}

	/**
	 * 开始录制
	 * 
	 * @param speexData
	 *            录制的SpeexData
	 */
	public void startRecord(SpeexData speexData) {
		if (isRecording) {
			logger.error("startRecord() - 正在录音...");
			return;
		}

		setSpeexData(speexData);
		setRecording(true);

		RecordPlayThread recordPlayThread = new RecordPlayThread();
		th = new Thread(recordPlayThread);
		th.start();
	}

	/**
	 * 停止录制
	 */
	public void stopRecord() {
		try {
			if (th != null) {
				th.interrupt();
			}
		} catch (Exception e) {
		}
		setRecording(false);
	}

	class RecordPlayThread extends Thread {

		public void run() {
			encoder = new SpeexEncoder();
			Thread encodeThread = new Thread(encoder);
			isRecording = true;
			encodeThread.start();

			synchronized (mutex) {
				while (!isRecording) {
					try {
						mutex.wait();
					} catch (InterruptedException e) {
						throw new IllegalStateException("Wait() interrupted!",
								e);
					}
				}
			}
			android.os.Process
					.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

			int bufferRead = 0;
			int bufferSize = AudioRecord.getMinBufferSize(frequency,
					AudioFormat.CHANNEL_IN_MONO, audioEncoding);

			short[] tempBuffer = new short[packagesize];

			AudioRecord recordInstance = new AudioRecord(
					MediaRecorder.AudioSource.MIC, frequency,
					AudioFormat.CHANNEL_IN_MONO, audioEncoding, bufferSize);

			recordInstance.startRecording();

			while (isRecording) {
				logger.debug("run() - 开始录制音频...");
				bufferRead = recordInstance.read(tempBuffer, 0, packagesize);
				if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
					throw new IllegalStateException(
							"read() returned AudioRecord.ERROR_INVALID_OPERATION");
				} else if (bufferRead == AudioRecord.ERROR_BAD_VALUE) {
					throw new IllegalStateException(
							"read() returned AudioRecord.ERROR_BAD_VALUE");
				} else if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
					throw new IllegalStateException(
							"read() returned AudioRecord.ERROR_INVALID_OPERATION");
				}
				putData(tempBuffer, bufferRead);
			}
			recordInstance.stop();
			setRecording(false);
		}
	}

	public void setSpeexData(SpeexData speexData) {
		this.speexData = speexData;
	}

	class SpeexEncoder implements Runnable {

		public SpeexEncoder() {
			speex.init();
			list = Collections.synchronizedList(new LinkedList<ReadData>());
		}

		public void run() {
			android.os.Process
					.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			int getSize = 0;
			try {
				speexData.startSetData();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			while (isRecording) {
				if (list.size() == 0) {
					logger.debug("run() - 没有数据需要做编码");
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				if (list.size() > 0) {
					synchronized (mutex) {
						ReadData rawdata = list.remove(0);
						getSize = speex.encode(rawdata.ready, 0, processedData,
								rawdata.size);
						logger.info("编码前 =" + rawdata.size + " 编码后 ="
								+ processedData.length + " getsize=" + getSize);
					}
					if (getSize > 0) {
						try {
							byte[] dataNew = new byte[getSize];
							System.arraycopy(processedData, 0, dataNew, 0,
									getSize);
							speexData.setFrame(dataNew);
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
						processedData = new byte[encoder_packagesize];
					}
				}
			}
		}

	}

	/**
	 * 供Recorder方待处理的数据
	 * 
	 * @param data
	 * @param size
	 */
	public void putData(short[] data, int size) {
		ReadData rd = new ReadData();
		synchronized (mutex) {
			rd.size = size;
			System.arraycopy(data, 0, rd.ready, 0, size);
			list.add(rd);
		}
	}

	public void setRecording(boolean isRecording) {
		synchronized (mutex) {
			this.isRecording = isRecording;
			if (isRecording == false) {
				logger.debug("setRecording() - 录制完成 ...");
				try {
					speexData.stopSetData();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	class ReadData {
		private int size;
		private short[] ready = new short[encoder_packagesize];
	}

}
