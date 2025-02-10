package de.keksuccino.modmanager;

import de.keksuccino.core.config.Config;

public class ModManager {

    public static final String VERSION = "1.6.0";

    public static Config config;

    public static void updateConfig() {

        try {

            config = new Config("config.txt");

            config.registerValue("mod_jar_name", "modid-1.0.0.jar", "general");
            config.registerValue("sources_jar_name", "modid-1.0.0-sources.jar", "general");
            config.registerValue("rename_jar_to", "modid_%loaders%_%version%_MC_%mc_version_range%.jar", "general");
            config.registerValue("dev_jar_prefix", "sources_", "general");
            config.registerValue("build_classes_dir", "build/classes/java/main/de", "general");
            config.registerValue("src_dir", "src/main/java/de", "general");
            config.registerValue("build_jar_dir", "build/libs", "general");
            config.registerValue("output_dir", "jar_output", "general");
            config.registerValue("display_name", "[%loaders%] v%version% MC %mc_version_range%", "general");
            config.registerValue("upload_from_folder", "jar_output", "general");
            config.registerValue("mod_relations", "konkrete-fabric:requiredDependency,melody:requiredDependency,fabric-api:requiredDependency,", "general");
            config.registerValue("game_versions", "1.21.4:Minecraft 1.21,1.21.3:Minecraft 1.21,Fabric,", "general");
            config.registerValue("release_type", "release", "general");
            config.registerValue("changelog", "Some changelog text.%n%Supports multiline input.", "general");
            config.registerValue("dev_file_description", "Sources for developers. (Mojmap)", "general");
            config.registerValue("token_file", "../../../.TOKENS", "general");

            config.registerValue("endpoint", "minecraft", "curseforge");
            config.registerValue("project_id", -10000L, "curseforge");
            config.registerValue("changelog_type", "text", "curseforge");

            config.registerValue("modrinth_project_id_or_slug", "", "modrinth");
            config.registerValue("modrinth_featured", false, "modrinth");

            config.clearUnusedValues();

            config.syncConfig();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
