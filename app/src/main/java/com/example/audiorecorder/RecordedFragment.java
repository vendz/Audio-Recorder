package com.example.audiorecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

/**
 * Use the {@link Fragment} subclass
 */
public class RecordedFragment extends Fragment {

    private NavController navController;
    private ImageButton listBtn;
    private ImageButton record_btn;
    private boolean isRecording = false;
    private Chronometer record_timer;
    private TextView record_filename;

    // Fields for permission access
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;

    // We will need a media recorder to record audio
    private MediaRecorder mediaRecorder;

    // variable for file location of recorded file
    private String recordFile;

    public RecordedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recorded, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        listBtn = view.findViewById(R.id.list_btn);
        record_btn = view.findViewById(R.id.record_btn);
        record_timer = view.findViewById(R.id.record_timer);
        record_filename = view.findViewById(R.id.record_filename);

        // onclick listener for 'record button'
        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording){
                    // Stop Recording
                    stopRecording();

                    // changing color of imageView
                    record_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_2, null));
                    isRecording = false;
                }
                else {
                    // Start Recording
                    if (checkPermissions()){

                        // 'startRecording' is needed to be surrounded by 'try-catch' statement
                        try {
                            startRecording();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Changing color of imageView
                        record_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_circle, null));
                        isRecording = true;
                    }
                }
            }
        });


        // onclick listener for 'list button' on right side of record button
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_recordedFragment_to_audioListFragment);
            }
        });
    }

    // Asking for Permission to record Audio
    private boolean checkPermissions(){
        if (ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    public void startRecording() throws IOException {
        record_timer.setBase(SystemClock.elapsedRealtime());
        // timer for chronometer
        record_timer.start();
        //TODO: code starts from line 124. this first few lines are ust for defining variables

        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        // selecting date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA);
        Date now = new Date();
        // we are using date and time in naming of our recording.
        recordFile = "recording_" + formatter.format(now) +".3gp";

        record_filename.setText("Recording, filename: " + recordFile);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        }catch (IOException e){
            e.printStackTrace();
        }

        mediaRecorder.start();
    }

    public void stopRecording(){
        record_filename.setText("Recording stopped, file saved: " + recordFile);
        long pauseOffset = SystemClock.elapsedRealtime() - record_timer.getBase();
        // stopping timer
        record_timer.stop();
        record_timer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
        // stop recording
        mediaRecorder.stop();
        mediaRecorder.release();
        // setting it to null so that we can create a new 'mediaRecorder' when we start again
        mediaRecorder = null;
    }
}