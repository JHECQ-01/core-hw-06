package com.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JsonPlaceholderClient {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    private final HttpClient httpClient;
    private final Gson gson;

    public JsonPlaceholderClient() {
        this.httpClient = HttpClients.createDefault();
        this.gson = new Gson();
    }

    public JsonObject createUser(JsonObject user) throws IOException {
        String url = BASE_URL + "/users";
        HttpPost request = new HttpPost(url);
        request.setHeader("Content-type", "application/json");
        request.setEntity(new StringEntity(user.toString()));

        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        return gson.fromJson(responseString, JsonObject.class);
    }

    public JsonObject updateUser(int userId, JsonObject updatedUser) throws IOException {
        String url = BASE_URL + "/users/" + userId;
        HttpPatch request = new HttpPatch(url);
        request.setHeader("Content-type", "application/json");
        request.setEntity(new StringEntity(updatedUser.toString()));

        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        return gson.fromJson(responseString, JsonObject.class);
    }

    public boolean deleteUser(int userId) throws IOException {
        String url = BASE_URL + "/users/" + userId;
        HttpDelete request = new HttpDelete(url);

        HttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        return statusCode >= 200 && statusCode < 300;
    }

    public JsonObject[] getAllUsers() throws IOException {
        String url = BASE_URL + "/users";
        HttpGet request = new HttpGet(url);

        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        return gson.fromJson(responseString, JsonObject[].class);
    }

    public JsonObject getUserById(int userId) throws IOException {
        String url = BASE_URL + "/users/" + userId;
        HttpGet request = new HttpGet(url);

        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        return gson.fromJson(responseString, JsonObject.class);
    }

    public JsonObject getUserByUsername(String username) throws IOException {
        String url = BASE_URL + "/users?username=" + username;
        HttpGet request = new HttpGet(url);

        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        JsonObject[] users = gson.fromJson(responseString, JsonObject[].class);
        return users.length > 0 ? users[0] : null;
    }

    public void fetchAndSaveCommentsForLastPostOfUser(int userId) throws IOException {
        String postsUrl = BASE_URL + "/users/" + userId + "/posts";
        HttpGet postsRequest = new HttpGet(postsUrl);

        HttpResponse postsResponse = httpClient.execute(postsRequest);
        HttpEntity postsEntity = postsResponse.getEntity();
        String postsResponseString = EntityUtils.toString(postsEntity, "UTF-8");
        JsonObject[] posts = gson.fromJson(postsResponseString, JsonObject[].class);

        if (posts.length > 0) {
            int lastPostId = posts[posts.length - 1].get("id").getAsInt();
            String commentsUrl = BASE_URL + "/posts/" + lastPostId + "/comments";
            HttpGet commentsRequest = new HttpGet(commentsUrl);

            HttpResponse commentsResponse = httpClient.execute(commentsRequest);
            HttpEntity commentsEntity = commentsResponse.getEntity();
            String commentsResponseString = EntityUtils.toString(commentsEntity, "UTF-8");

            String fileName = "user-" + userId + "-post-" + lastPostId + "-comments.json";
            File file = new File(fileName);
            FileUtils.writeStringToFile(file, commentsResponseString, StandardCharsets.UTF_8);
        }
    }

    public static void main(String[] args) throws IOException {
        JsonPlaceholderClient client = new JsonPlaceholderClient();

        JsonObject newUser = new JsonObject();
        newUser.addProperty("name", "John Doe");
        newUser.addProperty("username", "johndoe");
        newUser.addProperty("email", "john.doe@example.com");
        JsonObject createdUser = client.createUser(newUser);
        System.out.println("Created user: " + createdUser);

        int userIdToUpdate = 1;
        JsonObject updatedUser = new JsonObject();
        updatedUser.addProperty("name", "Updated Name");
        updatedUser.addProperty("username", "updated_username");
        JsonObject updatedUserResponse = client.updateUser(userIdToUpdate, updatedUser);
        System.out.println("Updated user: " + updatedUserResponse);

        int userIdToDelete = 1;
        boolean deleteResult = client.deleteUser(userIdToDelete);
        System.out.println("Delete result: " + deleteResult);

        JsonObject[] allUsers = client.getAllUsers();
        System.out.println("All users: ");
        for (JsonObject user : allUsers) {
            System.out.println(user);
        }

        int userIdToFetch = 1;
        JsonObject userById = client.getUserById(userIdToFetch);
        System.out.println("User by id " + userIdToFetch + ": " + userById);

        String usernameToFetch = "Bret";
        JsonObject userByUsername = client.getUserByUsername(usernameToFetch);
        System.out.println("User by username " + usernameToFetch + ": " + userByUsername);

        int userIdToFetchComments = 1;
        client.fetchAndSaveCommentsForLastPostOfUser(userIdToFetchComments);
        System.out.println("Comments fetched and saved successfully.");
    }
}