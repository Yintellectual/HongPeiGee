package com.spDeveloper.hongpajee.message.entity.content.visitor;

import java.util.List;

import com.spDeveloper.hongpajee.message.entity.content.HTMLContent;
import com.spDeveloper.hongpajee.message.entity.content.TextContent;
import com.spDeveloper.hongpajee.text.entity.Line;

import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;

import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppendContentVisitor implements ContentVisitor {

	private List<Line> text;
	
	@Override
	public void visit(HTMLContent htmlContent) {
		htmlContent.getLines().addAll(text);
	}

	@Override
	public void visit(TextContent textContent) {
		// TODO Auto-generated method stub
		textContent.getLines().addAll(text);
	}
}
