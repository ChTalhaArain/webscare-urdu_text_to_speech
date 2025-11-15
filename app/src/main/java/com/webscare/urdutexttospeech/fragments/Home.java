package com.webscare.urdutexttospeech.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.webscare.urdutexttospeech.R;

import java.util.ArrayDeque;
import java.util.Deque;

public class Home extends Fragment {

    private EditText inputText;
    private ImageView deleteButton, undoButton, redoButton, copyButton;

    private final Deque<String> undoStack = new ArrayDeque<>();
    private final Deque<String> redoStack = new ArrayDeque<>();
    private boolean isUndoOrRedo = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        inputText = view.findViewById(R.id.inputText);
        deleteButton = view.findViewById(R.id.deleteButton);
        undoButton = view.findViewById(R.id.undoButton);
        redoButton = view.findViewById(R.id.redoButton);
        copyButton = view.findViewById(R.id.copyButton);

        // Set initial button states
        updateButtonStates();

        // Set listeners
        setupClickListeners();
        setupTextWatcher();
    }

    private void setupClickListeners() {
        deleteButton.setOnClickListener(v -> {
            inputText.setText("");
            undoStack.clear();
            redoStack.clear();
            updateButtonStates();
            Toast.makeText(getContext(), "Text cleared", Toast.LENGTH_SHORT).show();
        });

        copyButton.setOnClickListener(v -> {
            String textToCopy = inputText.getText().toString().trim();
            if (!textToCopy.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("UrduText", textToCopy);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Nothing to copy", Toast.LENGTH_SHORT).show();
            }
        });

        undoButton.setOnClickListener(v -> undo());
        redoButton.setOnClickListener(v -> redo());
    }

    private void setupTextWatcher() {
        inputText.addTextChangedListener(new TextWatcher() {
            private String beforeText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isUndoOrRedo) {
                    beforeText = s.toString();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed for this implementation
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUndoOrRedo) {
                    String afterText = s.toString();
                    if (!afterText.equals(beforeText)) {
                        undoStack.push(beforeText);
                        redoStack.clear();
                        updateButtonStates();
                    }
                }
            }
        });
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            isUndoOrRedo = true;
            String lastState = undoStack.pop();
            redoStack.push(inputText.getText().toString());
            inputText.setText(lastState);
            inputText.setSelection(lastState.length());
            isUndoOrRedo = false;
            updateButtonStates();
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            isUndoOrRedo = true;
            String nextState = redoStack.pop();
            undoStack.push(inputText.getText().toString());
            inputText.setText(nextState);
            inputText.setSelection(nextState.length());
            isUndoOrRedo = false;
            updateButtonStates();
        }
    }

    private void updateButtonStates() {
        undoButton.setEnabled(!undoStack.isEmpty());
        redoButton.setEnabled(!redoStack.isEmpty());
        undoButton.setAlpha(undoStack.isEmpty() ? 0.5f : 1.0f);
        redoButton.setAlpha(redoStack.isEmpty() ? 0.5f : 1.0f);
    }
}
