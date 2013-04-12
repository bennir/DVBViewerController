/*
    From tunesremote-plus
    http://code.google.com/p/tunesremote-plus/source/browse/trunk/src/org/tunesremote/util/ThreadExecutor.java

    TunesRemote+ - http://code.google.com/p/tunesremote-plus/

    Copyright (C) 2008 Jeffrey Sharkey, http://jsharkey.org/
    Copyright (C) 2010 TunesRemote+, http://code.google.com/p/tunesremote-plus/

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    The Initial Developer of the Original Code is Jeffrey Sharkey.
    Portions created by Jeffrey Sharkey are
    Copyright (C) 2008. Jeffrey Sharkey, http://jsharkey.org/
    All Rights Reserved.
 */
package de.bennir.DVBViewerController.util;

import android.os.Process;
import android.util.Log;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadPoolExecutor to reduce resources when running threads. Starts with a
 * pool size of 4 and can grow to 50. Uses java.util.Concurrency cleverness to
 * provide the quality pooling and thread safety.
 */
public class ThreadExecutor {
    private static final String TAG = ThreadExecutor.class.toString();
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 50;
    private static final int KEEP_ALIVE = 10;
    private static final BlockingQueue<Runnable> sWorkQueue = new LinkedBlockingQueue<Runnable>(MAXIMUM_POOL_SIZE);

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            final String threadName = "TunesRemote #" + mCount.getAndIncrement();
            Log.d(TAG, String.format("Creating Thread: %s", threadName));
            final Thread thread = new Thread(r, threadName);
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            return thread;
        }
    };
    private static final ThreadPoolExecutor sExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS, sWorkQueue, sThreadFactory);

    public static void runTask(Runnable task) {
        Log.d(TAG, String.format("Run Executor Task = %d", sWorkQueue.size()));
        sExecutor.execute(task);
    }

}