package build.leash.integrations.calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** One attendee on a calendar event. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CalendarAttendee {

    @JsonProperty("email")
    private final String email;

    @JsonProperty("responseStatus")
    private final String responseStatus;

    public CalendarAttendee(
            @JsonProperty("email") String email,
            @JsonProperty("responseStatus") String responseStatus) {
        this.email = email;
        this.responseStatus = responseStatus;
    }

    public CalendarAttendee(String email) {
        this(email, null);
    }

    public String getEmail() { return email; }
    public String getResponseStatus() { return responseStatus; }
}
