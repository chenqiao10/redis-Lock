package com.redissionLock;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

public class ThreadWorker extends Thread {
	private String redisKey;
	private String hashKey;
	private boolean stopflag;
	RedisTemplate<String, String> redisTemplate;
	public ThreadWorker(String redisKey ,RedisTemplate<String, String> redisTemplate) {
		this.redisKey = redisKey;
		this.redisTemplate=redisTemplate;
	}
	 @Override
	public void run() {
	 while(stopflag!=false) {
			Boolean expire = redisTemplate.expire(redisKey, 60, TimeUnit.SECONDS);
			if(!expire) {
				break;
			}
		 try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
	 }
	}
	public String getRedisKey() {
		return redisKey;
	}
	public void setRedisKey(String redisKey) {
		this.redisKey = redisKey;
	}
	public String getHashKey() {
		return hashKey;
	}
	public void setHashKey(String hashKey) {
		this.hashKey = hashKey;
	}
	public boolean isStopflag() {
		return stopflag;
	}
	public void setStopflag(boolean stopflag) {
		this.stopflag = stopflag;
	}
}
