package com.webscare.urdutexttospeech.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.webscare.urdutexttospeech.databinding.FragmentLocalFilesBinding;
import com.webscare.urdutexttospeech.helper.AudioFile;
import com.webscare.urdutexttospeech.helper.AudioFilesAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalFiles extends Fragment {

    private FragmentLocalFilesBinding ui;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ui = FragmentLocalFilesBinding.inflate(inflater, container, false);
        return ui.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateRecyclerView();
    }

    private void populateRecyclerView() {
        List<AudioFile> audioFiles = loadAudioFiles();

        if (audioFiles.isEmpty()) {
            ui.recyclerFiles.setVisibility(View.GONE);
            ui.placeholder.setVisibility(View.VISIBLE);
        } else {
            ui.placeholder.setVisibility(View.GONE);
            ui.recyclerFiles.setVisibility(View.VISIBLE);

            AudioFilesAdapter adapter = new AudioFilesAdapter(requireContext(), audioFiles);
            ui.recyclerFiles.setLayoutManager(new LinearLayoutManager(requireContext()));
            ui.recyclerFiles.setAdapter(adapter);
        }
    }

    private List<AudioFile> loadAudioFiles() {
        List<AudioFile> list = new ArrayList<>();
        File dir = requireContext().getExternalFilesDir("Audio");

        if (dir == null) {
            return list;
        }

        File[] files = dir.listFiles();

        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(".mp3")) {
                    list.add(new AudioFile(f.getName(), f.getAbsolutePath()));
                }
            }
        }

        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ui = null;
    }
}
