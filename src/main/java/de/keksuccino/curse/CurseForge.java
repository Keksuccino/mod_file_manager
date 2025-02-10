package de.keksuccino.curse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CurseForge {

    public String token;
    public String endpoint;

    protected static final String API_BASE_URL = "https://%endpoint%.curseforge.com/api";

    public CurseForge(String endpoint, String token) {
        this.token = token;
        this.endpoint = endpoint;
    }

    /**
     * Returns a new project instance.<br>
     * <b>This will never return NULL, even if the project ID isn't valid!</b>
     */
    public CurseProject getProject(long projectId) {
        return new CurseProject(projectId, this);
    }

    public String getApiBaseUrl() {
        return API_BASE_URL.replace("%endpoint%", endpoint);
    }

    public List<CurseGameVersion> getGameVersions() throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet getVersions = new HttpGet(getApiBaseUrl() + "/game/versions");

        getVersions.addHeader("X-Api-Token", token);

        CloseableHttpResponse response = httpClient.execute(getVersions);

        Scanner sc = new Scanner(response.getEntity().getContent());

        //TODO remove debug
        System.out.println(response.getStatusLine());

        String content = "";
        while(sc.hasNext()) {
            content += sc.nextLine();
        }

//        System.out.println(content);

        List<CurseGameVersionType> types = getGameVersionTypes();

        Gson gson = new GsonBuilder().create();
        CurseGameVersion[] versionArray = gson.fromJson(content, CurseGameVersion[].class);

        List<CurseGameVersion> versions = new ArrayList<>();
        for (CurseGameVersion v : versionArray) {
            if (isValidGameVersion(v, types)) {
                versions.add(v);
            }
        }

        return versions;

    }

    private static boolean isValidGameVersion(CurseGameVersion version, List<CurseGameVersionType> types) {
        for (CurseGameVersionType t : types) {
            if (t.id == version.gameVersionTypeID) {
                return true;
            }
        }
        return false;
    }

    public List<CurseGameVersionType> getGameVersionTypes() throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet getVersions = new HttpGet(getApiBaseUrl() + "/game/version-types");

        getVersions.addHeader("X-Api-Token", token);

        CloseableHttpResponse response = httpClient.execute(getVersions);

        String content = "";

        Scanner sc = new Scanner(response.getEntity().getContent());

        //TODO remove debug
        System.out.println(response.getStatusLine());

        while(sc.hasNext()) {
            content += sc.nextLine();
        }

        Gson gson = new GsonBuilder().create();
        CurseGameVersionType[] versionArray = gson.fromJson(content, CurseGameVersionType[].class);
        List<CurseGameVersionType> versions = Arrays.asList(versionArray);

        return versions;

    }

    public static class CurseGameVersion {
        public long id;
        public long gameVersionTypeID;
        public String name;
        public String slug;
    }

    public static class CurseGameVersionType {
        public long id;
        public String name;
        public String slug;
    }

}
