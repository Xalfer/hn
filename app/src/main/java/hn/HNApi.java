package hn;

import java.net.*;
import java.net.http.*;
import java.lang.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import java.io.*;
import org.json.*;

/**
 * Utility functions for working with the Hacker News api. This class contains all of the networking 
 * side effects, that are neccessary for this program.
 */
public final class HNApi {
    /**
     * The base uri of the api.
     */
    public static final String baseUri = "https://hacker-news.firebaseio.com/v0/";

    /**
     * Asynchronously get the current top stories on Hacker News.
     * <p>
     * After retrieving the ids, it fetches the individual items, from which job postings are filltered out.
     * The remaining items are converted to stories and added to the returned queue.
     *
     * @return queue to which the stories will be added
     */
    public static BlockingQueue<Story> getTopStoriesAsync() {
        BlockingQueue<Story> stories = new SynchronousQueue<>();

        new Thread(() -> {
            HttpClient client = HttpClient.newBuilder().build();
            List<Integer> itemIds;
            try {
                itemIds = HNApi.getTopItemIds(client);
            } catch (Exception e) {
                System.err.println("Cannot get top item ids: " + e.getMessage());
                return;
            }

            Queue<Future<Item>> itemFutures = new LinkedList<>();

            int i;
            for (i = 0; i < Integer.min(5, itemIds.size()); i++) {
                try {
                    itemFutures.add(getItemAsync(client, itemIds.get(i)));
                } catch (URISyntaxException e) {
                    System.err.println("Cannot create URI: " + e.getMessage());
                    continue;
                }
            }
            while (!itemFutures.isEmpty()) {
                // Add new item request to queue.
                try {
                    itemFutures.add(getItemAsync(client, itemIds.get(i++)));
                } catch (URISyntaxException e) {
                    System.err.println("Cannot create URI: " + e.getMessage());
                } catch (IndexOutOfBoundsException e) {
                    // If the item id does not exist then we have ran out of items that have not been requested yet,
                    // so we continue with execution.
                }

                Future<Item> response = itemFutures.poll();
                if (response == null) {
                    continue;
                }
                Item item;
                try {
                    item = response.get();
                } catch (Exception e) {
                    System.err.println("Cannot get item: " + e.getMessage());
                    continue;
                }
                if (item.type.filter(t -> t == Item.Type.JOB).isPresent()) {
                    continue;
                }
                try {
                    stories.put(new Story(item));
                } catch (Exception e) {
                    System.err.println("Cannot put story into queue: " + e.getMessage());
                    return;
                }
            }
        }).start();

        return stories;
    }

    /**
     * Retrievs the ids of the current 500 top stories on Hacker News.
     *
     * @return A list containing the ids
     */
    private static List<Integer> getTopItemIds(HttpClient client) 
    throws IOException, InterruptedException, URISyntaxException {
        URI uri = new URI(baseUri + "topstories.json");
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

        try {
            List<Object> unparsed = new JSONArray(response.body().toString()).toList();
            List<Integer> ints = unparsed
                .stream()
                .map(x -> Integer.parseInt(x.toString()))
                .collect(Collectors.toList());
            return ints;
        } catch (Exception e) {
            System.err.println("Cannot parse recieved story ids: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Creates a future that will retrieve the item with a specified id.
     *
     * @param client a client to use to send the http requests
     * @param id the id of the item which is requested
     * @return future that will result in the item
     * @throws URISyntaxException if cannot create a uri with the specified id
     * @see #getItemAsync(HttpClient client, String id)
     */
    public static Future<Item> getItemAsync(HttpClient client, int id) throws URISyntaxException {
        return getItemAsync(client, Integer.toString(id));
    }

    /**
     * Creates a future that will retrieve the item with a specified id.
     *
     * @param client a client to use to send the http requests
     * @param id the id of the item which is requested
     * @return future that will result in the item
     * @throws URISyntaxException if cannot create a uri with the specified id
     */
    public static Future<Item> getItemAsync(HttpClient client, String id) throws URISyntaxException {
        URI uri = new URI(baseUri + "item/" + id + ".json");
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(
            request, 
            HttpResponse.BodyHandlers.ofString()
        );
        return response
            .thenApply(HttpResponse::body)
            .thenApply(Object::toString)
            .thenApply(JSONObject::new)
            .thenApply(Item::new);
    }

    private HNApi() {
        throw new UnsupportedOperationException("HNApi should never be instantiated.");
    }
}
