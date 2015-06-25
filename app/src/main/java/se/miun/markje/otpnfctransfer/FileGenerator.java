package se.miun.markje.otpnfctransfer;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * Created by martin on 2015-06-24.
 *
 * Append generates a file with a prefix + 5 digit serial value.
 * E.g. prefix = "aa" second file generated will be "aa00002"
 */
public class FileGenerator {

    public static String source = "/dev/random";
    public static final String APP_DIRECTORY = "otp_data";

    public static String EXTERNAL_DIRECTORY = "";

    private String prefix;
    private int fileSize;
    private int fileCount;
    private SecureRandom sr;

    public FileGenerator(String prefix, int fileSize) {
        sr = new SecureRandom();
        this.prefix = prefix;
        this.fileSize = fileSize;
        fileCount = 0;
    }

    /**
     * Increments file counter and returns the filename.
     * @return filename
     */
    private String newFileName(){
        fileCount++;
        String serial = String.format("%03d", fileCount);
        return prefix + serial;
    }

    public String generate() throws IOException {
        FileReader reader = null;
        try {
            reader = new FileReader(source);
        } catch (FileNotFoundException fnf){
            throw new FileNotFoundException("Can't find " + source);
        }
        // Output filename and path
        String fileName = newFileName();

        //File otpDirectory = new File(Environment.getExternalStorageDirectory(), APP_DIRECTORY);
        File otpDirectory = new File(EXTERNAL_DIRECTORY, APP_DIRECTORY);
        otpDirectory.mkdirs();
        File file = new File(otpDirectory, fileName);
        Log.d(this.getClass().toString(), "Filename: " + file.getAbsolutePath());
        FileOutputStream writer = new FileOutputStream(file);

        write(reader, writer);
        reader.close();
        writer.close();
        return fileName;
    }

    private void write(FileReader reader, FileOutputStream writer) throws IOException {
        byte[] output = new byte[fileSize];
        sr.nextBytes(output);
        writer.write(output);
    }

}
