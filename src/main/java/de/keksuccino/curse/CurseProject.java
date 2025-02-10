package de.keksuccino.curse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CurseProject {

    public CurseForge parent;
    public long projectId;

    public CurseProject(long projectId, CurseForge parent) {
        this.projectId = projectId;
        this.parent = parent;
    }

    /**
     * Upload a file to a project.
     * @return Returns the new file ID or -10000 if the upload failed.
     */
    public long uploadFile(CurseFile file) {

        try {

            if ((file != null) && (file.file != null) && file.file.isFile()) {

                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpPost uploadFile = new HttpPost(this.parent.getApiBaseUrl() + "/projects/" + projectId + "/upload-file");
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();

                uploadFile.addHeader("X-Api-Token", this.parent.token);

                final JSONObject json = new JSONObject();
                json.put("changelog", file.changelog);
                json.put("changelogType", file.changelogType.name);
                json.put("displayName", file.displayName);
                if (file.parentFileID != -10000) {
                    json.put("parentFileID", file.parentFileID);
                }
                if ((file.parentFileID == -10000) && !file.gameVersions.isEmpty()) {
                    json.put("gameVersions", file.gameVersions.toArray(new Long[0]));
                }
                json.put("releaseType", file.releaseType.name);

                if (!file.relations.isEmpty()) {

                    JSONObject relations = new JSONObject();

                    List<JSONObject> relationObjects = new ArrayList<>();
                    for (ProjectRelation r : file.relations) {
                        JSONObject relation = new JSONObject();
                        relation.put("slug", r.slug);
                        relation.put("type", r.relationType.name);
                        relationObjects.add(relation);
                    }

                    relations.put("projects", relationObjects.toArray(new JSONObject[0]));

                    json.put("relations", relations);

                }

                builder.addTextBody("metadata", json.toString(), ContentType.APPLICATION_JSON);

                builder.addBinaryBody("file", new FileInputStream(file.file), ContentType.APPLICATION_OCTET_STREAM, file.file.getName());

                HttpEntity multipart = builder.build();
                uploadFile.setEntity(multipart);
                CloseableHttpResponse response = httpClient.execute(uploadFile);

                Scanner sc = new Scanner(response.getEntity().getContent());

                //TODO remove debug
                System.out.println("uploadFile response status: " + response.getStatusLine());

                String content = "";
                while(sc.hasNext()) {
                    content += sc.nextLine();
                }

                //TODO remove debug
                System.out.println("uploadFile response: " + content);

                Gson gson = new GsonBuilder().create();
                CurseFileUploadResponse uploadResponse = gson.fromJson(content, CurseFileUploadResponse.class);

                if (uploadResponse != null) {
                    return uploadResponse.id;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -10000L;

    }

    public static class CurseFileUploadResponse {
        public long id;
    }

}
