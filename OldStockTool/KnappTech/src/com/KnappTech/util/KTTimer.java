package com.KnappTech.util;

public class KTTimer implements Runnable {
	private static final boolean ENABLED = true;
	private int waitTimeSeconds = 5;
	private String message = "timer expired.";
	private Thread wakeUpThread = null;
	private Thread timerThread = null;
	private boolean willStopThread = false;
	
	public KTTimer(String name, int waitTimeSeconds, String message,boolean willStopThread) {
		this.waitTimeSeconds = waitTimeSeconds;
		this.message = message;
		this.wakeUpThread = Thread.currentThread();
		this.willStopThread = willStopThread;
		if (ENABLED) {
			timerThread = new Thread(this,"timer for "+name);
			timerThread.start();
		}
	}
	
	public void stop() {
		if (ENABLED) {
			timerThread.interrupt();
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000*waitTimeSeconds);
			
			if (message!=null && !message.isEmpty()) {
				System.err.println(message);
			}
			if (wakeUpThread!=null && willStopThread) {
				wakeUpThread.interrupt();
			}
		} catch (InterruptedException e) {
			// do nothing.  Just stop waiting and don't stop previous thread.
		}
	}

	public boolean isRunning() {
		return timerThread!=null;
	}

}
