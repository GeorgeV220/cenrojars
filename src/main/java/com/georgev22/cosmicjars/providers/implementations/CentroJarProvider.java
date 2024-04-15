package com.georgev22.cosmicjars.providers.implementations;

import com.georgev22.cosmicjars.providers.Provider;
import com.georgev22.cosmicjars.utilities.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Implementation of Provider for downloading server jars from the CentroJars API.
 */
public class CentroJarProvider extends Provider {

    /**
     * Base URL of the CentroJars API.
     */
    private final String API_BASE_URL = "https://centrojars.com/api/";

    /**
     * Constructs a new CentroJarProvider with the specified server type, implementation, and version.
     *
     * @param serverType            Type of the server.
     * @param serverImplementation Name of the server implementation.
     * @param serverVersion         Version of the server.
     */
    public CentroJarProvider(String serverType, String serverImplementation, String serverVersion) {
        super(serverType, serverImplementation, serverVersion);
    }

    /**
     * Downloads the server jar from the CentroJars API.
     *
     * @return The URL of the downloaded server jar, or null if the download fails.
     */
    @Override
    public String downloadJar() {
        String apiUrl = API_BASE_URL + "fetchJar/" + this.getServerType() + "/" + this.getServerImplementation() + "/" + this.getServerVersion() + ".jar";
        this.main.getLogger().debug("Fetching jar: {}", apiUrl);
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = apiUrl.substring(apiUrl.lastIndexOf('/') + 1);

                String filePath = this.main.getCosmicJarsFolder() + this.getServerType() + "/" + this.getServerImplementation() + "/" + this.getServerVersion() + "/";

                return Utils.downloadFile(apiUrl, filePath, fileName);
            } else {
                this.main.getLogger().error("Failed to fetch jar. Response code: {} reason: {}", responseCode, connection.getResponseMessage());
                this.main.getLogger().error("API URL: {}", apiUrl);
            }
        } catch (IOException e) {
            this.main.getLogger().error("Error fetching jar: {}", e.getMessage());
        }
        return null;
    }
}