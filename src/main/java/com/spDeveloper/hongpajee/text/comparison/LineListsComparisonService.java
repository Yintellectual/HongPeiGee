package com.spDeveloper.hongpajee.text.comparison;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.spDeveloper.hongpajee.text.entity.Line;
import com.spDeveloper.hongpajee.text.entity.LineStatus;
@Service
public class LineListsComparisonService {

	
	private void increaseAllValuesBy1(Map<Integer, Integer> map) {
		map.keySet().forEach(key->{
			map.put(key, map.get(key)+1);
		});
	}
	public List<Line> compareTwoLineLists(List<Line> origin, List<Line> update){
		List<Line> result = new LinkedList<>();
		Map<Integer, Integer> index_of_update_line_in_result = new HashMap<>();
		
		int origin_start_index = 0;
		for(int i=0;i<update.size();i++) {
			boolean isUnchanged = false;
			for(int j=origin_start_index;j<origin.size();j++) {
				if(origin.get(j).contentEquals(update.get(i))) {
					result.add(update.get(i));
					isUnchanged = true;
					origin_start_index = j+1;
					break;
				}
				
			}
			if(!isUnchanged) {
				Line addedLine = update.get(i);
				addedLine.setStatus(LineStatus.added);
				result.add(addedLine);
			}
			index_of_update_line_in_result.put(new Integer(i), result.size()-1);
		}
		
		int update_start_index = 0;
		int index_of_next_origin_line_in_result = 0;
		for(int j=0;j<origin.size();j++) {
			boolean isUnchanged = false;
			for(int i=update_start_index;i<update.size();i++) {
				if(update.get(i).contentEquals(origin.get(j))) {
					update_start_index = i+1;
					isUnchanged = true;
					index_of_next_origin_line_in_result = index_of_update_line_in_result.get(new Integer(i))+1;
					break;
				}
			}
			if(!isUnchanged) {
				Line deletedLine = origin.get(j);
				deletedLine.setStatus(LineStatus.deleted);
				result.add(index_of_next_origin_line_in_result, deletedLine);
				index_of_next_origin_line_in_result++;
				increaseAllValuesBy1(index_of_update_line_in_result);
			}
		}
		
		return result;
	}
	
}
