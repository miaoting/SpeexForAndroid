package com.gauss.recorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gauss.speex.encode.SpeexData;
import com.gauss.speex.encode.SpeexPlayer;
import com.gauss.speex.encode.SpeexPlayer.SpeexPlayerListener;
import com.gauss.speex.encode.SpeexRecorder;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GaussRecorderActivity extends Activity implements OnClickListener {

	private static Logger logger = LoggerFactory
			.getLogger(GaussRecorderActivity.class);

	private String fileName = "/storage/emulated/0/oncea.spx";

	public static final int STOPPED = 0;
	public static final int RECORDING = 1;

	/** Speex编码音频录制 */
	private SpeexRecorder recorderInstance = null;
	/** Speex编码播放器 */
	private SpeexPlayer splayer = null;
	/** 声音文件 */
	private SpeexData speexData = null;



	Button startButton = null;
	Button stopButton = null;
	Button playButton = null;
	Button exitButon = null;
	TextView textView = null;
	int status = STOPPED;

	public void onClick(View v) {
		if (v == startButton) {
			this.setTitle("开始录音了！");
			speexData = new SpeexData(fileName);
			recorderInstance.startRecord(speexData);
		} else if (v == stopButton) {
			if (recorderInstance != null) {
				this.setTitle("停止了");
				recorderInstance.stopRecord();
			}
		} else if (v == playButton) {
			if(speexData == null){
				speexData  = new SpeexData(fileName);
			}
			if (speexData != null) {
				this.setTitle("开始播放");
				playButton.setText("停止");
				splayer.startPlay(speexData);
			}
		}  else if (v == exitButon) {
			if (recorderInstance != null) {
				recorderInstance.stopRecord();
			}
			if (splayer != null) {
				splayer.stopPlay();
			}
			System.exit(0);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		startButton = new Button(this);
		stopButton = new Button(this);
		exitButon = new Button(this);
		playButton = new Button(this);
		textView = new TextView(this);

		startButton.setText("Start");
		stopButton.setText("Stop");
		playButton.setText("播放");
		exitButon.setText("退出");
		textView.setText("android 录音机：\n(1)获取PCM数据." + "\n(2)使用speex编码");

		startButton.setOnClickListener(this);
		playButton.setOnClickListener(this);
		stopButton.setOnClickListener(this);
		exitButon.setOnClickListener(this);

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(textView);
		layout.addView(startButton);
		layout.addView(stopButton);
		layout.addView(playButton);
		layout.addView(exitButon);
		this.setContentView(layout);
		
		if (recorderInstance == null) {
			recorderInstance = new SpeexRecorder();
		}
		if (splayer == null) {
			splayer = new SpeexPlayer();
			splayer.setSpeexPlayerListener(new SpeexPlayerListener() {
				@Override
				public void onPlayStateChange(boolean isPlay) {
					if(!isPlay){
						
						handler.sendEmptyMessage(PLAY_STOP);
					}
				}
			});
		}
	}
	
	private final int PLAY_STOP = 0;
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PLAY_STOP:
				playButton.setText("播放");
				break;

			default:
				break;
			}
		};
	};
}