package global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by furszy on 6/4/17.
 */

public interface ContextWrapper {

    FileOutputStream openFileOutputPrivateMode(String name) throws IOException;

    FileOutputStream openFileOutput(String name, int mode) throws IOException;

    FileInputStream openFileInput(String name) throws IOException;

    File getFileStreamPath(String name) throws IOException;

    File getDir(String name, int mode);

    File getDirPrivateMode(String name);

    InputStream openAssestsStream(String name) throws IOException;

    String getPackageName();

    boolean isMemoryLow();

    String getVersionName();

    void stopBlockchain();
}
