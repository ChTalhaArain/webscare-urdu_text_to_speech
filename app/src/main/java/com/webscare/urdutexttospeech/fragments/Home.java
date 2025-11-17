package com.webscare.urdutexttospeech.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import com.webscare.urdutexttospeech.R;
import com.webscare.urdutexttospeech.databinding.FragmentHomeBinding;
import com.webscare.urdutexttospeech.databinding.PopupVoiceSettingsBinding;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;

public class Home extends Fragment {

    FragmentHomeBinding ui;

    private final Deque<String> undoStack = new ArrayDeque<>();
    private final Deque<String> redoStack = new ArrayDeque<>();
    private boolean isUndoOrRedo = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ui = FragmentHomeBinding.inflate(inflater, container, false);
        return ui.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateButtonStates();
        setupClickListeners();
        setupTextWatcher();
        setupVoicePopup();
    }

    private void setupClickListeners() {
        ui.deleteButton.setOnClickListener(v -> {
            ui.inputText.setText("");
            undoStack.clear();
            redoStack.clear();
            updateButtonStates();
            Toast.makeText(getContext(), "Text cleared", Toast.LENGTH_SHORT).show();
        });

        ui.copyButton.setOnClickListener(v -> {
            String textToCopy = ui.inputText.getText().toString().trim();
            if (!textToCopy.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("UrduText", textToCopy);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Nothing to copy", Toast.LENGTH_SHORT).show();
            }
        });

        ui.undoButton.setOnClickListener(v -> undo());
        ui.redoButton.setOnClickListener(v -> redo());
    }

    private void setupVoicePopup() {

        final String KEY_GENDER = "selected_gender";
        final String KEY_ACCENT = "selected_accent";

        final int DELAY = 200;

        SharedPreferences prefs = ui.getRoot().getContext().getSharedPreferences("gender_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();


        ui.playButton.setOnLongClickListener(view -> {
            int selectedColor = ContextCompat.getColor(ui.getRoot().getContext(), R.color.voiceMenuOptionsDrawable);

            PopupVoiceSettingsBinding popup = PopupVoiceSettingsBinding.inflate(getLayoutInflater());
            View popupView = popup.getRoot();

            String savedGender = prefs.getString(KEY_GENDER, "");
            String savedAccent = prefs.getString(KEY_ACCENT, "");

            if ("male".equals(savedGender))
                TextViewCompat.setCompoundDrawableTintList(popup.optionMale, ColorStateList.valueOf(selectedColor));
            else if ("female".equals(savedGender))
                TextViewCompat.setCompoundDrawableTintList(popup.optionFemale, ColorStateList.valueOf(selectedColor));


            switch (savedAccent) {
                case "standard":
                    TextViewCompat.setCompoundDrawableTintList(popup.accentStandard, ColorStateList.valueOf(selectedColor));
                    break;
                case "pakistani":
                    TextViewCompat.setCompoundDrawableTintList(popup.accentPakistani, ColorStateList.valueOf(selectedColor));
                    break;
                case "lahori":
                    TextViewCompat.setCompoundDrawableTintList(popup.accentLahori, ColorStateList.valueOf(selectedColor));
                    break;
                case "karachi":
                    TextViewCompat.setCompoundDrawableTintList(popup.accentKarachi, ColorStateList.valueOf(selectedColor));
                    break;
            }

            // set the width equivalent to 220dp

            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 220, ui.getRoot().getContext().getResources().getDisplayMetrics());
            PopupWindow popupWindow = new PopupWindow(popupView, width, LinearLayout.LayoutParams.WRAP_CONTENT, true);

            popupWindow.showAsDropDown(view, 0, 0);

            popup.headerGender.setOnClickListener(v -> {
                boolean isVisible = popup.optionsGender.getVisibility() == View.VISIBLE;
                animateSlide(popup.optionsGender, !isVisible);
                popup.headerGender.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gender_menu, 0, isVisible ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up, 0);

            });

            popup.headerAccent.setOnClickListener(v -> {
                boolean isVisible = popup.optionsAccent.getVisibility() == View.VISIBLE;
                animateSlide(popup.optionsAccent, !isVisible);
                popup.headerAccent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_accent_menu, 0, isVisible ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up, 0);
            });


            // Options handling ...


            Runnable resetGender = () -> {
                TextViewCompat.setCompoundDrawableTintList(popup.optionMale, ColorStateList.valueOf(Color.TRANSPARENT));
                TextViewCompat.setCompoundDrawableTintList(popup.optionFemale, ColorStateList.valueOf(Color.TRANSPARENT));
            };

            Runnable resetAccent = () -> {
                TextViewCompat.setCompoundDrawableTintList(popup.accentStandard, ColorStateList.valueOf(Color.TRANSPARENT));
                TextViewCompat.setCompoundDrawableTintList(popup.accentPakistani, ColorStateList.valueOf(Color.TRANSPARENT));
                TextViewCompat.setCompoundDrawableTintList(popup.accentLahori, ColorStateList.valueOf(Color.TRANSPARENT));
                TextViewCompat.setCompoundDrawableTintList(popup.accentKarachi, ColorStateList.valueOf(Color.TRANSPARENT));
            };

            popup.optionMale.setOnClickListener(v -> {
                resetGender.run();
                TextViewCompat.setCompoundDrawableTintList(popup.optionMale, ColorStateList.valueOf(selectedColor));
                editor.putString(KEY_GENDER, "male").apply();
                new Handler(Looper.getMainLooper()).postDelayed(popupWindow::dismiss, DELAY);
            });

            popup.optionFemale.setOnClickListener(v -> {
                resetGender.run();
                TextViewCompat.setCompoundDrawableTintList(popup.optionFemale, ColorStateList.valueOf(selectedColor));
                editor.putString(KEY_GENDER, "female").apply();
                new Handler(Looper.getMainLooper()).postDelayed(popupWindow::dismiss, DELAY);
            });

            popup.accentStandard.setOnClickListener(v -> {
                resetAccent.run();
                TextViewCompat.setCompoundDrawableTintList(popup.accentStandard, ColorStateList.valueOf(selectedColor));
                editor.putString(KEY_ACCENT, "standard").apply();
                new Handler(Looper.getMainLooper()).postDelayed(popupWindow::dismiss, DELAY);
            });

            popup.accentPakistani.setOnClickListener(v -> {
                resetAccent.run();
                TextViewCompat.setCompoundDrawableTintList(popup.accentPakistani, ColorStateList.valueOf(selectedColor));
                editor.putString(KEY_ACCENT, "pakistani").apply();
                new Handler(Looper.getMainLooper()).postDelayed(popupWindow::dismiss, DELAY);
            });

            popup.accentLahori.setOnClickListener(v -> {
                resetAccent.run();
                TextViewCompat.setCompoundDrawableTintList(popup.accentLahori, ColorStateList.valueOf(selectedColor));
                editor.putString(KEY_ACCENT, "lahori").apply();
                new Handler(Looper.getMainLooper()).postDelayed(popupWindow::dismiss, DELAY);
            });

            popup.accentKarachi.setOnClickListener(v -> {
                resetAccent.run();
                TextViewCompat.setCompoundDrawableTintList(popup.accentKarachi, ColorStateList.valueOf(selectedColor));
                editor.putString(KEY_ACCENT, "karachi").apply();
                new Handler(Looper.getMainLooper()).postDelayed(popupWindow::dismiss, DELAY);
            });

            return true;
        });
    }

    private void animateSlide(final View view, boolean expand) {
        int startHeight = expand ? 0 : view.getHeight();

        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int endHeight = expand ? view.getMeasuredHeight() : 0;

        if (expand) view.setVisibility(View.VISIBLE);

        ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
        animator.setDuration(200);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = value;
            view.setLayoutParams(layoutParams);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!expand) view.setVisibility(View.GONE);
            }
        });

        animator.start();
    }

    private void setupTextWatcher() {
        ui.inputText.addTextChangedListener(new TextWatcher() {
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
            redoStack.push(ui.inputText.getText().toString());
            ui.inputText.setText(lastState);
            ui.inputText.setSelection(lastState.length());
            isUndoOrRedo = false;
            updateButtonStates();
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            isUndoOrRedo = true;
            String nextState = redoStack.pop();
            undoStack.push(ui.inputText.getText().toString());
            ui.inputText.setText(nextState);
            ui.inputText.setSelection(nextState.length());
            isUndoOrRedo = false;
            updateButtonStates();
        }
    }

    private void updateButtonStates() {
        ui.undoButton.setEnabled(!undoStack.isEmpty());
        ui.redoButton.setEnabled(!redoStack.isEmpty());
        ui.undoButton.setAlpha(undoStack.isEmpty() ? 0.5f : 1.0f);
        ui.redoButton.setAlpha(redoStack.isEmpty() ? 0.5f : 1.0f);
    }

}
