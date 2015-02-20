package in.kevinj.colonists.client;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;

public class ContinuousRendererUtil {
	private static final float TURNOFF_DELAY_IN_SECONDS = 0.25f;
	public static final ContinuousRendererUtil instance = new ContinuousRendererUtil();

	private final Lock undoTaskLock;
	private final AtomicInteger refCount;
	private final Timer.Task undoTask = new Timer.Task() {
		@Override
		public void run() {
			undoTaskLock.lock();
			try {
				if (System.currentTimeMillis() < getExecuteTimeMillis())
					//canceled in endContinuousRendering()
					return;
				if (refCount.get() != 0)
					//startContinuousRendering() called after we were scheduled
					return;

				assert (undoTaskScheduled && isContinuousRendering && getExecuteTimeMillis() != 0);
				isContinuousRendering = false;
				Gdx.graphics.setContinuousRendering(false);
			} finally {
				undoTaskScheduled = false;
				undoTaskLock.unlock();
			}
		}
	};
	private boolean undoTaskScheduled, isContinuousRendering;

	private ContinuousRendererUtil() {
		undoTaskLock = new ReentrantLock();
		refCount = new AtomicInteger(0);
	}

	public void startContinuousRender() {
		if (refCount.incrementAndGet() == 1) {
			undoTaskLock.lock();
			try {
				if (!isContinuousRendering)
					Gdx.graphics.setContinuousRendering(true);
				isContinuousRendering = true;
			} finally {
				undoTaskLock.unlock();
			}
		}
	}

	public void endContinuousRender() {
		if (refCount.decrementAndGet() == 0) {
			undoTaskLock.lock();
			try {
				if (undoTaskScheduled)
					undoTask.cancel();
				undoTaskScheduled = true;
				Timer.instance().scheduleTask(undoTask, TURNOFF_DELAY_IN_SECONDS);
			} finally {
				undoTaskLock.unlock();
			}
		}
	}

	public void doShortContinuousRender() {
		startContinuousRender();
		endContinuousRender();
	}
}
