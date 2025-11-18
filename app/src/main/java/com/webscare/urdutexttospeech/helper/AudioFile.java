package com.webscare.urdutexttospeech.helper;

public class AudioFile {
    private String fileName;
    private String filePath;
    private boolean isRenaming = false;

    public AudioFile(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }

    public boolean isRenaming() { return isRenaming; }
    public void setRenaming(boolean renaming) { isRenaming = renaming; }
}
