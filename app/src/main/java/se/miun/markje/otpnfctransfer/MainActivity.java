package se.miun.markje.otpnfctransfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends Activity {

    private Switch fileGenerationSwitch;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        statusTextView = (TextView) findViewById(R.id.statusTextView);
        fileGenerationSwitch = (Switch) findViewById(R.id.fileGenerationSwitch);
        fileGenerationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    startFileGenerationService();
                }else{
                    stopFileGenerationService();
                }
            }
        });
        Log.d(this.getClass().toString(), "onCreate");

    }
    @Override
    protected void onNewIntent (Intent intent) {
        super.onNewIntent(intent);
        Log.d(this.getClass().toString(), "onResume");

        if( intent.getAction() != null && intent.getAction().equals(OTPFileGenerator.ACTION_RELEASE_FILE_LIST_RESULT)){
            ArrayList<CharSequence> list = intent.getCharSequenceArrayListExtra(OTPFileGenerator.RESULT_FILE_LIST);

            setStatus("Created " + list.size() + " files");
        }else{
            setStatus("Resumed");
        }
    }

    private void setStatus(CharSequence status) {
        statusTextView.setText(status);
    }

    private void startFileGenerationService() {
        OTPFileGenerator.startActionGenerate(this, OTPFileGenerator.GENERATION_PRIORITY_HIGH, "test", 10000);
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
}
