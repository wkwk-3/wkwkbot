package wkwk;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.signature.TwitterCredentials;

import java.text.SimpleDateFormat;
import java.util.*;

public class AutoTweet {
    int oldTime = -1;
    TwitterClient twitterClient;
    String tweetText = "困ったことがあったら、下記サーバーに参加し\n" +
            "気軽にご質問ください。\n" +
            "BOT招待 : \n" +
            "https://wkb.page.link/bot\n" +
            "サーバー招待 :\n" +
            "https://wkb.page.link/guild\n" +
            "::emoji::\n" +
            "#wkwkbot #Discord #bot #通話管理";
    String[] emojis = {"🌝","🌑","🌒","🌓","🌔","🌕","🌖","🌗","🌘","🌙","🌚","🌛","🌜","☀","🌞","⭐","🌟","🌠","☄","🌈","☂","❄","🔥","💧"};
    TimerTask task;
    Timer timer = new Timer();
    public AutoTweet(TweetAPIList list){
        twitterClient = new TwitterClient(TwitterCredentials.builder()
                .accessToken(list.getToken())
                .accessTokenSecret(list.getTokenSecret())
                .apiKey(list.getApi())
                .apiSecretKey(list.getApiSecret())
                .build());
    }
    public void start(){
        task = new TimerTask() {
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("HH");
                Date date = new Date();
                int newTime = Integer.parseInt(sdf.format(date));
                if (oldTime != newTime) {
                    oldTime = newTime;
                    tweetText = tweetText.replaceFirst("::emoji::", emojis[newTime]);
                    twitterClient.postTweet(tweetText);
                }
            }
        };
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY,1);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        timer.schedule(task, calendar.getTime() ,3600000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        int newTime = Integer.parseInt(sdf.format(date));
        tweetText = tweetText.replaceFirst("::emoji::", emojis[newTime]);
        twitterClient.postTweet(tweetText);
        System.out.println(calendar.getTime()+"にタイマー開始");
    }
    public void stop(){
        timer.cancel();
        System.out.println(new Date()+"にタイマー停止");
    }

}
