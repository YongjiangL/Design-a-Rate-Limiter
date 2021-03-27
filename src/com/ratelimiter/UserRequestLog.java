package com.ratelimiter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserRequestLog {
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock writeLock = readWriteLock.writeLock();
	private final Lock readLock = readWriteLock.readLock();

	Deque<Long> timestamps;
	long limit;
	long windowTime;
	
	public UserRequestLog(long limit, long windowTime) {
		timestamps = new ArrayDeque<Long>();
		this.limit = limit;
		this.windowTime = windowTime;
	}
	
	public boolean isLogFull() {
		readLock.lock();
		try {
			return timestamps.size() <= limit;
		}
		finally {
			readLock.unlock();
		}
	}
	
	public void addTime(long currentTime) {
		writeLock.lock();
		try {
			timestamps.offer(currentTime);
		}
		finally {
			writeLock.unlock();
		}
	}

	public void evitOutdatedTimes(long currentTime) {
		writeLock.lock();
		try {
			while (!timestamps.isEmpty() && (currentTime-timestamps.peekFirst() > this.windowTime)) {
				timestamps.pollFirst();
			}
		}
		finally {
			writeLock.unlock();
		}
	}
}
