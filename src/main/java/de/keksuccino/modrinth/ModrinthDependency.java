package de.keksuccino.modrinth;

import java.util.ArrayList;
import java.util.List;

public class ModrinthDependency {

    /** The ID of the version that this version depends on **/
    public String version_id;
    /** The ID of the project that this version depends on **/
    public String project_id;
    /** The file name of the dependency, mostly used for showing external dependencies on modpacks **/
    public String file_name;
    /** Types: required ; optional ; incompatible ; embedded **/
    public String dependency_type;

    public static ModrinthDependency build(String version_id, String project_id, String file_name, String dependency_type) {
        ModrinthDependency d = new ModrinthDependency();
        d.version_id = version_id;
        d.project_id = project_id;
        d.file_name = file_name;
        d.dependency_type = dependency_type;
        return d;
    }

    /**
     * @param projectIdsAndType Project IDs and types separated by colon. Example: konkrete:required
     **/
    public static ModrinthDependency[] buildSimpleDependencyArray(String... projectIdsAndType) {
        List<ModrinthDependency> l = new ArrayList<>();
        if (projectIdsAndType != null) {
            for (String s : projectIdsAndType) {
                if (s.contains(":")) {
                    String id = s.split("[:]", 2)[0];
                    String type = getDependencyTypeForCurseForgeType(s.split("[:]", 2)[1]);
                    l.add(ModrinthDependency.build(null, id, null, type));
                }
            }
        }
        return l.toArray(new ModrinthDependency[0]);
    }

    public static String getDependencyTypeForCurseForgeType(String type) {
        if (type.equals("requiredDependency")) {
            return "required";
        } else if (type.equals("optionalDependency")) {
            return "optional";
        } else if (type.equals("incompatible")) {
            return "incompatible";
        } else if (type.equals("embeddedLibrary")) {
            return "embedded";
        }
        return "optional";
    }

}
