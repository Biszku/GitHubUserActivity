import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GitHubActivity {
    private final String GITHUB_API_URL = "https://api.github.com";

    private final String userName;

    public GitHubActivity(String userName) {
        this.userName = userName;
    }

    public void displayEvents() {
        JsonNode events = getEvents();
        if (events == null) return;
        printEvents(events);
    }

    private JsonNode getEvents() {
        try {
            HttpResponse<String> response = fetchEvents();
            if (response.statusCode() == 404) {
                System.out.println("\u001B[31m" + "User not found!\u001B[0m");
            }
            if (response.statusCode() == 200) {
                return parseJson(response.body());
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

    private JsonNode parseJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(json);
    }

    private void printEvents(JsonNode events) {

        if (events.isEmpty()) {
            System.out.println("No recent activity found!");
            return;
        }

        for (JsonNode event : events) {
            String action = createAction(event);
            System.out.println(action);
        }
    }

    private String createAction(JsonNode event) {
        String type = event.get("type").asText();
        return switch (type) {
            case "PushEvent" -> createPushEventMessage(event);
            case "IssuesEvent" -> createIssuesEventMessage(event);
            case "WatchEvent" -> createWatchEventMessage(event);
            case "ForkEvent" -> createForkEventMessage(event);
            case "CreateEvent" -> createCreateEventMessage(event);
            default -> createUnknownEventMessage(event);
        };
    }

    private String createPushEventMessage(JsonNode event) {
        int commitCount = event.get("payload").get("size").asInt();
        return "Pushed " + commitCount + " commit(s) to " + event.get("repo").get("name");
    }

    private String createIssuesEventMessage(JsonNode event) {
        return event.get("payload").get("action").asText().toUpperCase().charAt(0)
                + event.get("payload").get("action").asText() + " an issue in ${event.repo.name}";
    }

    private String createWatchEventMessage(JsonNode event) {
        return "Starred " + event.get("repo").get("name").asText();
    }

    private String createForkEventMessage(JsonNode event) {
        return "Forked " + event.get("repo").get("name").asText();
    }

    private String createCreateEventMessage(JsonNode event) {
        return "Created " + event.get("payload").get("ref_type").asText()
                + " in " + event.get("repo").get("name").asText();
    }

    private String createUnknownEventMessage(JsonNode event) {
        return event.get("type").asText().replace("Event", "")
                + " in " + event.get("repo").get("name").asText();
    }
}