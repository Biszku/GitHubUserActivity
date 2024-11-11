import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GitHubService {
    private final String GITHUB_API_URL = "https://api.github.com";

    private final String userName;

    public GitHubService(String userName) {
        this.userName = userName;
    }

    public void printEvents() {
        JsonArray events = getEvents();
        if (events == null) return;
        printEvents(events);
    }

    private JsonArray getEvents() {
        try {
            HttpResponse<String> response = fetchEvents();
            if (response.statusCode() == 404) {
                System.out.println("\u001B[31m" + "User not found!\u001B[0m");
            }
            if (response.statusCode() == 200) {
                return JsonParser.parseString(response.body()).getAsJsonArray();
            } else {
                System.out.println("\u001B[31m" + "An error occurred!\u001B[0m");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return null;
    }

    private HttpResponse<String> fetchEvents() throws IOException, InterruptedException {
        String url = createUrl();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String createUrl() {
        return GITHUB_API_URL+"/users/%s/events".formatted(userName);
    }

    private void printEvents(JsonArray events) {

        if (events.isEmpty()) {
            System.out.println("No recent activity found!");
            return;
        }

        for (JsonElement event : events) {
            String action = createAction(event.getAsJsonObject());
            System.out.println(action);
        }
    }

    private String createAction(JsonObject event) {
        String type = event.get("type").getAsString();
        return switch (type) {
            case "PushEvent" -> createPushEventMessage(event);
            case "IssuesEvent" -> createIssuesEventMessage(event);
            case "WatchEvent" -> createWatchEventMessage(event);
            case "ForkEvent" -> createForkEventMessage(event);
            case "CreateEvent" -> createCreateEventMessage(event);
            default -> createUnknownEventMessage(event);
        };
    }

    private String createPushEventMessage(JsonObject event) {
        int commitCount = event.get("payload").getAsJsonObject().get("size").getAsInt();
        return "Pushed " + commitCount + " commit(s) to " + event.get("repo").getAsJsonObject().get("name");
    }

    private String createIssuesEventMessage(JsonObject event) {
        return event.get("payload").getAsJsonObject().get("action").getAsString().toUpperCase().charAt(0)
                + event.get("payload").getAsJsonObject().get("action").getAsString()
                + " an issue in ${event.repo.name}";
    }

    private String createWatchEventMessage(JsonObject event) {
        return "Starred " + event.get("repo").getAsJsonObject().get("name").getAsString();
    }

    private String createForkEventMessage(JsonObject event) {
        return "Forked " + event.get("repo").getAsJsonObject().get("name").getAsString();
    }

    private String createCreateEventMessage(JsonObject event) {
        return "Created " + event.get("payload").getAsJsonObject().get("ref_type").getAsString()
                + " in " + event.get("repo").getAsJsonObject().get("name").getAsString();
    }

    private String createUnknownEventMessage(JsonObject event) {
        return event.get("type").getAsString().replace("Event", "")
                + " in " + event.get("repo").getAsJsonObject().get("name").getAsString();
    }
}