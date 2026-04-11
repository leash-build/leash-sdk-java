package build.leash.sdk.types;

/**
 * Parameters for sending a Gmail message.
 */
public class SendMessageParams {

    private String to;
    private String subject;
    private String body;
    private String cc;
    private String bcc;

    private SendMessageParams() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getTo() { return to; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getCc() { return cc; }
    public String getBcc() { return bcc; }

    public static class Builder {
        private final SendMessageParams params = new SendMessageParams();

        public Builder to(String to) { params.to = to; return this; }
        public Builder subject(String subject) { params.subject = subject; return this; }
        public Builder body(String body) { params.body = body; return this; }
        public Builder cc(String cc) { params.cc = cc; return this; }
        public Builder bcc(String bcc) { params.bcc = bcc; return this; }

        public SendMessageParams build() {
            if (params.to == null || params.to.isEmpty()) {
                throw new IllegalArgumentException("'to' is required");
            }
            if (params.subject == null || params.subject.isEmpty()) {
                throw new IllegalArgumentException("'subject' is required");
            }
            if (params.body == null || params.body.isEmpty()) {
                throw new IllegalArgumentException("'body' is required");
            }
            return params;
        }
    }
}
