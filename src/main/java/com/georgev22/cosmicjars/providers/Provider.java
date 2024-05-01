package com.georgev22.cosmicjars.providers;

import com.georgev22.cosmicjars.Main;
import com.georgev22.cosmicjars.providers.implementations.*;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class representing a provider for downloading server jars.
 */
public abstract class Provider {

    /**
     * Main instance of the application.
     */
    protected final Main main = Main.getInstance();

    /**
     * Type of the server (e.g., Bukkit, Spigot).
     */
    private final String serverType;

    /**
     * Name of the server implementation (e.g., CraftBukkit, Paper).
     */
    private final String serverImplementation;

    /**
     * Version of the server.
     */
    private final String serverVersion;

    /**
     * Constructs a new Provider with the specified server type, implementation, and version.
     *
     * @param serverType           Type of the server.
     * @param serverImplementation Name of the server implementation.
     * @param serverVersion        Version of the server.
     */
    public Provider(String serverType, String serverImplementation, String serverVersion) {
        this.serverType = serverType;
        this.serverImplementation = serverImplementation;
        this.serverVersion = serverVersion;
    }

    /**
     * Retrieves the server implementation.
     *
     * @return The server implementation.
     */
    public String getServerImplementation() {
        return this.serverImplementation;
    }

    /**
     * Retrieves the server type.
     *
     * @return The server type.
     */
    public String getServerType() {
        return this.serverType;
    }

    /**
     * Retrieves the server version.
     *
     * @return The server version.
     */
    public String getServerVersion() {
        return this.serverVersion;
    }

    /**
     * Downloads and returns the path to the server jar.
     * This method passes the server type, implementation, and version from the constructor to the {@link #downloadJar(String, String, String)} method.
     *
     * @return The path to the server jar.
     * @see #downloadJar(String, String, String)
     */
    public String downloadJar() {
        return this.downloadJar(this.getServerType(), this.getServerImplementation(), this.getServerVersion());
    }

    /**
     * Downloads and returns the path to the server jar.
     *
     * @param serverType           Type of the server.
     * @param serverImplementation Name of the server implementation.
     * @param serverVersion        Version of the server.
     * @return The path to the server jar.
     */
    public abstract String downloadJar(String serverType, String serverImplementation, String serverVersion);

    /**
     * Retrieves information about the server.
     *
     * @return Information about the server.
     */
    public String getServerInfo() {
        return this.getServerType() + " " + this.getServerImplementation() + " " + this.getServerVersion();
    }

    /**
     * Retrieves the provider for the specified server type, implementation, and version.
     *
     * @param serverType           Type of the server.
     * @param serverImplementation Name of the server implementation.
     * @param serverVersion        Version of the server.
     * @return The provider for the specified server type, implementation, and version.
     */
    public static @NotNull Provider getProvider(@NotNull String serverType, String serverImplementation, String serverVersion) {
        Provider provider;
        return switch (serverType) {
            case "servers" -> switch (serverImplementation) {
                case "purpur" -> new PurpurProvider(serverType, serverImplementation, serverVersion);
                case "paper", "folia" -> new PaperProvider(serverType, serverImplementation, serverVersion);
                default -> new CentroJarProvider(serverType, serverImplementation, serverVersion);
            };
            case "modded" -> switch (serverImplementation) {
                case "mohist", "banner" -> new MohistProvider(serverType, serverImplementation, serverVersion);
                default -> new CentroJarProvider(serverType, serverImplementation, serverVersion);
            };
            case "proxies" -> switch (serverImplementation) {
                case "velocity" -> new PaperProvider(serverType, serverImplementation, serverVersion);
                case "bungeecord" -> new BungeeCordProvider(serverType, serverImplementation, serverVersion);
                default -> new CentroJarProvider(serverType, serverImplementation, serverVersion);
            };
            default -> new CentroJarProvider(serverType, serverImplementation, serverVersion);
        };
    }

}