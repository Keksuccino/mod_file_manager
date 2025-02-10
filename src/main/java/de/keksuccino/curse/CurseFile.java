package de.keksuccino.curse;

import de.keksuccino.core.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CurseFile {

    public File file;
    public String changelog;
    public ChangelogType changelogType = ChangelogType.TEXT;
    public String displayName;
    public long parentFileID = -10000L;
    public List<Long> gameVersions = new ArrayList<>();
    public ReleaseType releaseType;
    public List<ProjectRelation> relations = new ArrayList<>();

    public CurseFile(File file, String displayName, ReleaseType releaseType, @Nullable String changelog, @Nullable ChangelogType changelogType, @Nullable List<Long> gameVersions, @Nullable List<ProjectRelation> relations) {
        this.file = file;
        this.displayName = displayName;
        this.releaseType = releaseType;
        if (changelog != null) {
            this.changelog = changelog;
        } else {
            this.changelog = " ";
        }
        if (changelogType != null) {
            this.changelogType = changelogType;
        }
        if (gameVersions != null) {
            this.gameVersions.addAll(gameVersions);
        }
        if (relations != null) {
            this.relations.addAll(relations);
        }
    }

    public CurseFile(File file, String displayName, ReleaseType releaseType) {
        this(file, displayName, releaseType, null, null, null, null);
    }

    public CurseFile() {
    }

    public static enum ChangelogType {

        TEXT("text"),
        HTML("html"),
        MARKDOWN("markdown");

        public String name;

        ChangelogType(String name) {
            this.name = name;
        }

    }

    public static enum ReleaseType {

        ALPHA("alpha"),
        BETA("beta"),
        RELEASE("release");

        public String name;

        ReleaseType(String name) {
            this.name = name;
        }

    }

}
