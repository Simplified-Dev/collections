package dev.sbs.api.scheduler;

import dev.sbs.api.util.concurrent.Concurrent;
import dev.sbs.api.util.concurrent.ConcurrentList;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Scheduler {

	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(0);
	private final ConcurrentList<ScheduledTask> tasks = Concurrent.newList();
	private final Object lock = new Object();

	public Scheduler() {
		// Schedule Permanent Cleaner
		new ScheduledTask(this.executorService, () -> this.tasks.forEach(scheduledTask -> {
			if (scheduledTask.isDone())
				this.tasks.remove(scheduledTask);
		}), 1000, 30_000, false);
	}

	public void cancel(int id) {
		this.cancel(id, false);
	}

	public void cancel(int id, boolean mayInterruptIfRunning) {
		this.tasks.forEach(scheduledTask -> {
			if (scheduledTask.getId() == id)
				this.cancel(scheduledTask, mayInterruptIfRunning);
		});
	}

	public void cancel(ScheduledTask task) {
		this.cancel(task, false);
	}

	public void cancel(ScheduledTask task, boolean mayInterruptIfRunning) {
		task.cancel(mayInterruptIfRunning);
	}

	public ConcurrentList<ScheduledTask> getTasks() {
		return Concurrent.newUnmodifiableList(this.tasks);
	}

	/**
	 * Repeats a task (synchronously) every 50 milliseconds.<br><br>
	 *
	 * Warning: This method is run on the main thread, don't do anything heavy.
	 * @param task The task to run.
	 * @return The scheduled task.
	 */
	public ScheduledTask repeat(Runnable task) {
		return this.schedule(task, 0, 50);
	}

	/**
	 * Repeats a task (asynchronously) every 50 milliseconds.
	 *
	 * @param task The task to run.
	 * @return The scheduled task.
	 */
	public ScheduledTask repeatAsync(Runnable task) {
		return this.scheduleAsync(task, 0, 50);
	}

	/**
	 * Runs a task (synchronously).
	 *
	 * @param task The task to run.
	 * @return The scheduled task.
	 */
	public ScheduledTask schedule(Runnable task) {
		return this.schedule(task, 0);
	}

	/**
	 * Runs a task (synchronously).
	 *
	 * @param task The task to run.
	 * @param delay The delay (in milliseconds) to wait before the task runs.
	 * @return The scheduled task.
	 */
	public ScheduledTask schedule(Runnable task, long delay) {
		return this.schedule(task, delay, 0);
	}

	/**
	 * Runs a task (synchronously).
	 *
	 * @param task The task to run.
	 * @param initialDelay The initial delay (in milliseconds) to wait before the task runs.
	 * @param repeatDelay The repeat delay (in milliseconds) to wait before running the task again.
	 * @return The scheduled task.
	 */
	public ScheduledTask schedule(Runnable task, long initialDelay, long repeatDelay) {
		return this.scheduleTask(task, initialDelay, repeatDelay, false);
	}

	/**
	 * Runs a task (asynchronously).
	 *
	 * @param task The task to run.
	 * @return The scheduled task.
	 */
	public ScheduledTask scheduleAsync(Runnable task) {
		return this.scheduleAsync(task, 0);
	}

	/**
	 * Runs a task (asynchronously).
	 *
	 * @param task The task to run.
	 * @param initialDelay The initial delay (in milliseconds) to wait before the task runs.
	 * @return The scheduled task.
	 */
	public ScheduledTask scheduleAsync(Runnable task, long initialDelay) {
		return this.scheduleAsync(task, initialDelay, 0);
	}

	/**
	 * Runs a task (asynchronously).
	 *
	 * @param task The task to run.
	 * @param initialDelay The initial delay (in ticks) to wait before the task runs.
	 * @param repeatDelay The repeat delay (in ticks) to wait before running the task again.
	 * @return The scheduled task.
	 */
	public ScheduledTask scheduleAsync(Runnable task, long initialDelay, long repeatDelay) {
		return this.scheduleTask(task, initialDelay, repeatDelay, true);
	}

	private ScheduledTask scheduleTask(Runnable task, long initialDelay, long repeatDelay, boolean async) {
		synchronized (this.lock) {
			ScheduledTask scheduledTask = new ScheduledTask(this.executorService, task, initialDelay, repeatDelay, async);
			this.tasks.add(scheduledTask);
			return scheduledTask;
		}
	}

	/**
	 * Causes the currently executing thread to sleep (temporarily cease execution) for the specified number of milliseconds, subject to the precision and accuracy of system timers and schedulers. The thread does not lose ownership of any monitors.
	 *
	 * @param millis the length of time to sleep in milliseconds
	 */
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignore) { }
	}

}
