package com.webscare.urdutexttospeech.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.webscare.urdutexttospeech.R;
import com.webscare.urdutexttospeech.databinding.PopupFileMenuBinding;

import java.io.File;
import java.util.List;

public class AudioFilesAdapter extends RecyclerView.Adapter<AudioFilesAdapter.AudioViewHolder> {

    private final List<AudioFile> audioFiles;
    private final Context context;

    public AudioFilesAdapter(Context context, List<AudioFile> audioFiles) {
        this.context = context;
        this.audioFiles = audioFiles;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.audio_file_layout, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        AudioFile file = audioFiles.get(position);

        String fileName = file.getFileName();
        String fileNameWithoutExt = fileName.endsWith(".mp3") ? fileName.substring(0, fileName.length() - 4) : fileName;

        holder.tvFileName.setText(fileName);
        holder.etFileName.setText(fileNameWithoutExt);

        // Reset listeners to avoid conflicts from recycled views
        holder.etFileName.setOnEditorActionListener(null);
        holder.cbMenu.setOnClickListener(null);

        if (file.isRenaming()) {
            holder.tvFileName.setVisibility(View.GONE);
            holder.ivMenu.setVisibility(View.GONE);

            holder.etFileName.setVisibility(View.VISIBLE);
            holder.cbMenu.setVisibility(View.VISIBLE); // This is now the cancel button

            holder.etFileName.requestFocus();
            holder.etFileName.setSelection(holder.etFileName.length());
            showKeyboard(holder.etFileName);

            holder.etFileName.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    renameFile(file, holder.etFileName, position);
                    hideKeyboard(v);
                    return true;
                }
                return false;
            });

            holder.cbMenu.setOnClickListener(v -> {
                file.setRenaming(false);
                notifyItemChanged(position);
                hideKeyboard(v);
            });
        } else {
            holder.tvFileName.setVisibility(View.VISIBLE);
            holder.ivMenu.setVisibility(View.VISIBLE);

            holder.etFileName.setVisibility(View.GONE);
            holder.cbMenu.setVisibility(View.GONE);
        }

        holder.ivMenu.setOnClickListener(v -> {
            PopupFileMenuBinding popupBinding = PopupFileMenuBinding.inflate(LayoutInflater.from(context));
            View popupView = popupBinding.getRoot();

            PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

            popupBinding.actionPlay.setOnClickListener(view -> {
                Toast.makeText(context, "Play clicked", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });

            popupBinding.actionRename.setOnClickListener(view -> {
                file.setRenaming(true);
                notifyItemChanged(position);
                popupWindow.dismiss();
            });

            popupBinding.actionShare.setOnClickListener(view -> {
                File fileToShare = new File(file.getFilePath());
                if (fileToShare.exists()) {
                    Uri contentUri = FileProvider.getUriForFile(context, "com.webscare.urdutexttospeech.provider", fileToShare);

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("audio/mpeg");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    Intent chooser = Intent.createChooser(shareIntent, "Share Audio File");
                    context.startActivity(chooser);

                } else {
                    Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
                }
                popupWindow.dismiss();
            });

            popupBinding.actionRemove.setOnClickListener(view -> {
                File fileToDelete = new File(file.getFilePath());
                if (fileToDelete.exists()) {
                    if (fileToDelete.delete()) {
                        audioFiles.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, audioFiles.size());
                        Toast.makeText(context, "File removed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to remove file", Toast.LENGTH_SHORT).show();
                    }
                }
                popupWindow.dismiss();
            });

            popupWindow.showAsDropDown(holder.ivMenu);
        });
    }

    private void renameFile(AudioFile file, EditText editText, int position) {
        String newName = editText.getText().toString().trim();

        if (!newName.isEmpty()) {
            File oldFile = new File(file.getFilePath());
            File newFile = new File(oldFile.getParent(), newName + ".mp3");

            if (oldFile.exists()) {
                if(oldFile.equals(newFile)) {
                    // Name is the same, just exit rename mode
                } else if (newFile.exists()) {
                    Toast.makeText(context, "A file with this name already exists", Toast.LENGTH_SHORT).show();
                    return; // Don't exit rename mode
                } else {
                    boolean renamed = oldFile.renameTo(newFile);
                    if (renamed) {
                        file.setFileName(newName + ".mp3");
                        filePathUpdate(file, newFile.getAbsolutePath());
                    } else {
                        Toast.makeText(context, "Rename failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        file.setRenaming(false);
        notifyItemChanged(position);
    }

    private void filePathUpdate(AudioFile file, String newPath) {
        try {
            java.lang.reflect.Field pathField = AudioFile.class.getDeclaredField("filePath");
            pathField.setAccessible(true);
            pathField.set(file, newPath);
        } catch (Exception ignored) {}
    }

    private void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public int getItemCount() {
        return audioFiles.size();
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPlaceholder, ivMenu, cbMenu;
        TextView tvFileName;
        EditText etFileName;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPlaceholder = itemView.findViewById(R.id.ivPlaceholder);
            tvFileName   = itemView.findViewById(R.id.tvFileName);
            etFileName   = itemView.findViewById(R.id.etFileName);
            ivMenu       = itemView.findViewById(R.id.ivMenu);
            cbMenu       = itemView.findViewById(R.id.cbMenu);
        }
    }
}
