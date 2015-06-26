package se.miun.markje.otpnfctransfer;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 2015-06-26.
 *
 * The first file sent should be a device/user id. Currently it is just data.
 */
public class BeamFileTransfer {

    public static int MAX_FILES_TO_SEND = 100;
    private Activity activity;

    public static class NoNFCAdapterException extends RuntimeException{
        public NoNFCAdapterException(){
        }
    }

    private NfcAdapter nfcAdapter;

    private BeamFileTransferState state;

    // This is not the java.net.URI class. But android.net.Uri
    // This should be stored in a more permanent fashion.
    // Good enough for testing
    private List<Uri> uriList;
    private FileReciever fileReciever;
    /**
     * Default state is read mode active.
     *
     * @param activity Main activity
     */
    public BeamFileTransfer(Activity activity){
        state = BeamFileTransferState.READ;
        uriList = new ArrayList<>();
        this.activity = activity;
        fileReciever = new FileReciever();

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if ( nfcAdapter == null ) {
            throw new NoNFCAdapterException();
        }
    }

    public void setState(BeamFileTransferState newState){
        state = newState;
        if (state == BeamFileTransferState.WRITE ||
                state == BeamFileTransferState.READ_WRITE) {
            nfcAdapter.setBeamPushUrisCallback(new FileUrisCallback(),
                    activity);
        }else{
            nfcAdapter.setBeamPushUrisCallback(null, activity);
        }
    }

    public BeamFileTransferState getState(){
        return state;
    }

    public void addFilesAvailable(List<String> fileList){
        for(String fileName:fileList){
            File file = new File(fileName);
            uriList.add(Uri.fromFile(file));
        }
    }

    public void getFiles(Uri content){
        // These are the recieved files. No idea what to do with them yet
        Log.d(this.getClass().toString(), content.toString());
        fileReciever.storeFiles(content);
    }

    private class FileUrisCallback implements NfcAdapter.CreateBeamUrisCallback
    {
        @Override
        public Uri[] createBeamUris(NfcEvent event) {
            int arraySize = Math.max(uriList.size(), MAX_FILES_TO_SEND);
            Uri[] uriArray = new Uri[arraySize];
            for (int i = 0; i < arraySize; i++) {
                uriArray[i] = uriList.remove(0);
            }
            return uriArray;
        }
    }
}
