package de.keksuccino.modmanager;

import de.keksuccino.core.ModVersionExtractor;
import de.keksuccino.core.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ModFileHandler {

    private static final File TEMP_DIR = new File("temp_data");

    private static String gameVersionRangeString = null;
    private static String loadersString = null;

    public static boolean copySourcesJarToOut() {

        boolean returnValue = false;

        try {

            String modVersion = ModVersionExtractor.getModVersion();
            if (modVersion.replace(" ", "").equals("")) {
                System.out.println("WARN: copySourcesJarToOut: MOD VERSION NOT SET! WILL USE DEFAULT VERSION!");
                modVersion = "0.0.0";
            }

            File modJar = new File(ModManager.config.getOrDefault("build_jar_dir", "") + "/" + ModManager.config.getOrDefault("sources_jar_name", ""));

            if (modJar.isFile() && modJar.getPath().toLowerCase().endsWith(".jar")) {

                String outDirRaw = ModManager.config.getOrDefault("output_dir", "");
                if (!outDirRaw.replace(" ", "").equals("")) {
                    File outputDir = new File(outDirRaw);
                    if (!outputDir.isDirectory()) {
                        outputDir.mkdirs();
                    }
                    String renameTo = ModManager.config.getOrDefault("rename_jar_to", "");
                    if (!renameTo.toLowerCase().endsWith(".jar")) {
                        renameTo = ModManager.config.getOrDefault("sources_jar_name", "");
                    }
                    renameTo = ModManager.config.getOrDefault("dev_jar_prefix", "") + renameTo;
                    renameTo = renameTo.replace("%version%", modVersion).replace("%mc_version_range%", getGameVersionRangeString()).replace("%loaders%", getLoadersString());
                    org.apache.commons.io.FileUtils.copyFile(modJar, new File(outputDir.getPath() + "/" + renameTo));
                    returnValue = true;
                    System.out.println("INFO: copySourcesJarToOut: SOURCES JAR COPIED TO OUTPUT DIR AND RENAMED!");
                }

            } else {
                System.out.println("ERROR: copySourcesJarToOut: SOURCES JAR NOT FOUND: " + modJar.getPath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;

    }

    public static boolean copyMainJarToOut() {

        boolean returnValue = false;

        try {

            String modVersion = ModVersionExtractor.getModVersion();
            if (modVersion.replace(" ", "").equals("")) {
                System.out.println("WARN: copyNormalJarToOut: MOD VERSION NOT SET! WILL USE DEFAULT VERSION!");
                modVersion = "0.0.0";
            }

            File modJar = new File(ModManager.config.getOrDefault("build_jar_dir", "") + "/" + ModManager.config.getOrDefault("mod_jar_name", ""));

            if (modJar.isFile() && modJar.getPath().toLowerCase().endsWith(".jar")) {

                String outDirRaw = ModManager.config.getOrDefault("output_dir", "");
                if (!outDirRaw.replace(" ", "").equals("")) {
                    File outputDir = new File(outDirRaw);
                    if (!outputDir.isDirectory()) {
                        outputDir.mkdirs();
                    }
                    String renameTo = ModManager.config.getOrDefault("rename_jar_to", "");
                    if (!renameTo.toLowerCase().endsWith(".jar")) {
                        renameTo = ModManager.config.getOrDefault("mod_jar_name", "");
                    }
                    renameTo = renameTo.replace("%version%", modVersion).replace("%mc_version_range%", getGameVersionRangeString()).replace("%loaders%", getLoadersString());
                    org.apache.commons.io.FileUtils.copyFile(modJar, new File(outputDir.getPath() + "/" + renameTo));
                    returnValue = true;
                    System.out.println("INFO: copyNormalJarToOut: NORMAL JAR COPIED TO OUTPUT DIR AND RENAMED!");
                }

            } else {
                System.out.println("ERROR: copyNormalJarToOut: MOD JAR NOT FOUND: " + modJar.getPath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;

    }

    public static String getGameVersionRangeString() {
        if (gameVersionRangeString == null) {
            String gameVersionsString = ModManager.config.getOrDefault("game_versions", "");
            if (gameVersionsString.replace(" ", "").equals("")) {
                gameVersionsString = "1.19.2:Minecraft 1.19,";
            }
            String[] gameVersionsArray;
            if (gameVersionsString.contains(",")) {
                gameVersionsArray = gameVersionsString.split("[,]");
            } else {
                gameVersionsArray = new String[]{gameVersionsString};
            }
            List<String> modrinthGameVersions = new ArrayList<>();
            for (String s : gameVersionsArray) {
                if (!s.replace(" ", "").equals("")) {
                    if (s.contains(":")) {
                        String versionString = s.split("[:]", 2)[0];
                        modrinthGameVersions.add(versionString);
                    }
                }
            }
            modrinthGameVersions.sort(Comparator.naturalOrder());
            if (!modrinthGameVersions.isEmpty()) {
                if (modrinthGameVersions.size() == 1) {
                    gameVersionRangeString = modrinthGameVersions.get(0);
                } else {
                    gameVersionRangeString = modrinthGameVersions.get(0) + "-" + modrinthGameVersions.get(modrinthGameVersions.size()-1);
                }
            } else {
                gameVersionRangeString = "";
            }
        }
        return gameVersionRangeString;
    }

    public static String getLoadersString() {
        if (loadersString == null) {
            String gameVersionsString = ModManager.config.getOrDefault("game_versions", "");
            if (gameVersionsString.replace(" ", "").equals("")) {
                gameVersionsString = "1.19.2:Minecraft 1.19,";
            }
            String[] gameVersionsArray;
            if (gameVersionsString.contains(",")) {
                gameVersionsArray = gameVersionsString.split("[,]");
            } else {
                gameVersionsArray = new String[]{gameVersionsString};
            }
            List<String> loaderStrings = new ArrayList<>();
            for (String s : gameVersionsArray) {
                if (!s.replace(" ", "").equals("")) {
                    if (!s.contains(":")) {
                        if (s.equalsIgnoreCase("forge") || s.equalsIgnoreCase("fabric") || s.equalsIgnoreCase("neoforge") || s.equalsIgnoreCase("quilt")) {
                            loaderStrings.add(s.toLowerCase());
                        }
                    }
                }
            }
            if (!loaderStrings.isEmpty()) {
                if (loaderStrings.size() == 1) {
                    loadersString = loaderStrings.get(0);
                } else {
                    loadersString = "";
                    for (String s : loaderStrings) {
                        if (loadersString.length() > 0) {
                            loadersString += "_";
                        }
                        loadersString += s;
                    }
                }
            } else {
                loadersString = "";
            }
        }
        return loadersString;
    }

    public static String getLoadersStringFancy() {
        String loadersString = getLoadersString();
        String fancy = "";
        if (loadersString.contains("_")) {
            for (String s : loadersString.split("[_]")) {
                String loader;
                if (s.equalsIgnoreCase("neoforge")) {
                    loader = "NeoForge";
                } else if (s.length() > 1) {
                    loader = s.substring(0, 1).toUpperCase() + s.substring(1);
                } else {
                    loader = s.toUpperCase();
                }
                if (fancy.length() > 0) {
                    fancy += " & ";
                }
                fancy += loader;
            }
        } else {
            if (loadersString.length() > 1) {
                if (loadersString.equalsIgnoreCase("neoforge")) {
                    fancy = "NeoForge";
                } else {
                    fancy = loadersString.substring(0, 1).toUpperCase() + loadersString.substring(1);
                }
            } else {
                fancy = loadersString.toUpperCase();
            }
        }
        return fancy;
    }

}
