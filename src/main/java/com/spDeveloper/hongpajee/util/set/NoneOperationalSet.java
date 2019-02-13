package com.spDeveloper.hongpajee.util.set;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoneOperationalSet<T> extends OperationalSet<T> {
	
	private HashSet<T> set = new HashSet<>();
	private String name;
	@Override
	public HashSet<T> toSet(){
		return new HashSet<>(set);
	}
}
