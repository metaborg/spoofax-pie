package mb.log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

public class LoggingOutputStream extends OutputStream {
    private static final int initialBufferLength = 2048;

    private final Logger logger;
    private final Level level;
    private final Pattern[] excludePatterns;

    private boolean closed = false;
    private byte[] buffer;
    private int count;


    public LoggingOutputStream(Logger logger, Level level, String... excludePatterns) throws IllegalArgumentException {
        this.level = level;
        this.logger = logger;
        this.excludePatterns = new Pattern[excludePatterns.length];
        for(int i = 0; i < excludePatterns.length; ++i) {
            this.excludePatterns[i] = Pattern.compile(excludePatterns[i], Pattern.DOTALL);
        }

        buffer = new byte[initialBufferLength];
        count = 0;
    }


    @Override public void close() {
        flush();
        closed = true;
    }

    @Override public void write(final int b) throws IOException {
        if(closed) {
            throw new IOException("The stream has been closed");
        }

        switch(b) {
            case '\n':
                // Flush if writing last line separator.
                doFlush();
                return;
            case '\r':
            case 0:
                // Do not log carriage return and nulls.
                return;
        }

        // Grow buffer if it is full.
        if(count == buffer.length) {
            final int newBufLength = buffer.length + initialBufferLength;
            final byte[] newBuf = new byte[newBufLength];
            System.arraycopy(buffer, 0, newBuf, 0, buffer.length);
            buffer = newBuf;
        }

        buffer[count] = (byte) b;
        count++;
    }

    @Override public void flush() {
        // Never manually flush, always require a \n to be written.
    }

    private void doFlush() {
        try {
            final String message = new String(buffer, 0, count);
            for(Pattern pattern : excludePatterns) {
                if(pattern.matcher(message).matches()) {
                    return;
                }
            }
            logger.log(level, message);
        } finally {
            // Not resetting the buffer; assuming that if it grew that it will likely grow similarly again.
            count = 0;
        }
    }
}
