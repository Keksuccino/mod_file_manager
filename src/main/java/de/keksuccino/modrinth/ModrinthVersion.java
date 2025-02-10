package de.keksuccino.modrinth;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModrinthVersion {

    /** REQUIRED! The files to upload **/
    public List<File> files = new ArrayList<>();

    /** REQUIRED! The name of this version **/
    public String name;
    /** REQUIRED! The version number. Ideally will follow semantic versioning **/
    public String version_number;
    /** The changelog for this version **/
    public String changelog;
    /** A list of specific versions of projects that this version depends on **/
    public ModrinthDependency[] dependencies;
    /** REQUIRED! A list of versions of Minecraft that this version supports. VERSION EXAMPLES: 1.16.5 ; 1.17.1 **/
    public String[] game_versions;
    /** REQUIRED! Types: release ; beta ; alpha **/
    public String version_type;
    /** REQUIRED! The mod loaders that this version supports. LOADER EXAMPLES: fabric ; forge **/
    public String[] loaders;
    /** REQUIRED! Whether the version is featured or not **/
    public boolean featured = false;
    /** The multipart field name of the primary file **/
    public String primary_file = "0";

    public static String[] buildGameVersionsArray(String... versions) {
        List<String> l = new ArrayList<>();
        if (versions != null) {
            for (String s : versions) {
                l.add(convertGameVersionFromCurseForgeGameVersion(s));
            }
        }
        return l.toArray(new String[0]);
    }

    public static String convertGameVersionFromCurseForgeGameVersion(String curseForgeVersion) {
        if (curseForgeVersion.contains(":")) {
            return curseForgeVersion.split("[:]", 2)[0];
        }
        return curseForgeVersion;
    }

}
