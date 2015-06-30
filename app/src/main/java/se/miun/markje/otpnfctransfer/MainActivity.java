package se.miun.markje.otpnfctransfer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;


public class MainActivity extends Activity {

    private static final int FILE_SIZE = 10000;
    private Switch fileGenerationSwitch;
    private CheckBox sendActiveCheckbox;
    private TextView statusTextView;

    private BeamFileTransfer beamFileTransfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        statusTextView = (TextView) findViewById(R.id.statusTextView);

        sendActiveCheckbox = (CheckBox) findViewById(R.id.nfcTransferActive);
        sendActiveCheckbox.setOnCheckedChangeListener(new SendstateChangedListener());

        fileGenerationSwitch = (Switch) findViewById(R.id.fileGenerationSwitch);
        fileGenerationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startFileGenerationService();
                } else {
                    stopFileGenerationService();
                }
            }
        });
        beamFileTransfer = new BeamFileTransfer(this);
        onNewIntent(getIntent());
    }

    /**
     * Intents receievd are OTPFileGenerator.ACTION_RELEASE_FILE_LIST_RESULT
     * When files have been generated (stopped by user)
     * and ACTION_VIEW
     * When files are recieved by Beam.
     * @param intent Incoming intent
     */
    @Override
    protected void onNewIntent (Intent intent) {
        super.onNewIntent(intent);
        Log.d(this.getClass().toString(), "Activity " + intent);
        if ( intent.getAction() == null )
            return;

        if( intent.getAction().equals(Intent.ACTION_VIEW)){
            Uri uri = intent.getData();
            setStatus("Beaming");
            beamFileTransfer.getFiles(uri);
            setStatus("Beamed");
            return;
        }

        if( intent.getAction().equals(OTPFileGenerator.ACTION_RELEASE_FILE_LIST_RESULT)){
            ArrayList<String> fileList = intent.getStringArrayListExtra(OTPFileGenerator.RESULT_FILE_LIST);

            setStatus("Created " + fileList.size() + " files");
            beamFileTransfer.addFilesAvailable(fileList);
        }else{
            setStatus("Resumed");
        }
    }

    /**
     * Sets status text in the lower part of the activity.
     * @param status New status
     */
    private void setStatus(CharSequence status) {
        statusTextView.setText(status);
    }

    private void startFileGenerationService() {
        OTPFileGenerator.startActionGenerate(this, OTPFileGenerator.GENERATION_PRIORITY_HIGH, "test", FILE_SIZE);
        setStatus("Generating");
    }

    private void stopFileGenerationService() {
        OTPFileGenerator.startActionStopGenerate(this);
        setStatus("Stopping");
        Intent response = new Intent(this, MainActivity.class);
        OTPFileGenerator.releaseFileList(this, response);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class SendstateChangedListener implements CompoundButton.OnCheckedChangeListener {

        /**
         * Called when the checked state of sendbutton (on/off) changes
         *
         * @param buttonView The compound button view whose state has changed. Ignored
         * @param isChecked  The new checked state of buttonView.
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                beamFileTransfer.setState(BeamFileTransferState.READ_WRITE);
            }else{
                beamFileTransfer.setState(BeamFileTransferState.READ);
            }
        }
    }
}
