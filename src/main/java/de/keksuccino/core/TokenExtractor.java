package de.keksuccino.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Helper class for extracting tokens from a text file.
 * The file should contain lines in the following format:
 *
 * curseforge:<token>
 * modrinth:<token>
 */
public class TokenExtractor {

    private String curseForgeToken;
    private String modrinthToken;

    /**
     * Constructs a TokenExtractor and loads tokens from the given file.
     *
     * @param filePath the path to the token file.
     * @throws IOException if an I/O error occurs reading the file.
     */
    public TokenExtractor(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        for (String line : lines) {
            if (line.startsWith("curseforge:")) {
                curseForgeToken = line.substring("curseforge:".length()).trim();
            } else if (line.startsWith("modrinth:")) {
                modrinthToken = line.substring("modrinth:".length()).trim();
            }
        }
    }

    /**
     * Returns the CurseForge token extracted from the file.
     *
     * @return the CurseForge token, or null if not found.
     */
    public String getCurseForgeToken() {
        return curseForgeToken;
    }

    /**
     * Returns the Modrinth token extracted from the file.
     *
     * @return the Modrinth token, or null if not found.
     */
    public String getModrinthToken() {
        return modrinthToken;
    }

}
