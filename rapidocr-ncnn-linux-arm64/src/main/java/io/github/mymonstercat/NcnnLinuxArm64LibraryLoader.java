package io.github.mymonstercat;

import io.github.mymonstercat.loader.LibraryLoader;

import java.io.IOException;

/**
 * @author Monster
 */
public class NcnnLinuxArm64LibraryLoader implements LibraryLoader {

    @Override
    public void loadLibrary() throws IOException {
        JarFileUtil.copyFileFromJar("lib/libRapidOcr.so", "/ncnn", true, false);
    }
}
