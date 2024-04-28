package fr.supermax_8.endertranslate.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;

public class ResourceUtils {


    /**
     * Copies a resource from the JAR file to a specified location on the file system.
     *
     * @param resourcePath The path of the resource to copy from the JAR.
     * @param outputPath   The destination path where the resource should be saved.
     * @throws IOException If an error occurs while copying the resource.
     */
    public static void saveResource(String resourcePath, String outputPath) throws IOException {
        printAllResources();
        try (InputStream inputStream = getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("The specified resource was not found: " + resourcePath);
            }

            Path destination = Path.of(outputPath);
            Files.createDirectories(destination.getParent());

            // Copy the resource to the destination location
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }


    /**
     * Returns an InputStream for a specified resource from the JAR.
     *
     * @param resourcePath The path of the resource to retrieve from the JAR.
     * @return An InputStream for the specified resource.
     */
    public static InputStream getResourceAsStream(String resourcePath) {
        InputStream inputStream = ResourceUtils.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            System.out.println("%%__USER__%% The specified resource was not found: " + resourcePath);
            return null;
        }
        return inputStream;
    }


    public static void printAllResources() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("");

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                System.out.println("Resource: " + resource.getFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}