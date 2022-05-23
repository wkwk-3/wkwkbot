package wkwk.twitterSystem;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import wkwk.csv.TweetDataLoad;
import wkwk.parameter.record.TweetAPIRecord;

import java.text.SimpleDateFormat;
import java.util.*;

public class AutoTweet {
    TweetDataLoad csvReader = new TweetDataLoad();
    int oldTime = -1;
    TwitterClient twitterClient;
    String tweetText = csvReader.getTweetTemplate();
    List<String> emojis = csvReader.getTweetEmojis();
    TimerTask task;
    Timer timer;

    public AutoTweet(TweetAPIRecord list) {
        System.out.println(tweetText);
        twitterClient = new TwitterClient(TwitterCredentials.builder()
                .accessToken(list.getToken())
                .accessTokenSecret(list.getTokenSecret())
                .apiKey(list.getApi())
                .apiSecretKey(list.getApiSecret())
                .build());
    }

    public void start() {
        task = new TimerTask() {
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("H");
                Date date = new Date();
                int newTime = Integer.parseInt(sdf.format(date));
                if (oldTime != newTime) {
                    oldTime = newTime;
                    String tweet = tweetText.replaceFirst("::emoji::", emojis.get(newTime));
                    twitterClient.postTweet(tweet);
                    System.out.println(date + "にツイート");
                }
            }
        };
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        timer = new Timer();
        timer.schedule(task, calendar.getTime(), 43200000L);
        System.out.println(calendar.getTime() + "にタイマー開始");
    }

    public void stop() {
        timer.cancel();
        System.out.println(new Date() + "にタイマー停止");
    }
}
