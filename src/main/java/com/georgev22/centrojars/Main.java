package com.georgev22.centrojars;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Main class for CentroJars application.
 */
public class Main {

    private final String API_BASE_URL = "https://centrojars.com/api/";
    private final String PROPERTIES_FILE = "centrojars.properties";
    private final String CENTRO_JARS_FOLDER = "./centroJars/";
    private final Logger logger = LogManager.getLogger("CentroJars");

    /**
     * Main method to start the CentroJars application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        new Main().start(args);
    }

    /**
     * Starts the CentroJars application.
     */
    public void start(String[] args) {
        configureLogging();
        logger.info("CentroJars starting...");
        logger.info("""
                                                                                                                                          \s
                                                                                              ,---._                                      \s
                  ,----..                               ___                                 .-- -.' \\                                     \s
                 /   /   \\                            ,--.'|_                               |    |   :                                    \s
                |   :     :                  ,---,    |  | :,'    __  ,-.    ,---.          :    ;   |                __  ,-.             \s
                .   |  ;. /              ,-+-. /  |   :  : ' :  ,' ,'/ /|   '   ,'\\         :        |              ,' ,'/ /|   .--.--.   \s
                .   ; /--`     ,---.    ,--.'|'   | .;__,'  /   '  | |' |  /   /   |        |    :   :   ,--.--.    '  | |' |  /  /    '  \s
                ;   | ;       /     \\  |   |  ,"' | |  |   |    |  |   ,' .   ; ,. :        :           /       \\   |  |   ,' |  :  /`./  \s
                |   : |      /    /  | |   | /  | | :__,'| :    '  :  /   '   | |: :        |    ;   | .--.  .-. |  '  :  /   |  :  ;_    \s
                .   | '___  .    ' / | |   | |  | |   '  : |__  |  | '    '   | .; :    ___ l           \\__\\/: . .  |  | '     \\  \\    `. \s
                '   ; : .'| '   ;   /| |   | |  |/    |  | '.'| ;  : |    |   :    |  /    /\\    J   :  ," .--.; |  ;  : |      `----.   \\\s
                '   | '/  : '   |  / | |   | |--'     ;  :    ; |  , ;     \\   \\  /  /  ../  `..-    , /  /  ,.  |  |  , ;     /  /`--'  /\s
                |   :    /  |   :    | |   |/         |  ,   /   ---'       `----'   \\    \\         ; ;  :   .'   \\  ---'     '--'.     / \s
                 \\   \\ .'    \\   \\  /  '---'           ---`-'                         \\    \\      ,'  |  ,     .-./             `--'---'  \s
                  `---`       `----'                                                   "---....--'     `--`---'                           \s
                                                                                                                                          \s
                """);
        logger.info("Made with love by George V. https://github.com/GeorgeV220");
        logger.info("https://centrojars.com/");

        Properties properties = loadProperties();
        if (properties.isEmpty()) {
            properties = promptUserForServerDetails();
            saveProperties(properties);
        }

        String serverType = properties.getProperty("server.type");
        String serverCategory = properties.getProperty("server.category");
        String version = properties.getProperty("server.version");

        String fileName = downloadJar(serverType, serverCategory, version);
        if (fileName != null) {
            startMinecraftServer(fileName, args);
        } else {
            logger.error("Failed to download jar.");
        }
    }

    /**
     * Configures logging for the application.
     */
    private void configureLogging() {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
    }

    /**
     * Loads properties from the properties file.
     *
     * @return Loaded properties.
     */
    private Properties loadProperties() {
        Properties properties = new Properties();
        try {
            File file = new File(PROPERTIES_FILE);
            if (!file.exists()) {
                return properties;
            }
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            logger.error("Failed to load properties file: {}", e.getMessage());
        }
        return properties;
    }

    /**
     * Prompts the user for server details and returns them as properties.
     *
     * @return Properties containing server details.
     */
    private Properties promptUserForServerDetails() {
        Properties properties = new Properties();
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();

            System.out.println("Properties file not found. Please provide the following details:");
            System.out.println("Check https://centrojars.com/ for more details.");
            String serverType = lineReader.readLine("Server Type (e.g., servers): ");
            String serverCategory = lineReader.readLine("Server Category (e.g., spigot): ");
            String version = lineReader.readLine("Version (e.g., latest): ");
            properties.setProperty("server.type", serverType);
            properties.setProperty("server.category", serverCategory);
            properties.setProperty("server.version", version);
        } catch (IOException e) {
            logger.error("Error prompting user for server details: {}", e.getMessage());
        }
        return properties;
    }

    /**
     * Saves properties to the properties file.
     *
     * @param properties Properties to be saved.
     */
    private void saveProperties(Properties properties) {
        try {
            properties.store(new FileOutputStream(PROPERTIES_FILE), null);
        } catch (IOException e) {
            logger.error("Failed to save properties file: {}", e.getMessage());
        }
    }

    /**
     * Downloads the JAR file from the CentroJars API.
     *
     * @param type     Server type.
     * @param category Server category.
     * @return Absolute path of the downloaded JAR file, or null if download failed.
     * @param* @param version Server version.
     */
    private String downloadJar(String type, String category, String version) {
        String apiUrl = API_BASE_URL + "fetchJar/" + type + "/" + category + "/" + version + ".jar";
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = apiUrl.substring(apiUrl.lastIndexOf('/') + 1);

                String filePath = CENTRO_JARS_FOLDER + type + "/" + version + "/";
                File outputFile = new File(filePath + fileName);
                outputFile.getParentFile().mkdirs();

                int contentLength = connection.getContentLength();

                try (InputStream inputStream = connection.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    int totalBytesRead = 0;
                    long startTime = System.nanoTime();
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;

                        long currentTime = System.nanoTime();
                        long elapsedTime = currentTime - startTime;
                        double speed = totalBytesRead / (elapsedTime / 1e9);

                        double remainingBytes = contentLength - totalBytesRead;
                        double timeLeft = remainingBytes / speed;

                        int progress = (int) (totalBytesRead * 100.0 / contentLength);
                        String progressBar = getProgressBar(progress);

                        System.out.printf("\rDownloading... %s  Speed: %.2f KB/s  Time left: %.2f seconds",
                                progressBar, speed / 1024, timeLeft);
                        System.out.flush();
                    }

                    System.out.println();
                }

                logger.info("Jar downloaded successfully: {}", outputFile.getAbsolutePath());
                return outputFile.getAbsolutePath();
            } else {
                logger.error("Failed to fetch jar. Response code: {} reason: {}", responseCode, connection.getResponseMessage());
                logger.error("API URL: {}", apiUrl);
            }
        } catch (IOException e) {
            logger.error("Error fetching jar: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Generates a progress bar string based on the given progress percentage.
     *
     * @param progress Progress percentage.
     * @return Progress bar string.
     */
    private String getProgressBar(int progress) {
        StringBuilder progressBar = new StringBuilder("[");
        int numChars = progress / 2;
        for (int i = 0; i < 50; i++) {
            if (i < numChars) {
                progressBar.append("=");
            } else if (i == numChars) {
                progressBar.append(">");
            } else {
                progressBar.append(" ");
            }
        }
        progressBar.append("] ");
        progressBar.append(progress);
        progressBar.append("%");
        return progressBar.toString();
    }

    /**
     * Starts the Minecraft server using the specified JAR file and command line arguments.
     *
     * @param jarFile Path to the Minecraft server JAR file.
     * @param args    Command line arguments.
     */
    private void startMinecraftServer(String jarFile, String[] args) {
        try {
            List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

            List<String> command = new ArrayList<>();
            command.add("java");
            command.addAll(vmArguments);
            command.add("-jar");
            command.add(jarFile);
            command.addAll(Arrays.asList(args));

            logger.info("Starting Minecraft server with command: {}", String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            Process pr = pb.start();

            int exitCode = pr.waitFor();
            logger.info("Minecraft server exited with code: {}", exitCode);
        } catch (IOException | InterruptedException e) {
            logger.error("Error starting Minecraft server: {}", e.getMessage());
        }
    }
}