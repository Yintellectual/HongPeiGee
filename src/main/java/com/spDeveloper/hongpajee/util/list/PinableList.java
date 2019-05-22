package com.spDeveloper.hongpajee.util.list;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PinableList {

	List<String> pined = new ArrayList<>();
	List<String> unpined = new ArrayList<>();
	ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	public List<String> getAll() throws InterruptedException, TimeoutException {
		try {
			if (readWriteLock.readLock().tryLock(5, TimeUnit.SECONDS)) {
				List<String> result = new ArrayList<>();
				result.addAll(pined);
				result.addAll(unpined);
				return result;
			}else {
				throw new TimeoutException("The read-lock of the PinableList is locked.");
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public List<String> getPined() throws InterruptedException, TimeoutException {
		try {
			if (readWriteLock.readLock().tryLock(5, TimeUnit.SECONDS)) {
				return new ArrayList<>(pined);
			}else {
				throw new TimeoutException("The read-lock of the PinableList is locked.");
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public List<String> getUnpined() throws InterruptedException, TimeoutException {
		try {
			if (readWriteLock.readLock().tryLock(5, TimeUnit.SECONDS)) {
				return new ArrayList<>(unpined);
			}else {
				throw new TimeoutException("The read-lock of the PinableList is locked.");
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public void pinToFirst(String element) {
		try {
			readWriteLock.writeLock().lock();
			unpined.remove(element);
			pined.remove(element);
			pined.add(0, element);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public void pinToLast(String element) {
		try {
			readWriteLock.writeLock().lock();
			unpined.remove(element);
			pined.remove(element);
			pined.add(element);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public void unpinToFirst(String element) {
		try {
			readWriteLock.writeLock().lock();
			pined.remove(element);
			unpined.remove(element);
			unpined.add(0, element);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}
	public void unpinToLast(String element) {
		try {
			readWriteLock.writeLock().lock();
			pined.remove(element);
			unpined.remove(element);
			unpined.add(element);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}
	
	public void remove(String element) {
		try {
			readWriteLock.writeLock().lock();
			pined.remove(element);
			unpined.remove(element);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public boolean isPined(String element) throws InterruptedException, TimeoutException {
		try {
			if (readWriteLock.readLock().tryLock(5, TimeUnit.SECONDS)) {
				return pined.contains(element);
			}else {
				throw new TimeoutException("The read-lock of the PinableList is locked.");
			}
		} finally {
			readWriteLock.readLock().unlock();
		}

	}

	public boolean isUnpined(String element) throws InterruptedException, TimeoutException {
		try {
			if (readWriteLock.readLock().tryLock(5, TimeUnit.SECONDS)) {
				return unpined.contains(element);
			}else {
				throw new TimeoutException("The read-lock of the PinableList is locked.");
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public boolean contains(String element) throws InterruptedException, TimeoutException {
		try {
			if (readWriteLock.readLock().tryLock(5, TimeUnit.SECONDS)) {
				return isPined(element) || isUnpined(element);
			}else {
				throw new TimeoutException("The read-lock of the PinableList is locked.");
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	
}
