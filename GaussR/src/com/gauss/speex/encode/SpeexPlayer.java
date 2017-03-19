/**
 * 
 */
package com.gauss.speex.encode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * @author Speex编码播放器
 * 
 */
public class SpeexPlayer {
	private static Logger logger = LoggerFactory.getLogger(SpeexPlayer.class);

	private SpeexData speexData = null;

	private boolean isPlay = false;

	private Thread th;
	/** 播放状态监听 */
	private SpeexPlayerListener speexPlayerListener = null;

	protected Speex speexDecoder = null;

	private AudioTrack track = null;

	public SpeexPlayer() {
		isPlay = false;
	}

	/**
	 * 开始播放
	 */
	public void startPlay(SpeexData speexData) {
		if (isPlay()) {
			logger.error("startPlay() - 正在播放...");
			stopPlay();
			return;
		}
		setSpeexData(speexData);
		logger.debug("startPlay() - ");
		RecordPlayThread rpt = new RecordPlayThread();

		th = new Thread(rpt);
		th.start();
	}

	/**
	 * 停止播放
	 */
	public void stopPlay() {
		try {
			if (th != null) {
				th.interrupt();
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 初始化音频播放器
	 * 
	 * @param sampleRate
	 *            采样率
	 * @throws Exception
	 */
	private void initializeAndroidAudio(int sampleRate) throws Exception {
		logger.debug("initializeAndroidAudio() - sampleRate:{}", sampleRate);
		int minBufferSize = AudioTrack.getMinBufferSize(sampleRate,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

		if (minBufferSize < 0) {
			logger.error("Failed to get minimum buffer size: {}",
					Integer.toString(minBufferSize));
			throw new Exception("Failed to get minimum buffer size: "
					+ Integer.toString(minBufferSize));
		}

		track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
				minBufferSize, AudioTrack.MODE_STREAM);

	}

	/**
	 * 是否在播放
	 * 
	 * @return
	 */
	public boolean isPlay() {
		return isPlay;
	}

	/**
	 * 设置 SpeexData
	 * 
	 * @param speexData
	 */
	public void setSpeexData(SpeexData speexData) {
		this.speexData = speexData;
	}

	/**
	 * 解码方法
	 * 
	 * @param voiceNew
	 *            Speex编码流
	 * @throws Exception
	 */
	public void decode() throws Exception {
		int decsize = 0;
		int packetNo = 0;
		isPlay = true;
		speexDecoder = new Speex();
		speexDecoder.init();
		if (speexPlayerListener != null) {
			speexPlayerListener.onPlayStateChange(isPlay);
		}
		try {
			while (true) {
				if (Thread.interrupted()) {
					break;
				}
				if (!isPlay) {
					break;
				}
				if (packetNo == 0) {
					// 初始化声音播放器
					initializeAndroidAudio(8000);
					speexData.startGetData();
					packetNo++;
				} else {
					// 每次20毫秒读取20字节
					int sizeNew = 20;
					short[] decoded = new short[160];
					byte[] vo = new byte[sizeNew];
					int len = speexData.getFrame(vo);
					logger.debug("decode() - len:{}", len);
					if (len == SpeexData.GET_OVER) {
						break;
					}
					if (len == SpeexData.GET_WAIT) {
						continue;
					}
					if ((decsize = speexDecoder.decode(vo, decoded, vo.length)) > 0) {
						track.write(decoded, 0, decsize);
						track.setStereoVolume(0.7f, 0.7f);// 设置当前音量大小
						track.play();
					}
					packetNo++;
				}
			}
		} catch (Exception ef) {
			logger.error(ef.getMessage(), ef);
		} finally {
			logger.debug("decode() - 播放结束...");
			try {
				speexData.stopGetData();
			} catch (Exception e) {
			}
			if (track.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
				track.stop();
			}
			track.release();
			isPlay = false;

			if (speexPlayerListener != null) {
				speexPlayerListener.onPlayStateChange(isPlay);
			}
		}
	}

	class RecordPlayThread extends Thread {

		public void run() {

			try {
				decode();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	};

	public SpeexPlayerListener getSpeexPlayerListener() {
		return speexPlayerListener;
	}

	/**
	 * 设置播放状态监听
	 * 
	 * @param speexPlayerListener
	 */
	public void setSpeexPlayerListener(SpeexPlayerListener speexPlayerListener) {
		this.speexPlayerListener = speexPlayerListener;
	}

	/**
	 * 播放状态监听
	 */
	public interface SpeexPlayerListener {
		public void onPlayStateChange(boolean isPlay);
	}

}
