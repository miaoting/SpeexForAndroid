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
 * @author Speex���벥����
 * 
 */
public class SpeexPlayer {
	private static Logger logger = LoggerFactory.getLogger(SpeexPlayer.class);

	private SpeexData speexData = null;

	private boolean isPlay = false;

	private Thread th;
	/** ����״̬���� */
	private SpeexPlayerListener speexPlayerListener = null;

	protected Speex speexDecoder = null;

	private AudioTrack track = null;

	public SpeexPlayer() {
		isPlay = false;
	}

	/**
	 * ��ʼ����
	 */
	public void startPlay(SpeexData speexData) {
		if (isPlay()) {
			logger.error("startPlay() - ���ڲ���...");
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
	 * ֹͣ����
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
	 * ��ʼ����Ƶ������
	 * 
	 * @param sampleRate
	 *            ������
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
	 * �Ƿ��ڲ���
	 * 
	 * @return
	 */
	public boolean isPlay() {
		return isPlay;
	}

	/**
	 * ���� SpeexData
	 * 
	 * @param speexData
	 */
	public void setSpeexData(SpeexData speexData) {
		this.speexData = speexData;
	}

	/**
	 * ���뷽��
	 * 
	 * @param voiceNew
	 *            Speex������
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
					// ��ʼ������������
					initializeAndroidAudio(8000);
					speexData.startGetData();
					packetNo++;
				} else {
					// ÿ��20�����ȡ20�ֽ�
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
						track.setStereoVolume(0.7f, 0.7f);// ���õ�ǰ������С
						track.play();
					}
					packetNo++;
				}
			}
		} catch (Exception ef) {
			logger.error(ef.getMessage(), ef);
		} finally {
			logger.debug("decode() - ���Ž���...");
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
	 * ���ò���״̬����
	 * 
	 * @param speexPlayerListener
	 */
	public void setSpeexPlayerListener(SpeexPlayerListener speexPlayerListener) {
		this.speexPlayerListener = speexPlayerListener;
	}

	/**
	 * ����״̬����
	 */
	public interface SpeexPlayerListener {
		public void onPlayStateChange(boolean isPlay);
	}

}
