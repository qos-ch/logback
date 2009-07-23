package ch.qos.logback.core.rolling.helper;


public class CompressionRunnable implements Runnable {

  final Compressor compressor;
  final String nameOfFile2Compress;
  final  String nameOfCompressedFile;
  
  public CompressionRunnable(Compressor compressor, String nameOfFile2Compress,
      String nameOfCompressedFile) {
    this.compressor = compressor;
    this.nameOfFile2Compress = nameOfFile2Compress;
    this.nameOfCompressedFile = nameOfCompressedFile;
  }

  public void run() {
    compressor.compress(nameOfFile2Compress, nameOfCompressedFile);
  }
}
