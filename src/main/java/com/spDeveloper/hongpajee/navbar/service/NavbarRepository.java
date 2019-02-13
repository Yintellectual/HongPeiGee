package com.spDeveloper.hongpajee.navbar.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.spDeveloper.hongpajee.exception.OutdatedEditeeException;
import com.spDeveloper.hongpajee.navbar.entity.NavItem;
import com.spDeveloper.hongpajee.redis.RedisJsonDAO;

/**
 * This is a two dimensional data structure. The order in both dimensions
 * matters. This data structure should allow massive and fast read. This data
 * structure should be thread safe to write.
 */

@Service
public class NavbarRepository {

	// before any update of data, always check with the versionId. If the versionId
	// does not match, then refuse the update.
	private Long versionId = 0l;
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	private List<String> navItemIds = new CopyOnWriteArrayList<>();
	private Map<String, NavItem> navItems = new ConcurrentHashMap<>();
	private List<NavItem> readOnlyNavItems = new ArrayList<>();

	@Autowired
	RedisJsonDAO redisJsonDAO;
	@Autowired
	Gson gson;

	public void save() {
		readWriteLock.writeLock().lock();
		try {
			if (navItemIds != null && !navItemIds.isEmpty()) {
				redisJsonDAO.persist(navItemIds, "navItemIds");
			}
			if (navItems != null && !navItems.isEmpty()) {
				redisJsonDAO.persist(navItems, "navItems");
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	@PostConstruct
	public void init() {
		readWriteLock.writeLock().lock();
		try {
			navItemIds.addAll(redisJsonDAO.recoverList("navItemIds", String.class));
			navItems.putAll(redisJsonDAO.recoverMap("navItems", NavItem.class));
			burn();
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public Long getVersionId() {
		return versionId;
	}

	public NavItem getNavItem(String parentId, String id) {
		readWriteLock.readLock().lock();
		try {
			if (parentId == null) {
				// a navbar item
				if (isNavItem(id)) {
					return navItems.get(id);
				} else {
					return null;
				}
			} else {
				// a dropdown
				if (isNavItem(parentId)) {
					int index = dropdownIndex(parentId, id);
					if (index == -1) {
						return null;
					} else {
						return navItems.get(parentId).getDropdown().get(index);
					}
				} else {
					return null;
				}
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public String getElderBrotherId(String parentId, String id) {
		readWriteLock.readLock().lock();
		try {
			if (parentId == null || parentId.isEmpty()) {
				// navbar item
				if (isNavItem(id)) {
					int index = navItemIds.indexOf(id);
					if (index <= 0) {
						return null;
					} else {
						return navItemIds.get(index - 1);
					}
				} else {
					return null;
				}
			} else {
				// dropdown
				int index = dropdownIndex(parentId, id);
				if (index <= -1) {
					return null;
				} else if (index == 0) {
					return null;
				} else {
					return navItems.get(parentId).getDropdown().get(index - 1).getId();
				}
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public boolean addOrUpdate(NavItem navItem, String parentId, String elderBrotherId, Long expectedVersionId)
			throws OutdatedEditeeException {
		readWriteLock.writeLock().lock();
		try {
			if (!versionId.equals(expectedVersionId)) {
				throw new OutdatedEditeeException("Navbar update failed");
			}

			if (navItem == null || navItem.getId() == null || navItem.getId().isEmpty()) {
				return false;
			}
			if (parentId == null || parentId.isEmpty()) {
				// is navbar item
				if (isNavItem(navItem.getId())) {
					NavItem oldVersion = navItems.get(navItem.getId());
					navItem.setDropdown(oldVersion.getDropdown());
					boolean removal = remove(null, oldVersion.getId(), expectedVersionId);
					expectedVersionId = versionId;
				} else {

				}

				if (elderBrotherId == null || elderBrotherId.isEmpty()) {
					int index = 0;
					navItemIds.add(index, navItem.getId());
					navItems.put(navItem.getId(), navItem);
					versionId++;
					return true;
				} else if (!isNavItem(elderBrotherId)) {
					return false;
				} else {
					int index = navItemIds.indexOf(elderBrotherId) + 1;
					navItemIds.add(index, navItem.getId());
					navItems.put(navItem.getId(), navItem);
					versionId++;
					return true;
				}
			} else {
				// is dropdown item
				if (!isNavItem(parentId)) {
					return false;
				} else {
					NavItem parent = navItems.get(parentId);
					List<NavItem> dropdowns = parent.getDropdown();
					int navItemIndex = dropdownIndex(parentId, navItem.getId());
					if (navItemIndex != -1) {
						dropdowns.remove(navItemIndex);
					} else {

					}

					if (elderBrotherId == null || elderBrotherId.isEmpty()) {
						int index = 0;
						dropdowns.add(index, navItem);
						versionId++;
						return true;
					} else if (dropdownIndex(parentId, elderBrotherId) == -1) {
						return false;
					} else {
						int elderBrotherIndex = dropdownIndex(parentId, elderBrotherId);
						dropdowns.add(elderBrotherIndex + 1, navItem);
						versionId++;
						return true;
					}
				}
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public boolean isNavItem(String id) {
		readWriteLock.readLock().lock();
		try {
			if (id == null || id.isEmpty()) {
				return false;
			}
			return navItems.keySet().contains(id);
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public int dropdownIndex(String parentId, String id) {
		readWriteLock.readLock().lock();
		try {
			if (parentId == null || parentId.isEmpty()) {
				return -1;
			} else if (id == null || id.isEmpty()) {
				return -1;
			} else {
				if (!isNavItem(parentId)) {
					return -1;
				}
				NavItem parent = navItems.get(parentId);
				if (parent == null || !parent.getHasDropdown()) {
					return -1;
				} else {
					for (int i = 0; i < parent.getDropdown().size(); i++)
						if (id.equals(parent.getDropdown().get(i).getId())) {
							return i;
						}
				}
				return -1;
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public boolean remove(String parentId, String id, Long expectedVersionId)
			throws OutdatedEditeeException {
		readWriteLock.writeLock().lock();
		try {
			if (!versionId.equals(expectedVersionId)) {
				throw new OutdatedEditeeException("Navbar removal failed. ");
			}

			if (parentId == null) {
				if (isNavItem(id)) {
					navItemIds.remove(id);
					navItems.remove(id);
					versionId++;
					return true;
				} else {
					return false;
				}
			} else {
				int dropdownIndex = dropdownIndex(parentId, id);
				if (dropdownIndex == -1) {
					return false;
				} else {
					NavItem parent = navItems.get(parentId);
					List<NavItem> dropdown = parent.getDropdown();
					dropdown.remove(dropdownIndex);
					versionId++;
					return true;
				}
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public <T> void move(List<T> list, int oldIndex, int vector, Long expectedVersionId)
			throws OutdatedEditeeException {
		readWriteLock.writeLock().lock();
		try {
			if (!versionId.equals(expectedVersionId)) {
				throw new OutdatedEditeeException("Navbar rearrangement failed. ");
			}
			int newIndex = oldIndex + vector;
			if (newIndex < 0 || newIndex >= list.size()) {
				return;
			} else {
				T item = list.get(oldIndex);
				T neighbour = list.get(newIndex);
				list.set(newIndex, item);
				list.set(oldIndex, neighbour);
				versionId++;
				return;
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public void left(String id, Long expectedVersionId) throws OutdatedEditeeException {
		readWriteLock.writeLock().lock();
		try {
			if (!versionId.equals(expectedVersionId)) {
				throw new OutdatedEditeeException("Navbar left move failed. ");
			}
			if (isNavItem(id)) {
				int oldIndex = navItemIds.indexOf(id);
				move(navItemIds, oldIndex, -1, expectedVersionId);
				expectedVersionId = versionId;
				versionId++;
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public void right(String id, Long expectedVersionId) throws OutdatedEditeeException {
		readWriteLock.writeLock().lock();
		try {
			if (!versionId.equals(expectedVersionId)) {
				throw new OutdatedEditeeException("Navbar left move failed. ");
			}

			if (isNavItem(id)) {
				int oldIndex = navItemIds.indexOf(id);
				move(navItemIds, oldIndex, 1, expectedVersionId);
				expectedVersionId = versionId;
				versionId++;
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}

	}

	public void up(String parentId, String id, Long expectedVersionId) throws OutdatedEditeeException {

		readWriteLock.writeLock().lock();
		try {
			if (!versionId.equals(expectedVersionId)) {
				throw new OutdatedEditeeException("Navbar left move failed. ");
			}
			int dropdownIndex = dropdownIndex(parentId, id);
			if (dropdownIndex != -1) {
				List<NavItem> dropdownList = navItems.get(parentId).getDropdown();
				int oldIndex = dropdownIndex;
				move(dropdownList, oldIndex, -1, expectedVersionId);
				expectedVersionId = versionId;
				versionId++;
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public void down(String parentId, String id, Long expectedVersionId) throws OutdatedEditeeException {

		readWriteLock.writeLock().lock();
		try {
			if (!versionId.equals(expectedVersionId)) {
				throw new OutdatedEditeeException("Navbar left move failed. ");
			}
			int dropdownIndex = dropdownIndex(parentId, id);
			if (dropdownIndex != -1) {
				List<NavItem> dropdownList = navItems.get(parentId).getDropdown();
				int oldIndex = dropdownIndex;
				move(dropdownList, oldIndex, 1, expectedVersionId);
				expectedVersionId = versionId;
				versionId++;
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public void burn() {
		readWriteLock.writeLock().lock();
		try {
			readOnlyNavItems = toReadOnly();
			save();
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public List<NavItem> toReadOnly() {
		readWriteLock.writeLock().lock();
		try {
			List<NavItem> result = new ArrayList<>();
			for(String itemId:navItemIds) {
				NavItem item = navItems.get(itemId);
				result.add((NavItem)item.clone());
			}
			return result;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public List<NavItem> getReadOnly() {
		List<NavItem> result = new ArrayList<>();
		result.add(new NavItem("站长直播间", "/user/live", UUID.randomUUID().toString()));
		result.addAll(readOnlyNavItems);
		return result;
	}
}
