package wkwk.record;

import lombok.Data;

@Data
public class TweetAPIRecord {

    private String token;
    private String tokenSecret;
    private String api;
    private String apiSecret;

    public TweetAPIRecord() {
        token = null;
        tokenSecret = null;
        api = null;
        apiSecret = null;
    }
}