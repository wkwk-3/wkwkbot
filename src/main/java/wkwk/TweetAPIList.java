package wkwk;

import lombok.Data;

@Data
public class TweetAPIList {

    private String token;
    private String tokenSecret;
    private String api;
    private String apiSecret;

    public TweetAPIList(){
        token = null;
        tokenSecret = null;
        api = null;
        apiSecret = null;
    }
}