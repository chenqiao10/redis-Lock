package com.redissionLock.service.impl;

import java.util.List;

public interface ITestService {

	boolean getLock(String key, String requestId);

	boolean releaseLock(String lockKey, String requestId);

	List<String> getMap();
}
