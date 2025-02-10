package de.keksuccino.modrinth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.keksuccino.core.Nullable;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import java.io.File;
import java.util.*;

public class ModrinthProject {

    public Modrinth parent;

    public String id;
    public String slug;

    public boolean isValidProject() {
        return (id != null) && (slug != null);
    }

    /** Returns the version ID or NULL if something went wrong. **/
    @Nullable
    public String createVersion(ModrinthVersion version) {

        if (!isValidProject()) {
            System.out.println("ERROR: CREATE VERSION: CANNOT CREATE VERSION FOR INVALID PROJECT!");
            return null;
        }

        try {

            if ((version != null) && (version.files != null) && !version.files.isEmpty()) {

                for (File f : version.files) {
                    if (!f.isFile()) {
                        System.out.println("ERROR: CREATE VERSION: FILE NOT FOUND: " + f.getPath());
                        return null;
                    }
                }

                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpPost post = new HttpPost(Modrinth.VERSION_URL);
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setContentType(ContentType.MULTIPART_FORM_DATA);

                post.addHeader("User-Agent", Modrinth.USER_AGENT);
                post.addHeader("Authorization", this.parent.token);

                final JSONObject json = new JSONObject();

                int filePart = 0;
                Map<String, File> fileParts = new HashMap<>();
                for (File f : version.files) {
                    String name = "" + filePart;
                    fileParts.put(name, f);
                    filePart++;
                }

                json.put("name", version.name);
                json.put("version_number", version.version_number);
                json.put("changelog", version.changelog);
                if (version.dependencies != null) {
                    List<JSONObject> deps = new ArrayList<>();
                    for (ModrinthDependency d : version.dependencies) {
                        ModrinthProject mp = null;
                        if (d.project_id != null) {
                            mp = this.parent.getProject(d.project_id);
                            if ((mp == null) && (d.project_id != null) && (d.project_id.endsWith("-forge") || d.project_id.endsWith("-fabric"))) {
                                mp = this.parent.getProject(d.project_id.replace("-forge", "").replace("-fabric", ""));
                            }
                        }
                        JSONObject o = new JSONObject();
                        o.put("version_id", d.version_id);
                        if (mp != null) {
                            o.put("project_id", mp.id);
                        } else {
                            o.put("project_id", d.project_id);
                        }
                        o.put("file_name", d.file_name);
                        o.put("dependency_type", d.dependency_type);
                        deps.add(o);
                    }
                    json.put("dependencies", deps.toArray(new JSONObject[0]));
                } else {
                    json.put("dependencies", (JSONObject[]) null);
                }
                json.put("game_versions", version.game_versions);
                json.put("version_type", version.version_type);
                json.put("loaders", version.loaders);
                json.put("featured", version.featured);
                json.put("project_id", this.id);
                json.put("file_parts", fileParts.keySet().toArray(new String[0]));
                json.put("primary_file", version.primary_file);

                System.out.println("JSON:" + json.toString());

                builder.addTextBody("data", json.toString());

                for (Map.Entry<String, File> m : fileParts.entrySet()) {
                    builder.addBinaryBody(m.getKey(), m.getValue());
                }

                HttpEntity multipart = builder.build();
                post.setEntity(multipart);
                CloseableHttpResponse response = httpClient.execute(post);

                Scanner sc = new Scanner(response.getEntity().getContent());

                //TODO remove debug
                System.out.println("createVersion response status: " + response.getStatusLine());

                String content = "";
                while(sc.hasNext()) {
                    content += sc.nextLine();
                }

                //TODO remove debug
                System.out.println("createVersion response: " + content);

                Gson gson = new GsonBuilder().create();
                ModrinthCreateVersionResponse createResponse = gson.fromJson(content, ModrinthCreateVersionResponse.class);

                if (createResponse != null) {
                    return createResponse.id;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

}
