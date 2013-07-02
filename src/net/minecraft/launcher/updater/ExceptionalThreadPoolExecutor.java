package net.minecraft.launcher.updater;

import net.minecraft.launcher.Launcher;

import java.util.concurrent.*;

public class ExceptionalThreadPoolExecutor extends ThreadPoolExecutor {
    public ExceptionalThreadPoolExecutor(int threadCount) {
        super(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
    }

    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        if ((t == null) && ((r instanceof Future)))
            try {
                Future future = (Future) r;
                if (future.isDone())
                    future.get();
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
    }

    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new ExceptionalFutureTask(runnable, value);
    }

    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new ExceptionalFutureTask(callable);
    }

    public class ExceptionalFutureTask<T> extends FutureTask<T> {
        public ExceptionalFutureTask(Callable<T> callable) {
            super(callable);
        }

        public ExceptionalFutureTask(Runnable runnable, T value) {
            super(runnable, value);
        }

        protected void done() {
            try {
                get();
            } catch (Throwable t) {
                Launcher.getInstance().println("Unhandled exception in executor " + this, t);
            }
        }
    }
}


