package dev.sbs.api.scheduler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A handle for a task submitted to a {@link Scheduler}, wrapping a {@link ScheduledFuture}
 * and exposing lifecycle state (running, repeating, done, cancelled).
 * <p>
 * Each task is assigned a monotonically increasing {@linkplain #getId() id} at creation time.
 * A task may be <em>one-shot</em> (executes once after an initial delay) or <em>repeating</em>
 * (re-executes with a fixed delay between the end of one execution and the start of the next).
 * Repeating tasks use {@link ScheduledExecutorService#scheduleWithFixedDelay} so that long-running
 * executions do not cause catch-up bursts.
 * <p>
 * Execution errors are caught, logged, and tracked via {@link #getConsecutiveErrors()}; the
 * counter resets to zero after every successful execution.
 *
 * @see Scheduler
 */
@Getter
@Log4j2
public final class ScheduledTask implements Runnable {

    /** Global counter used to assign a unique id to every {@code ScheduledTask}. */
    private static final AtomicLong currentId = new AtomicLong(1);

    /** Epoch millisecond timestamp recorded when this task was created. */
    private final long addedTime = System.currentTimeMillis();

    /** Unique identifier for this task, assigned from {@link #currentId}. */
    private final long id;

    /** The delay before the first execution, expressed in {@link #timeUnit}. */
    private final long initialDelay;

    /**
     * The delay between the end of one execution and the start of the next,
     * expressed in {@link #timeUnit}. A value of {@code 0} indicates a one-shot task.
     */
    private final long period;

    /** The time unit for both {@link #initialDelay} and {@link #period}. */
    private final @NotNull TimeUnit timeUnit;

    /** {@code true} while the task's {@link Runnable} is actively executing. */
    private volatile boolean running;

    /** {@code true} if this task was scheduled with a positive {@link #period}. */
    private volatile boolean repeating;

    /**
     * Rolling count of consecutive execution failures. Reset to zero after each
     * successful execution; incremented on each caught exception.
     */
    private AtomicInteger consecutiveErrors = new AtomicInteger(0);

    @Getter(AccessLevel.NONE)
    private final @NotNull Runnable runnableTask;

    @Getter(AccessLevel.NONE)
    private final @NotNull ScheduledFuture<?> scheduledFuture;

    /**
     * Creates and immediately schedules a new task on the given executor.
     * <p>
     * If {@code period} is greater than zero the task repeats using
     * {@link ScheduledExecutorService#scheduleWithFixedDelay}; otherwise it is
     * scheduled as a one-shot via {@link ScheduledExecutorService#schedule}.
     *
     * @param executorService the executor that will run this task
     * @param task            the work to execute
     * @param initialDelay    the delay before the first execution
     * @param period          the delay between end-of-execution and the next start ({@code 0} for one-shot)
     * @param timeUnit        the time unit for {@code initialDelay} and {@code period}
     */
    ScheduledTask(
        @NotNull ScheduledExecutorService executorService,
        @NotNull final Runnable task,
        @Range(from = 0, to = Long.MAX_VALUE) long initialDelay,
        @Range(from = 0, to = Long.MAX_VALUE) long period,
        @NotNull TimeUnit timeUnit
    ) {
        this.id = currentId.getAndIncrement();
        this.runnableTask = task;
        this.initialDelay = initialDelay;
        this.period = period;
        this.timeUnit = timeUnit;
        this.repeating = this.period > 0;

        // Schedule Task
        if (this.isRepeating())
            this.scheduledFuture = executorService.scheduleWithFixedDelay(this, initialDelay, period, timeUnit);
        else
            this.scheduledFuture = executorService.schedule(this, initialDelay, timeUnit);
    }

    /**
     * Attempts to cancel this task without interrupting a running execution.
     * <p>
     * Equivalent to {@code cancel(false)}.
     *
     * @see #cancel(boolean)
     */
    public void cancel() {
        this.cancel(false);
    }

    /**
     * Attempts to cancel this task, optionally interrupting a running execution.
     * <p>
     * If the underlying {@link ScheduledFuture#cancel(boolean)} call succeeds, the
     * {@link #repeating} and {@link #running} flags are cleared. If cancellation fails
     * (e.g. the task already completed), the flags are left unchanged.
     *
     * @param mayInterruptIfRunning {@code true} to interrupt the executing thread;
     *                              {@code false} to allow in-progress execution to finish
     */
    public void cancel(boolean mayInterruptIfRunning) {
        if (this.scheduledFuture.cancel(mayInterruptIfRunning)) {
            this.repeating = false;
            this.running = false;
        }
    }

    /**
     * Returns whether this task has completed, either normally, via cancellation, or
     * due to an exception (for one-shot tasks).
     *
     * @return {@code true} if the underlying future is done
     */
    public boolean isDone() {
        return this.scheduledFuture.isDone();
    }

    /**
     * Returns whether this task was cancelled before it completed normally.
     *
     * @return {@code true} if the underlying future was cancelled
     * @see #cancel(boolean)
     */
    public boolean isCanceled() {
        return this.scheduledFuture.isCancelled();
    }

    /**
     * Executes the wrapped {@link Runnable}, tracking the {@link #running} state and
     * logging any exceptions. On success the {@link #consecutiveErrors} counter is
     * reset to zero; on failure it is incremented.
     */
    @Override
    public void run() {
        try {
            // Run Task
            this.running = true;
            this.runnableTask.run();
            this.consecutiveErrors.set(0);
        } catch (Exception ex) {
            this.consecutiveErrors.incrementAndGet();
            log.error("Task {} failed: {}", this.id, ex.getMessage(), ex);
        } finally {
            this.running = false;
        }
    }

}
