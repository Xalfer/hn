package hn;

import java.awt.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.stream.*;
import javax.swing.*;
import org.json.*;

/**
 * An item parsed from json that the Hacker News api returns.
 * <p>
 * The documentation (https://github.com/HackerNews/API) says that every field except `id` is optional.
 * It doesn't storgly specify what types of items are expected to have what fields, so I cannot excpect 
 * anything and thus splitting different items into their own types is not possible (or at least that's 
 * what I understood).
 */
public class Item {
    /**
     * The id of the item. Must always be present.
     */
    public final int id;
    /**
     * Whether the item has been deleted. If it is not present in json this is parsed from, 
     * it defaults to false.
     */
    public final boolean deleted;
    /**
     * Type of this item.
     */
    public final Optional<Type> type;
    /**
     * Poster of this item.
     */
    public final Optional<String> by;
    /**
     * Time of this items posting. Stored in utix time.
     */
    public final Optional<Integer> time;
    /**
     * The text content of the item.
     */
    public final Optional<String> text;
    /**
     * Whether the item is dead.
     */
    public final boolean dead;
    /**
     * Parent of this item. Should probably be present only if this is a comment.
     */
    public final Optional<Integer> parent;
    /**
     * The poll this is a part of. Should probably be present only if this is a pollopt.
     */
    public final Optional<Integer> poll;
    /**
     * This items comments.
     */
    public final Optional<List<Integer>> kids;
    /**
     * The url of this story.
     */
    public final Optional<URI> url;
    /**
     * Score of this item.
     */
    public final Optional<Integer> score;
    /**
     * The title of this item.
     */
    public final Optional<String> title;
    /**
     * List of related pollopts. Should probably be present only if this is a poll.
     */
    public final Optional<List<Integer>> parts;
    /**
     * Comment count. Should probably be present only if this is a story or poll.
     */
    public final Optional<Integer> descendants;

    /**
     * A type of the item.
     */
    public enum Type {
        /**
         * a job posting
         */
        JOB, 
        /**
         * a story
         */
        STORY, 
        /**
         * a comment on a comment or a poll
         */
        COMMENT, 
        /**
         * a poll
         */
        POLL, 
        /**
         * a option that can be selected on a poll
         */
        POLLOPT,
    }

    /**
     * Creates an item from the specified json.
     *
     * @param json the json that this item is supposed to be parsed from
     */
    public Item(JSONObject json) throws JSONException {
        this.id = json.getInt("id");
        this.deleted = json.optBoolean("deleted", false);
        this.type = getOptionalString(json, "type").map(type -> Type.valueOf(type.toUpperCase()));
        this.by = getOptionalString(json, "by");
        this.time = getOptionalInt(json, "time");
        this.text = getOptionalString(json, "text");
        this.dead = json.optBoolean("dead", false);
        this.parent = getOptionalInt(json, "parent");
        this.poll = getOptionalInt(json, "poll");
        this.kids = getOptionalInts(json, "kids");
        this.url = getOptionalString(json, "url").flatMap(str -> {
                try {
                    return Optional.of(new URI(str));
                } catch (Exception e) {
                    return Optional.empty();
                }
            }
        );
        this.score = getOptionalInt(json, "score");
        this.title = getOptionalString(json, "title");
        this.parts = getOptionalInts(json, "parts");
        this.descendants = getOptionalInt(json, "descendants");
    }

    private static Optional<String> getOptionalString(JSONObject json, String name) {
        try {
            return Optional.of(json.getString(name));
        } catch (JSONException e) {
            return Optional.empty();
        }
    }
    private static Optional<Integer> getOptionalInt(JSONObject json, String name) {
        try {
            return Optional.of(json.getInt(name));
        } catch (JSONException e) {
            return Optional.empty();
        }
    }
    private static Optional<List<Integer>> getOptionalInts(JSONObject json, String name) {
        try {
            return Optional.of(json.getJSONArray(name)
                .toList()
                .stream()
                .map(Object::toString)
                .map(Integer::parseInt)
                .collect(Collectors.toList()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
