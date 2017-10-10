package org.pivtrum.imp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import global.ContextWrapper;

/**
 * Created by furszy on 6/15/17.
 */

public class ContextWrapperImp implements ContextWrapper {


    @Override
    public FileOutputStream openFileOutputPrivateMode(String name) throws IOException {
        return new FileOutputStream(initFile(new File("test/"+name)));
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws IOException {
        return openFileOutputPrivateMode(name);
    }

    @Override
    public FileInputStream openFileInput(String name) throws IOException {
        return new FileInputStream(initFile(new File("test/"+name)));
    }

    @Override
    public File getFileStreamPath(String name) throws IOException {
        File file = new File("test/"+name);
        file.getParentFile().mkdirs();
        return file;
    }

    @Override
    public File getDir(String name, int mode) {
        return null;
    }

    @Override
    public File getDirPrivateMode(String name) {
        return null;
    }

    @Override
    public InputStream openAssestsStream(String name) throws IOException {
        return null;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public boolean isMemoryLow() {
        return false;
    }

    @Override
    public String getVersionName() {
        return null;
    }

    @Override
    public void stopBlockchain() {

    }

    private File initFile(File file) throws IOException {
        if (!file.exists()){
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
    }
}
