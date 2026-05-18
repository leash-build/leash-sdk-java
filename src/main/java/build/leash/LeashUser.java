package build.leash;

import java.util.Objects;
import java.util.Optional;

/**
 * Authenticated Leash user — mirror of the TS {@code LeashUser} interface
 * and Python {@code LeashUser} dataclass.
 *
 * <p>Immutable value type. Use {@link #builder()} to construct or, more
 * commonly, obtain via {@code leash.auth().user()}.
 */
public final class LeashUser {

    private final String id;
    private final String email;
    private final String name;
    private final String picture;

    private LeashUser(String id, String email, String name, String picture) {
        this.id = Objects.requireNonNull(id, "id");
        this.email = email == null ? "" : email;
        this.name = name == null ? "" : name;
        this.picture = picture;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    /** Optional avatar URL — present only when the platform's JWT carried one. */
    public Optional<String> getPicture() {
        return Optional.ofNullable(picture);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof LeashUser)) return false;
        LeashUser that = (LeashUser) other;
        return id.equals(that.id)
                && email.equals(that.email)
                && name.equals(that.name)
                && Objects.equals(picture, that.picture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, name, picture);
    }

    @Override
    public String toString() {
        return "LeashUser{id=" + id + ", email=" + email + ", name=" + name + "}";
    }

    public static final class Builder {
        private String id;
        private String email;
        private String name;
        private String picture;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder picture(String picture) {
            this.picture = picture;
            return this;
        }

        public LeashUser build() {
            return new LeashUser(id, email, name, picture);
        }
    }
}
