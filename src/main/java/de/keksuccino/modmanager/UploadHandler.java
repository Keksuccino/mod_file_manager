package de.keksuccino.modmanager;

import de.keksuccino.core.ModVersionExtractor;
import de.keksuccino.core.TokenExtractor;
import de.keksuccino.curse.CurseFile;
import de.keksuccino.curse.CurseForge;
import de.keksuccino.curse.CurseProject;
import de.keksuccino.curse.ProjectRelation;
import de.keksuccino.modrinth.Modrinth;
import de.keksuccino.modrinth.ModrinthDependency;
import de.keksuccino.modrinth.ModrinthProject;
import de.keksuccino.modrinth.ModrinthVersion;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadHandler {

    public static CurseForge curseforge;
    public static CurseProject curseforgeProject;

    public static Modrinth modrinth;
    public static ModrinthProject modrinthProject;

    public static void prepare() throws Exception {

        TokenExtractor tokenExtractor = new TokenExtractor(ModManager.config.getOrDefault("token_file", ""));

        String curseToken = tokenExtractor.getCurseForgeToken();
        if (!curseToken.replace(" ", "").equals("")) {
            String endpoint = ModManager.config.getOrDefault("endpoint", "").toString();
            if (!endpoint.replace(" ", "").equals("")) {
                curseforge = new CurseForge(endpoint, curseToken);
            } else {
                throw new Exception("CURSEFORGE ENDPOINT NOT SET!");
            }
        } else {
            throw new Exception("CURSEFORGE TOKEN NOT SET!");
        }

        long projectId = (long) ModManager.config.getOrDefault("project_id", -10000L);
        if (projectId != -10000L) {
            curseforgeProject = curseforge.getProject(projectId);
        } else {
            throw new Exception("CURSEFORGE PROJECT ID NOT SET!");
        }

        String modrinthToken = tokenExtractor.getModrinthToken();
        if ((modrinthToken != null) && (modrinthToken.length() > 0)) {
            modrinth = new Modrinth(modrinthToken);
        } else {
            throw new Exception("MODRINTH TOKEN NOT SET! WILL NOT UPLOAD TO MODRINTH!");
        }

        if (modrinth != null) {
            String modrinthProjectIdSlug = ModManager.config.getOrDefault("modrinth_project_id_or_slug", "").toString();
            if ((modrinthProjectIdSlug != null) && (modrinthProjectIdSlug.length() > 0)) {
                modrinthProject = modrinth.getProject(modrinthProjectIdSlug);
                if (modrinthProject == null) {
                    throw new Exception("UNABLE TO GET MODRINTH PROJECT! STOPPING!");
                }
            } else {
                throw new Exception("MODRINTH PROJECT ID/SLUG NOT SET!");
            }
        }

    }

    /**
     * Uploads the mod.
     *
     * @param skipCurseForge If true, uploads to CurseForge will be skipped.
     * @param skipModrinth If true, uploads to Modrinth will be skipped.
     * @throws Exception if any error occurs during the upload process.
     */
    public static void upload(boolean skipCurseForge, boolean skipModrinth) throws Exception {

        try {

            prepare();

            CurseFile modFile = new CurseFile();
            ModrinthVersion modrinthVersion = new ModrinthVersion();

            // MOD VERSION
            String modVersion = ModVersionExtractor.getModVersion();
            if (modVersion.replace(" ", "").equals("")) {
                System.out.println("WARN: MOD VERSION NOT SET! WILL USE DEFAULT VERSION!");
                modVersion = "1.0.0";
            }
            modrinthVersion.version_number = modVersion + "-"
                    + ModFileHandler.getGameVersionRangeString() + "-"
                    + ModFileHandler.getLoadersString().replace("_", "-");

            // DISPLAY NAME
            String displayName = ModManager.config.getOrDefault("display_name", "").toString();
            if (displayName.replace(" ", "").equals("")) {
                System.out.println("WARN: MOD DISPLAY NAME NOT SET! WILL USE DEFAULT DISPLAY NAME!");
                displayName = "Release v%version%";
            }
            if (displayName.contains("%version%")) {
                displayName = displayName.replace("%version%", modVersion);
            }
            modFile.displayName = displayName;
            modrinthVersion.name = displayName;

            // RELEASE TYPE
            String releaseType = ModManager.config.getOrDefault("release_type", "").toString();
            if (releaseType.equals("alpha")) {
                modFile.releaseType = CurseFile.ReleaseType.ALPHA;
            } else if (releaseType.equals("beta")) {
                modFile.releaseType = CurseFile.ReleaseType.BETA;
            } else if (releaseType.equals("release")) {
                modFile.releaseType = CurseFile.ReleaseType.RELEASE;
            } else {
                System.out.println("WARN: NO MOD RELEASE TYPE SET! WILL USE DEFAULT TYPE!");
                releaseType = "alpha";
                modFile.releaseType = CurseFile.ReleaseType.ALPHA;
            }
            modrinthVersion.version_type = releaseType;

            // GAME VERSIONS
            String gameVersionsString = ModManager.config.getOrDefault("game_versions", "").toString();
            if (gameVersionsString.replace(" ", "").equals("")) {
                System.out.println("WARN: MOD GAME VERSIONS NOT SET! WILL USE DEFAULT VERSIONS!");
                gameVersionsString = "1.19.2:Minecraft 1.19,";
            }
            String[] gameVersionsArray = gameVersionsString.contains(",")
                    ? gameVersionsString.split("[,]")
                    : new String[]{gameVersionsString};
            List<Long> gameVersions = new ArrayList<>();
            List<String> modrinthGameVersions = new ArrayList<>();
            List<String> modrinthLoaders = new ArrayList<>();
            List<CurseForge.CurseGameVersion> curseVersions = curseforge.getGameVersions();
            List<CurseForge.CurseGameVersionType> curseVersionTypes = curseforge.getGameVersionTypes();
            for (String s : gameVersionsArray) {
                if (!s.replace(" ", "").equals("")) {
                    if (s.contains(":")) {
                        String versionString = s.split("[:]", 2)[0];
                        String typeString = s.split("[:]", 2)[1];
                        long typeId = -10000;
                        for (CurseForge.CurseGameVersionType t : curseVersionTypes) {
                            if (t.name.equals(typeString)) {
                                typeId = t.id;
                                break;
                            }
                        }
                        if (typeId != -10000) {
                            CurseForge.CurseGameVersion version = null;
                            for (CurseForge.CurseGameVersion v : curseVersions) {
                                if (v.name.equals(versionString) && (v.gameVersionTypeID == typeId)) {
                                    version = v;
                                    break;
                                }
                            }
                            if (version != null) {
                                modrinthGameVersions.add(version.name);
                                gameVersions.add(version.id);
                            } else {
                                System.out.println("ERROR: SKIPPING INVALID MOD GAME VERSION: " + s);
                            }
                        } else {
                            System.out.println("ERROR: SKIPPING INVALID MOD GAME VERSION: " + s);
                        }
                    } else {
                        if (s.equalsIgnoreCase("forge") || s.equalsIgnoreCase("fabric") ||
                                s.equalsIgnoreCase("neoforge") || s.equalsIgnoreCase("quilt")) {
                            modrinthLoaders.add(s.toLowerCase());
                        }
                        CurseForge.CurseGameVersion version = null;
                        for (CurseForge.CurseGameVersion v : curseVersions) {
                            if (v.name.equals(s)) {
                                version = v;
                                break;
                            }
                        }
                        if (version != null) {
                            gameVersions.add(version.id);
                        } else {
                            System.out.println("ERROR: SKIPPING INVALID MOD GAME VERSION: " + s);
                        }
                    }
                }
            }
            modFile.gameVersions.addAll(gameVersions);
            modrinthVersion.game_versions = modrinthGameVersions.toArray(new String[0]);
            modrinthVersion.loaders = modrinthLoaders.toArray(new String[0]);

            // Replace MC version range and loaders placeholders
            modFile.displayName = modFile.displayName.replace("%mc_version_range%", ModFileHandler.getGameVersionRangeString())
                    .replace("%loaders%", ModFileHandler.getLoadersStringFancy());
            modrinthVersion.name = modrinthVersion.name.replace("%mc_version_range%", ModFileHandler.getGameVersionRangeString())
                    .replace("%loaders%", ModFileHandler.getLoadersStringFancy());

            // CHANGELONG TYPE
            String changelogType = ModManager.config.getOrDefault("changelog_type", "").toString();
            if (changelogType.equals("text")) {
                modFile.changelogType = CurseFile.ChangelogType.TEXT;
            } else if (changelogType.equals("html")) {
                modFile.changelogType = CurseFile.ChangelogType.HTML;
            } else if (changelogType.equals("markdown")) {
                modFile.changelogType = CurseFile.ChangelogType.MARKDOWN;
            } else {
                System.out.println("WARN: MOD CHANGELOG TYPE NOT SET! WILL USE DEFAULT TYPE!");
                modFile.changelogType = CurseFile.ChangelogType.TEXT;
            }

            // CHANGELONG HANDLING FIX:
            // Instead of reading a changelog from a file ("changelog_file"), we now use the plain text changelog.
            String changelog = ModManager.config.getOrDefault("changelog", "Some changelog text.").toString();
            modFile.changelog = changelog.replace("%n%", "\n");
            // For Modrinth, convert newline characters to <br>
            modrinthVersion.changelog = changelog.replace("%n%", "<br>");

            // MOD RELATIONS
            String modRelationsString = ModManager.config.getOrDefault("mod_relations", "").toString();
            ModrinthDependency[] modrinthDependencies = new ModrinthDependency[0];
            if (!modRelationsString.replace(" ", "").equals("")) {
                String[] modRelationsArray = modRelationsString.contains(",")
                        ? modRelationsString.split("[,]")
                        : new String[]{modRelationsString};
                modrinthDependencies = ModrinthDependency.buildSimpleDependencyArray(modRelationsArray);
                for (String s : modRelationsArray) {
                    if (s.contains(":")) {
                        String slug = s.split("[:]", 2)[0].replace(" ", "");
                        String typeString = s.split("[:]", 2)[1].replace(" ", "");
                        ProjectRelation.RelationType type = null;
                        if (slug.length() > 0) {
                            if (typeString.equals("requiredDependency")) {
                                type = ProjectRelation.RelationType.REQUIRED_DEPENDENCY;
                            } else if (typeString.equals("optionalDependency")) {
                                type = ProjectRelation.RelationType.OPTIONAL_DEPENDENCY;
                            } else if (typeString.equals("incompatible")) {
                                type = ProjectRelation.RelationType.INCOMPATIBLE;
                            } else if (typeString.equals("embeddedLibrary")) {
                                type = ProjectRelation.RelationType.EMBEDDED_LIBRARY;
                            } else if (typeString.equals("tool")) {
                                type = ProjectRelation.RelationType.TOOL;
                            }

                            if (type != null) {
                                ProjectRelation r = new ProjectRelation();
                                r.slug = slug;
                                r.relationType = type;
                                modFile.relations.add(r);
                            } else {
                                System.out.println("ERROR: INVALID MOD RELATION: " + s + " (INVALID TYPE)");
                            }
                        } else {
                            System.out.println("ERROR: INVALID MOD RELATION: " + s + " (INVALID SLUG)");
                        }
                    } else {
                        System.out.println("ERROR: INVALID MOD RELATION: " + s);
                    }
                }
            } else {
                System.out.println("WARN: NO MOD RELATIONS SET! WILL NOT SET RELATIONS!");
            }
            modrinthVersion.dependencies = modrinthDependencies;

            // MODRINTH FEATURED
            boolean modrinthFeatured = (boolean) ModManager.config.getOrDefault("modrinth_featured", false);
            modrinthVersion.featured = modrinthFeatured;

            File modrinthMainFile = null;
            File modrinthSourceFile = null;

            // UPLOAD FILES TO CURSEFORGE
            String uploadFromPath = ModManager.config.getOrDefault("upload_from_folder", "").toString();
            if (uploadFromPath.replace(" ", "").equals("")) {
                throw new Exception("Mod upload directory not set!");
            }
            File uploadFrom = new File(uploadFromPath);
            if (!uploadFrom.isDirectory()) {
                throw new Exception("Mod upload directory not found: " + uploadFromPath);
            }
            String modNameConfigured = ModManager.config.getOrDefault("rename_jar_to", "").toString();
            modNameConfigured = modNameConfigured.replace("%version%", modVersion)
                    .replace("%mc_version_range%", ModFileHandler.getGameVersionRangeString())
                    .replace("%loaders%", ModFileHandler.getLoadersString());
            String devNameConfigured = ModManager.config.getOrDefault("dev_jar_prefix", "").toString() + modNameConfigured;
            File mainMod = new File(uploadFrom.getPath() + "/" + modNameConfigured);
            if (!mainMod.isFile()) {
                throw new Exception("MAIN MOD FILE NOT FOUND: " + mainMod.getPath());
            }
            modFile.file = mainMod;
            modrinthMainFile = mainMod;
            long fileId = 0;
            if (!skipCurseForge) {
                fileId = curseforgeProject.uploadFile(modFile);
                if (fileId == -10000L) {
                    throw new Exception("CurseForge main mod upload failed.");
                }
            } else {
                System.out.println("INFO: SKIPPING CURSEFORGE MAIN FILE UPLOAD! (RUNNING IN 'UPLOAD-SKIP-CURSE' MODE)");
            }
            File sourceMod = new File(uploadFrom.getPath() + "/" + devNameConfigured);
            if (!sourceMod.isFile()) {
                throw new Exception("SOURCE MOD FILE NOT FOUND: " + sourceMod.getPath());
            }
            CurseFile sourceModFile = new CurseFile();
            sourceModFile.file = sourceMod;
            modrinthSourceFile = sourceMod;
            sourceModFile.parentFileID = fileId;
            sourceModFile.relations = modFile.relations;
            sourceModFile.releaseType = modFile.releaseType;
            sourceModFile.changelog = ModManager.config.getOrDefault("dev_file_description", "").toString();
            if (sourceModFile.changelog.replace(" ", "").equals("")) {
                sourceModFile.changelog = "";
            }
            if (!skipCurseForge) {
                long sourceFileId = curseforgeProject.uploadFile(sourceModFile);
                if (sourceFileId == -10000L) {
                    throw new Exception("CurseForge source mod upload failed.");
                } else {
                    System.out.println("INFO: CURSEFORGE: UPLOAD SUCCESSFULLY FINISHED!");
                }
            } else {
                System.out.println("INFO: SKIPPING CURSEFORGE DEV FILE UPLOAD! (RUNNING IN 'UPLOAD-SKIP-CURSE' MODE)");
            }

            // UPLOAD FILES TO MODRINTH
            if ((modrinth != null) && (modrinthProject != null) && !skipModrinth) {
                modrinthVersion.files.add(modrinthMainFile);
                modrinthVersion.files.add(modrinthSourceFile);
                String response = modrinthProject.createVersion(modrinthVersion);
                if (response != null) {
                    System.out.println("INFO: MODRINTH: VERSION SUCCESSFULLY CREATED AND FILES UPLOADED! (ID: " + response + ")");
                } else {
                    throw new Exception("MODRINTH: Failed to create version or upload files!");
                }
            } else if (skipModrinth) {
                System.out.println("INFO: SKIPPING MODRINTH BECAUSE UPLOAD MODE IS SET TO SKIP MODRINTH!");
            }
        } catch (Exception e) {
            // Rethrow the exception with its message for display by the GUI.
            throw new Exception("Upload failed: " + e.getMessage(), e);
        }

    }

}
