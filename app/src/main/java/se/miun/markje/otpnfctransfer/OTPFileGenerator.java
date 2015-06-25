package se.miun.markje.otpnfctransfer;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class OTPFileGenerator extends Service {

    private static final String ACTION_GENERATE = "se.miun.markje.otpnfctransfer.action.GENERATE";
    private static final String ACTION_STOP_GENERATE = "se.miun.markje.otpnfctransfer.action.STOP_GENERATE";
    private static final String ACTION_RELEASE_FILE_LIST = "se.miun.markje.otpnfctransfer.action.RELEASE_FILE_LIST";
    public static final String ACTION_RELEASE_FILE_LIST_RESULT = "se.miun.markje.otpnfctransfer.action.RELEASE_FILE_LIST_RESULT";

    private static final String EXTRA_GENERATION_PRIORITY = "se.miun.markje.otpnfctransfer.extra.GENERATION_PRIORITY";
    private static final String EXTRA_FILE_PREFIX = "se.miun.markje.otpnfctransfer.extra.FILE_PREFIX";
    private static final String EXTRA_FILE_SIZE = "se.miun.markje.otpnfctransfer.extra.FILE_SIZE";
    private static final String EXTRA_TARGET_INTENT = "se.miun.markje.otpnfctransfer.extra.TARGET_INTENT";

    public static final String RESULT_FILE_LIST = "se.miun.markje.otpnfctransfer.result.RESULT_FILE_LIST";

    public static final String GENERATION_PRIORITY_LOW = "se.miun.markje.otpnfctransfer.extra.GENERATION_PRIORITY_LOW";
    public static final String GENERATION_PRIORITY_HIGH = "se.miun.markje.otpnfctransfer.extra.GENERATION_PRIORITY_HIGH";

    /**
     * Starts this service to generate files containing random numbers.
     *
     * @see IntentService
     */
    public static void startActionGenerate(Context context, String priority, String prefix, int fileSize) {
        Intent intent = new Intent(context, OTPFileGenerator.class);
        intent.setAction(ACTION_GENERATE);
        intent.putExtra(EXTRA_GENERATION_PRIORITY, priority);
        intent.putExtra(EXTRA_FILE_PREFIX, prefix);
        intent.putExtra(EXTRA_FILE_SIZE, fileSize);

        FileGenerator.EXTERNAL_DIRECTORY = context.getExternalFilesDir(null).getAbsolutePath();
        Log.d(context.getClass().toString(), "External :" + FileGenerator.EXTERNAL_DIRECTORY);

        context.startService(intent);
    }

    /**
     * Starts this service to stop generating data.
     *
     * @see IntentService
     */
    public static void startActionStopGenerate(Context context) {
        Intent intent = new Intent(context, OTPFileGenerator.class);
        intent.setAction(ACTION_STOP_GENERATE);
        context.startService(intent);
    }
    public static void releaseFileList(Context context, Intent target) {
        Intent intent = new Intent(context, OTPFileGenerator.class);
        intent.setAction(ACTION_RELEASE_FILE_LIST);
        intent.putExtra(EXTRA_TARGET_INTENT, target);

        context.startService(intent);
    }

    // State stuff
    private boolean generating;

    private ArrayList<String> fileList;

    public OTPFileGenerator() {
        generating = false;
        fileList = new ArrayList<>();
    }

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     * <p/>
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        onHandleIntent(intent);

        return START_STICKY;
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GENERATE.equals(action)) {
                final String priority = intent.getStringExtra(EXTRA_GENERATION_PRIORITY);
                final String prefix = intent.getStringExtra(EXTRA_FILE_PREFIX);
                int fileSize  = intent.getIntExtra(EXTRA_FILE_SIZE, 0);
                handleActionGenerate(priority, prefix, fileSize);
            } else if (ACTION_STOP_GENERATE.equals(action)) {
                handleActionStopGenerate();
            } else if (ACTION_RELEASE_FILE_LIST.equals(action)) {
                handleReleaseFileList((Intent)intent.getParcelableExtra(EXTRA_TARGET_INTENT));
            }
        }
    }

    private void handleReleaseFileList(Intent targetIntent) {
        ArrayList<CharSequence> list = new ArrayList<>();
        for (String name:fileList) {
            list.add(name);
        }
        fileList.clear();
        targetIntent = new Intent(this, MainActivity.class);
        targetIntent.setAction(ACTION_RELEASE_FILE_LIST_RESULT);
        targetIntent.putCharSequenceArrayListExtra(RESULT_FILE_LIST, list);
        targetIntent.putExtra("HELLO", "HELLO");
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(targetIntent);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGenerate(String priority, String prefix, int fileSize) {
        generating = true;
        Thread t = new Thread(new FileGeneratorRunnable(priority, prefix, fileSize));
        t.start();
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStopGenerate() {
        generating = false;
    }

    private class FileGeneratorRunnable implements Runnable {
        private String priority;
        private String prefix;
        private int fileSize;
        public FileGeneratorRunnable(String priority, String prefix, int fileSize) {
            this.priority = priority;
            this.prefix = prefix;
            this.fileSize = fileSize;
        }

        @Override
        public void run() {
            FileGenerator generator = new FileGenerator(prefix, fileSize);
            try {
                while (generating) {
                    String fileName = generator.generate();
                    fileList.add(fileName);
                    Thread.sleep(100);
                }
            }catch (IOException ioe){
                ioe.printStackTrace();
                Log.e(this.getClass().toString(), ioe.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                generating = false;
            }
        }
    }
}
