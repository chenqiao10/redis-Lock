package com.redissionLock.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redissionLock.ThreadWorker;
import com.redissionLock.service.impl.ITestService;

@RestController
public class TestController {
	@Resource
	RedisTemplate<String, String> redisTemplate;
	String redisKey = "basedata:productKey";
	String number = "basedata:number";
	String hashKey = "lockKey";
	@Autowired
	private RedissonClient redissonClient;
	@Autowired
	private ITestService testService;
	static List<String> list = new ArrayList<>();

	@RequestMapping("/addProduct")

	public void addProduct() throws RuntimeException {
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		redisTemplate.opsForValue().set(number, 10 + "");
	}

	@RequestMapping("/addLock")
	public void addLock() throws RuntimeException {
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		try {
			long num = redisTemplate.opsForValue().increment(redisKey, 1);
			/*
			 * ThreadWorker worker=new ThreadWorker(redisKey,redisTemplate); worker.start();
			 */
			if (num == 1) {
				int stack = Integer.parseInt(redisTemplate.opsForValue().get(number));
				if (stack > 1) {
					redisTemplate.opsForValue().set(number, (stack - 1) + "");
					System.out.println("成功" + (stack - 1));
				} else {
					System.out.println("失败");
				}
			}
		} finally {
			redisTemplate.opsForValue().increment(redisKey, -1);
			// redisTemplate.delete(redisKey);
		}
	}

	@RequestMapping("/addSetNexLock")
	public void addSetNexLock() throws RuntimeException {
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		ThreadWorker worker = null;
		Boolean setIfAbsent = false;
		try {
			setIfAbsent = redisTemplate.opsForValue().setIfAbsent(redisKey, "1");
			worker = new ThreadWorker(redisKey, redisTemplate);
			worker.start();
			if (setIfAbsent) {
				int stack = Integer.parseInt(redisTemplate.opsForValue().get(number));
				if (stack > 1) {
					redisTemplate.opsForValue().set(number, (stack - 1) + "");
					System.out.println("成功" + (stack - 1));
				} else {
					System.out.println("失败");
				}
			} else {
				System.out.println("枪锁失败");
			}
		} finally {
			if (setIfAbsent) {
				worker.setStopflag(true);
				redisTemplate.delete(redisKey);
			}
		}
	}

	@RequestMapping("/addRedissionLock")
	public void addRedissionLock() throws RuntimeException, InterruptedException {
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		/*
		 * HashOperations<String, Object, Object> opsForHash =
		 * redisTemplate.opsForHash(); opsForHash.putIfAbsent(redisKey, hashKey, 1);
		 */
		// 创建锁对象，并制定锁的名称
		RLock tasklock = redissonClient.getLock("taskLock");
		// 获取锁,设置自动失效时间为50s
		boolean isLock = tasklock.tryLock(50, TimeUnit.SECONDS);
		try {
			if (isLock) {
				int stack = Integer.parseInt(redisTemplate.opsForValue().get(number));
				if (stack > 1) {
					redisTemplate.opsForValue().set(number, (stack - 1) + "");
					System.out.println("成功" + (stack - 1));
				} else {
					System.out.println("失败");
				}
			}
		} finally {
			tasklock.unlock();
		}
	}

	@RequestMapping("/addSLock")
	public void addSLock() throws RuntimeException {
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());

		synchronized (this) {
			int stack = Integer.parseInt(redisTemplate.opsForValue().get(number));
			if (stack > 1) {
				redisTemplate.opsForValue().set(number, (stack - 1) + "");
				System.out.println("成功" + (stack - 1));
			} else {
				System.out.println("失败");
			}

		}
	}

	@RequestMapping("/addSetNexLock1")
	public void addSetNexLock1() throws RuntimeException {
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		ThreadWorker worker = null;
		Boolean setNX = getLock(redisTemplate);
		try {
			worker = new ThreadWorker(redisKey, redisTemplate);
			worker.start();
			if (setNX) {
				int stack = Integer.parseInt(redisTemplate.opsForValue().get(number));
				if (stack > 1) {
					redisTemplate.opsForValue().set(number, (stack - 1) + "");
					System.out.println("成功" + (stack - 1));
				} else {
					System.out.println("失败");
				}
			} else {
				list.add("1");
			}
		} finally {
			if (setNX) {
				worker.setStopflag(true);
				redisTemplate.delete(redisKey);
			}
		}
	}

	private Boolean getLock(RedisTemplate<String, String> redisTemplate) {
		RedisConnectionFactory setIfAbsent = redisTemplate.getConnectionFactory();
		Boolean setNX = setIfAbsent.getConnection().setNX(redisKey.getBytes(), "1".getBytes());
		while (true) {
			if (!setNX) {
				setNX = setIfAbsent.getConnection().setNX(redisKey.getBytes(), "1".getBytes());
			} else {
				break;
			}
		}
		return setNX;

	}

	@RequestMapping("/addSetNexLock2")
	public void addSetNexLock2() throws RuntimeException {
		System.out.println(list.size());
	}

}
