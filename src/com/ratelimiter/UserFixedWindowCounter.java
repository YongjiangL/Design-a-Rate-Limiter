package com.ratelimiter;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserFixedWindowCounter {
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock writeLock = readWriteLock.writeLock();
	private final Lock readLock = readWriteLock.readLock();

	long[] calls, timestamps;
	long limit;
	int windowTime;
	
	public UserFixedWindowCounter(long limit, int windowTime) {
		this.limit = limit;
		this.windowTime = windowTime;
		calls = new long[this.windowTime];
		timestamps = new long[this.windowTime];
	}
	
	public long getNumberOfCalls(long timestamp) {
		readLock.lock();
		try {
			long sum = 0;
			for (int i = 0; i < this.windowTime; i++) {
				if (timestamp-timestamps[i] < this.windowTime)
					sum += calls[i];
			}
			return sum;
		}
		finally {
			readLock.unlock();
		}	
	}
	
	public void addTime(long currentTime) {
		writeLock.lock();
		try {
			int hash = (int)(currentTime % this.windowTime);
			if (timestamps[hash] != currentTime) {
				timestamps[hash] = currentTime;
				calls[hash] = 1;
			}
			else
				calls[hash]++;
		}
		finally {
			writeLock.unlock();
		}
	}
	
	public boolean shouldAllowCall(long currentTime) {
		addTime(currentTime);
		return getNumberOfCalls(currentTime) <= limit;
	}
}
