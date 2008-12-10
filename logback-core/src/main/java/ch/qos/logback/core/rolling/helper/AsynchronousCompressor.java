package ch.qos.logback.core.rolling.helper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsynchronousCompressor {
  Compressor compressor;
  
  public AsynchronousCompressor(Compressor compressor) {
    this.compressor = compressor;
  }
  
  public Future<?> compressAsynchronously() {
    ExecutorService executor = Executors.newScheduledThreadPool(1);
    Future<?> future = executor.submit(new CompressionRunnable(compressor));
    executor.shutdown();
    return future;
  }
  
}
