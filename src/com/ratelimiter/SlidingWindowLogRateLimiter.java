package com.ratelimiter;

import java.util.concurrent.ConcurrentHashMap;

public class SlidingWindowLogRateLimiter {
	ConcurrentHashMap<String, UserRequestLog> userMap = new ConcurrentHashMap<String, UserRequestLog>();
	public final long RATE_LIMIT = 100;
	public final long WINDOW_SIZE = 60;
	
	public SlidingWindowLogRateLimiter() {
	}
	
	public void addUser(String userId, long limit, long windowTime) {
		userMap.putIfAbsent(userId, new UserRequestLog(limit, windowTime));
	}
	
	public void removeUser(String userId) {
		userMap.remove(userId);
	}
	
	public long getCurrentTime() {
		return System.currentTimeMillis();
	}
	
	public boolean shouldAllowCall(String userId) {
		if (!userMap.containsKey(userId))
			addUser(userId, this.RATE_LIMIT, this.WINDOW_SIZE);
		
		UserRequestLog userLog = userMap.get(userId);
		long currentTime = this.getCurrentTime();
		//remove all outdated timestamps
		userLog.evitOutdatedTimes(currentTime);
		userLog.addTime(currentTime);
		return userLog.isLogFull();
	}
}
