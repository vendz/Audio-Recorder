package com.example.audiorecorder;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class AudioListFragment extends Fragment implements AudioListAdapter.onItemListClick{

    private ConstraintLayout player_sheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private File[] allFiles;
    private RecyclerView audio_list_view;
    private AudioListAdapter audioListAdapter;

    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private File fileToPlay;

    // UI elements
    private ImageButton player_play;
    private ImageButton player_rewind;
    private ImageButton player_forward;
    private SeekBar player_seekbar;
    private TextView player_header_title;
    private TextView player_filename;

    // we will need this to control audio with SeekBar
    private Handler  seekbarHandler;
    private Runnable updateSeekbar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        player_sheet = view.findViewById(R.id.player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(player_sheet);
        audio_list_view = view.findViewById(R.id.audio_list_view);

        // UI elements
        player_filename = view.findViewById(R.id.player_filename);
        player_play = view.findViewById(R.id.player_play);
        player_forward = view.findViewById(R.id.player_forward);
        player_rewind = view.findViewById(R.id.player_rewind);
        player_header_title = view.findViewById(R.id.player_header_title);
        player_seekbar = view.findViewById(R.id.player_seekbar);

        // for displaying in RecyclerView
        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        allFiles = directory.listFiles();

        audioListAdapter = new AudioListAdapter(allFiles, this);

        audio_list_view.setHasFixedSize(true);
        audio_list_view.setLayoutManager(new LinearLayoutManager(getContext()));
        audio_list_view.setAdapter(audioListAdapter);

        // when we drag down the 'player' it used to disappear so code below will fix it
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN){
                    // this will make sure that when the player comes back when 'HIDDEN'
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // we cannot delete this and also there is no use for this code block
            }
        });

        player_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying){
                    pauseAudio();
                    player_play.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_play_foreground, null));
                    player_header_title.setText("Paused");
                    isPlaying = false;
                }
                else {
                    if (fileToPlay != null){
                        resumeAudio();
                        player_play.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_pause_foreground, null));
                        player_header_title.setText("Playing");
                        isPlaying = true;
                    }
                }
            }
        });

        player_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (fileToPlay != null){
                    pauseAudio();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fileToPlay != null){
                    int progress = seekBar.getProgress();
                    mediaPlayer.seekTo(progress);
                    resumeAudio();
                }
            }
        });

    }

    @Override
    public void onClickListener(File file, int position) {
        fileToPlay = file;
        if (isPlaying){
            stopAudio();
            // if an audio is already playing and you select different file then it will start playing that file
            playAudio(fileToPlay);
        }
        else {
            playAudio(fileToPlay);
        }
    }

    private void stopAudio() {
        player_play.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_play_foreground, null));
        player_header_title.setText("Not Playing");
        player_filename.setText("");
        isPlaying = false;
        mediaPlayer.stop();
    }

    private void playAudio(File fileToPlay) {
        mediaPlayer = new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        player_play.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_pause_foreground, null));
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (IOException e){
            e.printStackTrace();
        }
        player_filename.setText(fileToPlay.getName());
        player_header_title.setText("Playing");
        isPlaying = true;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();
                player_header_title.setText("Not Playing");
            }
        });

        player_seekbar.setMax(mediaPlayer.getDuration());
        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);
    }

    private void pauseAudio(){
        mediaPlayer.pause();
        player_play.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_play_foreground, null));
        player_header_title.setText("Playing");
        seekbarHandler.removeCallbacks(updateSeekbar);
        isPlaying = false;
    }

    private void resumeAudio(){
        mediaPlayer.start();
        player_play.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_pause_foreground, null));
        player_header_title.setText("Playing");
        seekbarHandler.removeCallbacks(updateSeekbar);
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);
        isPlaying = false;
    }

    private void updateRunnable(){
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                player_seekbar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this,0);
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        stopAudio();
        if (isPlaying){
            stopAudio();
        }
    }
}