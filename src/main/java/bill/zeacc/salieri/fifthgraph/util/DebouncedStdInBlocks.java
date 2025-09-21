package bill.zeacc.salieri.fifthgraph.util ;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public class DebouncedStdInBlocks {

    private final long quietMillis;
    private final BiConsumer<String, Runnable> onBlock; // (block, done) -> { ...; done.run(); }
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "stdin-debounce");
        t.setDaemon(true);
        return t;
    });
    private final Thread reader;

    private final Object lock = new Object();
    private final BufferedReader br;

    private final StringBuilder current = new StringBuilder();
    private final StringBuilder queued = new StringBuilder();

    private ScheduledFuture<?> pending = null;
    private boolean processing = false;
    private volatile boolean running = true;

    public DebouncedStdInBlocks(CliContext ctx, InputStream in, long quietMillis,
                          BiConsumer<String, Runnable> onBlock) {
        this.quietMillis = quietMillis;
        this.onBlock = onBlock;
        this.br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.reader = new Thread(this::readLoop, "stdin-reader");
        this.reader.setDaemon(true);
        ctx.init(this);
    }


	public void start() { reader.start(); }
    public void stop() {
        running = false;
        scheduler.shutdownNow();
        try { reader.join(250); } catch (InterruptedException ignored) {}
    }

    private void readLoop() {
        try {
            String line;
            while (running && (line = br.readLine()) != null) {
                synchronized (lock) {
                    // We only get here AFTER user hits Enter => good place to (re)debounce
                    if (!processing) {
                        current.append(line).append('\n');
                        scheduleDebounce();
                    } else {
                        queued.append(line).append('\n');
                    }
                }
            }
            // EOF: if anything pending and not processing, flush once
            synchronized (lock) { if (!processing && current.length() > 0) flushNow(); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scheduleDebounce() {
        if (pending != null) pending.cancel(false);
        pending = scheduler.schedule(this::flushIfIdle, quietMillis, TimeUnit.MILLISECONDS);
    }

    private void flushIfIdle() {
        synchronized (lock) {
            if (processing || current.length() == 0) return;
            flushNow();
        }
    }

    private void flushNow() {
        final String block = current.toString();
        current.setLength(0);
        processing = true;

        // Provide a 'done' callback that unblocks the next batch safely.
        Runnable done = () -> {
            synchronized (lock) {
                processing = false;
                if (queued.length() > 0) {
                    current.append(queued);
                    queued.setLength(0);
                    scheduleDebounce(); // wait quietMillis after the *last* queued line
                }
            }
        };

        // Run handler off the reader lock
        CompletableFuture.runAsync(() -> onBlock.accept(block, done));
    }

    public static class CliContext {
    	private DebouncedStdInBlocks debouncer ;

    	public CliContext ( ) {
    		;
    	}

    	public void init ( DebouncedStdInBlocks debouncer ) {
			this.debouncer = debouncer ;
		}

    	public void stop ( ) {
			debouncer.stop ( ) ;
		}
    }
}
