package global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by furszy on 6/4/17.
 */

public interface ContextWrapper {

    FileOutputStream openFileOutputPrivateMode(String name) throws FileNotFoundException;

    FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException;

    FileInputStream openFileInput(String name) throws FileNotFoundException;

    File getFileStreamPath(String name);

    File getDir(String name, int mode);

    File getDirPrivateMode(String name);

    InputStream openAssestsStream(String name) throws IOException;

}
