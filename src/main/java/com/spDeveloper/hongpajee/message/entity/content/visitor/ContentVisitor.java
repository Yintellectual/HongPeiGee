package com.spDeveloper.hongpajee.message.entity.content.visitor;

import com.spDeveloper.hongpajee.message.entity.content.HTMLContent;
import com.spDeveloper.hongpajee.message.entity.content.TextContent;

public interface ContentVisitor {
	void visit(HTMLContent htmlContent);
	void visit(TextContent textContent);
}
