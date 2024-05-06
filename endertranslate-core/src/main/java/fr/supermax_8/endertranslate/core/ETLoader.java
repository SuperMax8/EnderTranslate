package fr.supermax_8.endertranslate.core;

import fr.supermax_8.endertranslate.core.jarloader.JarDependency;
import fr.supermax_8.endertranslate.core.jarloader.JarLoader;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ETLoader {

    private static final String[] libsLink = {
            "https://github.com/SuperMax8/EnderTranslate/releases/download/0.1.0/endertranslate-libs-0.1.0.jar"
    };

    public static void loadExternalLibs(File dataFolder) throws IOException {
        EnderTranslate.log("Loading external libs");
        long ts = System.currentTimeMillis();
        File libs = new File(dataFolder, "libs");
        libs.mkdirs();

        URLClassLoader loader = (URLClassLoader) ETLoader.class.getClassLoader();

        File AIO = new File(libs, "AIO.jar");
        if (AIO.exists()) {
            JarDependency dependency = new JarDependency(AIO.toPath());
            JarLoader.load(dependency, loader);
            long ts2 = System.currentTimeMillis();
            EnderTranslate.log("Libs loaded in " + (ts2 - ts) + " ms");
            return;
        }

        EnderTranslate.log("If you have problems loading libs, you can download them directly and put it in plugins/BoostedAudio/libs");
        for (String link : libsLink) {
            EnderTranslate.log(link);
            File lib = new File(libs, link.substring(link.lastIndexOf('/') + 1));
            Path libsPath = Paths.get(lib.getAbsolutePath());

            JarDependency dependency = new JarDependency(link, libsPath);
            JarLoader.downloadIfNotExists(dependency);
            JarLoader.load(dependency, loader);
        }
        long ts2 = System.currentTimeMillis();
        EnderTranslate.log("Libs loaded in " + (ts2 - ts) + " ms");
    }

    public static long loadLibs(File dataFolder) {
        long ts = System.currentTimeMillis();
        try {
            File libs = new File(dataFolder, "libs");
            libs.mkdirs();

            URLClassLoader loader = (URLClassLoader) ETLoader.class.getClassLoader();

            // Load the AIO
            File AIO = new File(libs, "AIO.jar");
            if (AIO.exists()) {
                JarDependency dependency = new JarDependency(AIO.toPath());
                JarLoader.load(dependency, loader);
                long ts2 = System.currentTimeMillis();
                return ts2 - ts;
            }

            // Classic libs loading
            for (String link : libsLink) {
                System.out.println("Loading " + link);
                //BoostedAudioAPI.getAPI().info(link);
                File lib = new File(libs, link.substring(link.lastIndexOf('/') + 1));
                Path libsPath = Paths.get(lib.getAbsolutePath());

                JarDependency dependency = new JarDependency(link, libsPath);
                JarLoader.downloadIfNotExists(dependency);
                JarLoader.load(dependency, loader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long ts2 = System.currentTimeMillis();
        return ts2 - ts;
    }

}