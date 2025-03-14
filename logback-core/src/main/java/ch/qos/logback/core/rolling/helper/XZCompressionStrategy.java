package ch.qos.logback.core.rolling.helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class XZCompressionStrategy extends CompressionStrategyBase {

    @Override
    public void compress(String nameOfFile2xz, String nameOfxzedFile, String innerEntryName) {
        File file2xz = new File(nameOfFile2xz);

        if (!file2xz.exists()) {
            addWarn("The file to compress named [" + nameOfFile2xz + "] does not exist.");

            return;
        }

        if (!nameOfxzedFile.endsWith(".xz")) {
            nameOfxzedFile = nameOfxzedFile + ".xz";
        }

        File xzedFile = new File(nameOfxzedFile);

        if (xzedFile.exists()) {
            addWarn("The target compressed file named [" + nameOfxzedFile + "] exist already. Aborting file compression.");
            return;
        }

        addInfo("XZ compressing [" + file2xz + "] as [" + xzedFile + "]");
        createMissingTargetDirsIfNecessary(xzedFile);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(nameOfFile2xz));
                        XZOutputStream xzos = new XZOutputStream(new FileOutputStream(nameOfxzedFile), new LZMA2Options())) {

            byte[] inbuf = new byte[BUFFER_SIZE];
            int n;

            while ((n = bis.read(inbuf)) != -1) {
                xzos.write(inbuf, 0, n);
            }
        } catch (Exception e) {
            addError("Error occurred while compressing [" + nameOfFile2xz + "] into [" + nameOfxzedFile + "].", e);
        }

        if (!file2xz.delete()) {
            addWarn("Could not delete [" + nameOfFile2xz + "].");
        }
    }
}
