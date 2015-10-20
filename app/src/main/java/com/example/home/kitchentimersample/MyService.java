package com.example.home.kitchentimersample;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {

	long _alarmTime;
	Timer _timer;
	MediaPlayer _mediaPlayer;
	Handler _handler = new Handler();

	@Override
	public void onCreate() {
		super.onCreate();
		_mediaPlayer = MediaPlayer.create(this, R.raw.se_maoudamashii_jingle04);
		_mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				stopSelf();
			}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (_timer != null)
			_timer.cancel();
		initAlarm();
		_alarmTime = calcAlarmTime(intent);

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				// startと同時に１秒減るため、あらかじめ1000ms分を足しておく
				long remainingTime = _alarmTime - System.currentTimeMillis() + 1000;
				Intent broadcastIntent = new Intent();
				broadcastIntent.putExtra("time", remainingTime);
				broadcastIntent.setAction("com.example.home.kitchentimersample"); // 一意の文字列を指定
				sendBroadcast(broadcastIntent);
				doAlarm(remainingTime);
			}
		};
		_timer = new Timer();
		_timer.schedule(timerTask, 0/*Delay*/, 200 /*Interval*/);
		return super.onStartCommand(intent, flags, startId);
	}

	private void initAlarm() {
		if (_mediaPlayer.isPlaying()) {
			_mediaPlayer.pause();
			_mediaPlayer.seekTo(0);
		}
	}

	private void doAlarm(long remainingTime) {
		if (remainingTime > 0) return;

		_timer.cancel();
		_mediaPlayer.start();
		// UIスレッド外から描画処理を行う場合はハンドラーを投げる
		_handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "時間です", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private long calcAlarmTime(Intent intent) {
		ArrayList<String> timeAry = intent.getStringArrayListExtra("time");
		String min = timeAry.get(0) + timeAry.get(1);
		String sec = timeAry.get(2) + timeAry.get(3);
		long lMin = Long.parseLong(min) * 60 * 1000;
		long lSec = Long.parseLong(sec) * 1000;
		return (System.currentTimeMillis() + lMin + lSec);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_timer.cancel();
		_mediaPlayer.release();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
