package de.keksuccino.curse;

public class ProjectRelation {

    public String slug;
    public RelationType relationType;

    public static enum RelationType {

        EMBEDDED_LIBRARY("embeddedLibrary"),
        INCOMPATIBLE("incompatible"),
        OPTIONAL_DEPENDENCY("optionalDependency"),
        REQUIRED_DEPENDENCY("requiredDependency"),
        TOOL("tool");

        public String name;

        RelationType(String name) {
            this.name = name;
        }

    }

}
