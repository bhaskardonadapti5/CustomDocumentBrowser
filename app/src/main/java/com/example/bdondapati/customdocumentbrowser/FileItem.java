package com.example.bdondapati.customdocumentbrowser;


import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Created by NKommuri on 8/1/2017.
 */

public class FileItem {
    String fileName, fileExt, filePath;
    boolean isDirectory;
    File file;

    public FileItem(File file) {
        String name = file.getName();
        this.isDirectory = file.isDirectory();
        if (!isDirectory) {
            this.fileName = FilenameUtils.getBaseName(file.getName());
            this.fileExt = FilenameUtils.getExtension(file.getName());

        } else
            this.fileName = name;
        this.filePath = file.getAbsolutePath();
        this.file = file;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return CommonUtils.captilizeWord(fileName);
    }

    public String getFileExt() {
        return fileExt != null ? fileExt.toUpperCase() : fileExt;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public File getFile() {
        return file;
    }
}
