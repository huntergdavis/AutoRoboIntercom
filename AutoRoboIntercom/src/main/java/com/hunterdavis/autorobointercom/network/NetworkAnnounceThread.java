package com.hunterdavis.autorobointercom.network;

import java.io.IOException;

/**
 * Created by hunter on 3/3/14.
 */
public class NetworkAnnounceThread extends Thread{
    private Object mPauseLock;
    private boolean mPaused;
    private boolean mFinished;

    // sleep for 5 minutes
    private long mthreadSleepTime = 1000;// 1000 * 60 * 5;

    public NetworkAnnounceThread() {
        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;
    }


    @Override
    public void run() {
        while (!mFinished) {
            try {
                NetworkTransmissionUtilities.sendTextToAllClients("");
                Thread.sleep(mthreadSleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (mPauseLock) {
                while (mPaused) {
                    try {
                        mPauseLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    /**
     * Call this on pause.
     */
    public void onPause() {
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }
}
