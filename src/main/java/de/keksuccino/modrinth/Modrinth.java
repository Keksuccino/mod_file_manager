package de.keksuccino.modrinth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.keksuccino.core.Nullable;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.util.Scanner;

public class Modrinth {

    public static final String USER_AGENT = "Helper tool for mod authors (upload files, etc.) for personal use | Contact: https://github.com/Keksuccino";

    //https://api.modrinth.com/v2/version
    public static final String BASE_URL = "https://api.modrinth.com/v2";
    public static final String VERSION_URL = BASE_URL + "/version";
    public static final String PROJECT_URL = BASE_URL + "/project";

    public final String token;

    public Modrinth(String token) {
        this.token = token;
    }

    //https://api.modrinth.com/v2/project/konkrete
    @Nullable
    public ModrinthProject getProject(String idOrSlug) {

        try {

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet get = new HttpGet(PROJECT_URL + "/" + idOrSlug);

            get.addHeader("User-Agent", USER_AGENT);
            get.addHeader("Authorization", token);

            CloseableHttpResponse response = httpClient.execute(get);

            Scanner sc = new Scanner(response.getEntity().getContent());

            String content = "";
            while(sc.hasNext()) {
                content += sc.nextLine();
            }

            Gson gson = new GsonBuilder().create();
            ModrinthProject project = gson.fromJson(content, ModrinthProject.class);

            if ((project != null) && project.isValidProject()) {
                //TODO remove debug
                System.out.println("############################### PROJECT ID: " + project.id);
                project.parent = this;
                return project;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

}
