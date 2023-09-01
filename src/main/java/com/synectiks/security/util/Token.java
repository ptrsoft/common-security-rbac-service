package com.synectiks.security.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Token {

    public static Map<String, Message> cache = new HashMap<>();
    public static long DEFAULT_TIME_TO_EXPIRE = 3L;
//    public static void main(String a[]) throws InterruptedException {
//        Message msg = new Message("kmi", LocalDateTime.now(), DEFAULT_TIME_TO_EXPIRE);
//        put("abc@gmail.com", msg);
//    }

    public static void put(String key, String token)  {
        Message msg = new Message(token);
        cache.put(key, msg);
//        boolean isExpired = get(key, LocalDateTime.now());
//        System.out.println(isExpired);
//        Thread.currentThread().sleep(2000);
//
//        isExpired = get(key, LocalDateTime.now());
//        System.out.println(isExpired);
    }

    public static String get(String key) {
        if (!cache.containsKey(key)) {
            return null;
        }
        Message msg = cache.get(key);
        return msg.getCode();
//        Duration duration = Duration.between(msg.getDateTime(), newTIme);
//        System.out.println("duration: " + duration);
//        System.out.println("seconds : " + duration.getSeconds());
//
//        if (duration.getSeconds() > DEFAULT_TIME_TO_EXPIRE) {
//            return false;
//        }
//        return true;
    }

    public static void remove(String key){
        if(cache.containsKey(key)){
            cache.remove(key);
        }
    }
    private static class Message {
        String code;
        LocalDateTime dateTime;
        long minsToExpire;
        public Message(String code, LocalDateTime dateTime, long minsToExpire) {
            this.code = code;
            this.dateTime = dateTime;
            this.minsToExpire = minsToExpire;
        }
        public Message(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
        public LocalDateTime getDateTime() {
            return dateTime;
        }
        public long getMinsToExpire() {
            return minsToExpire;
        }
    }
}
