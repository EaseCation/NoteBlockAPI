package com.xxmicloxx.NoteBlockAPI.runnable;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import com.xxmicloxx.NoteBlockAPI.player.SongPlayer;

/**
 * Created by funcraft on 2016/1/25.
 */
public class TickerRunnable implements Runnable {
    @Override
    public void run() {
        while (NoteBlockAPI.getInstance().isEnabled()) {
            long start = System.currentTimeMillis();
            try {
                NoteBlockAPI.getInstance().playingSongs.forEach((s, a) -> a.forEach((SongPlayer::tryPlay)));
            } catch (Exception e) {
                //ignore
            }
            long time = System.currentTimeMillis() - start;
            if (time < 10) {
                try {
                    Thread.sleep(10 - time);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
    }
}
