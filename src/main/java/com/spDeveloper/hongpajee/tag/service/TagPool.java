package com.spDeveloper.hongpajee.tag.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.text.html.HTML.Tag;

import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.tag.entity.TagHolder;
import com.spDeveloper.hongpajee.util.set.NoneOperationalSet;
import com.spDeveloper.hongpajee.util.set.OperationalSet;

public class TagPool<T extends TagHolder> {

	Map<String, Set<T>> tags = new ConcurrentHashMap<>();

	public void addTag(String tag) {
		if (tag == null) {
			return;
		} else {
			tags.putIfAbsent(tag, new CopyOnWriteArraySet<>());
		}
	}

	public void dropTag(String tag) {
		if (tag == null) {
			return;
		} else {
			tags.remove(tag);
		}
	}

	private Set<T> getStorageByTag(String tag) {
		return tags.get(tag);
	}

	public void removeElement(T element) {
		element.getTags().forEach(tag -> removeElement(tag, element));
	}

	public void removeElement(String tag, T element) {
		if (getStorageByTag(tag) == null) {
			return;
		} else {
			getStorageByTag(tag).remove(element);
			if (getStorageByTag(tag).isEmpty()) {
				dropTag(tag);
			}
		}
	}

	public void addElement(T article) {
		removeElement(article);
		article.getTags().forEach(tag -> addElement(tag, article));
	}

	public void addElement(String tag, T article) {
		addTag(tag);
		getStorageByTag(tag).add(article);
	}

	public HashSet<T> getByTag(String tag) {
		HashSet<T> result = new HashSet<>();

		Set<T> elements = getStorageByTag(tag);
		if (elements == null || elements.size() == 0) {

		} else {
			result.addAll(getStorageByTag(tag));
		}
		return result;
	}

	public List<String> getAllTags() {
		List<String> result = new ArrayList<>(tags.keySet());
		Collections.sort(result);
		return result;
	}

	public void fillIn(OperationalSet<T> operationalSet) {
		if (operationalSet instanceof NoneOperationalSet<?>) {
			NoneOperationalSet<T> set = (NoneOperationalSet<T>) operationalSet;
			set.setSet(getByTag(set.getName()));
		} else {
			fillIn(operationalSet.getOperant1());
			fillIn(operationalSet.getOperant2());
		}
	}
}
