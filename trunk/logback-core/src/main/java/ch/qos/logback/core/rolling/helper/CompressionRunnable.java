package ch.qos.logback.core.rolling.helper;


public class CompressionRunnable implements Runnable {

  final Compressor compressor;
  public CompressionRunnable(Compressor compressor) {
    this.compressor = compressor;
  }

  public void run() {
    compressor.compress();
  }
}
