package build.leash.sdk;

/**
 * Represents an authenticated Leash user extracted from a JWT token.
 */
public class LeashUser {

    private final String id;
    private final String email;
    private final String name;
    private final String picture;

    public LeashUser(String id, String email, String name, String picture) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.picture = picture;
    }

    /** Returns the user's unique identifier. */
    public String getId() {
        return id;
    }

    /** Returns the user's email address. */
    public String getEmail() {
        return email;
    }

    /** Returns the user's display name. */
    public String getName() {
        return name;
    }

    /** Returns the URL of the user's profile picture. */
    public String getPicture() {
        return picture;
    }
}
