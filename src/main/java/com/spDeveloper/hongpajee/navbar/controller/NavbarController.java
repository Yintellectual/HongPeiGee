package com.spDeveloper.hongpajee.navbar.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.google.gson.JsonObject;
import com.spDeveloper.hongpajee.exception.OutdatedEditeeException;
import com.spDeveloper.hongpajee.navbar.entity.NavItem;
import com.spDeveloper.hongpajee.navbar.service.NavbarRepository;
import com.spDeveloper.hongpajee.post.entity.Article;
import com.spDeveloper.hongpajee.tag.service.TagPool;


@Controller
public class NavbarController {

	public final String EXPECTED_VERSION_ID = "expectedVersionId";

	@Autowired
	NavbarRepository navbarRepository;
	@Autowired
	TagPool<Article> tagPool;
	
	
	@GetMapping("/admin/nav")
	public String nav(Model model, HttpServletRequest request) {
		request.getSession().setAttribute(EXPECTED_VERSION_ID, navbarRepository.getVersionId());
		model.addAttribute("navItems", navbarRepository.toReadOnly());

		return "editable_navbar";
	}

	@PostMapping("/admin/nav/burn")
	public String burn() {
		navbarRepository.burn();
		return "redirect:/";
	}

	@PostMapping("/admin/nav/left")
	public String left(@RequestParam String id, HttpServletRequest request) {
		Long expectedVersionId = (Long) request.getSession().getAttribute(EXPECTED_VERSION_ID);
		try {
			navbarRepository.left(id, expectedVersionId);
		} catch (OutdatedEditeeException e) {
			// TODO Auto-generated catch block
			return "redirect:/admin/nav?error=edition_is_outdated";
		}
		request.getSession().setAttribute(EXPECTED_VERSION_ID, navbarRepository.getVersionId());
		return "redirect:/admin/nav";
	}

	@PostMapping("/admin/nav/right")
	public String right(@RequestParam String id, HttpServletRequest request) {
		Long expectedVersionId = (Long) request.getSession().getAttribute(EXPECTED_VERSION_ID);
		try {
			navbarRepository.right(id, expectedVersionId);
		} catch (OutdatedEditeeException e) {
			// TODO Auto-generated catch block
			return "redirect:/admin/nav?error=edition_is_outdated";
		}
		request.getSession().setAttribute(EXPECTED_VERSION_ID, navbarRepository.getVersionId());
		return "redirect:/admin/nav";
	}

	@PostMapping("/admin/nav/up")
	public String up(@RequestParam String id, @RequestParam("parent_id") String parentId, HttpServletRequest request) {
		Long expectedVersionId = (Long) request.getSession().getAttribute(EXPECTED_VERSION_ID);
		try {
			navbarRepository.up(parentId, id, expectedVersionId);
		} catch (OutdatedEditeeException e) {
			// TODO Auto-generated catch block
			return "redirect:/admin/nav?error=edition_is_outdated";
		}
		request.getSession().setAttribute(EXPECTED_VERSION_ID, navbarRepository.getVersionId());
		return "redirect:/admin/nav";
	}

	@PostMapping("/admin/nav/down")
	public String down(@RequestParam String id, @RequestParam("parent_id") String parentId,
			HttpServletRequest request) {
		Long expectedVersionId = (Long) request.getSession().getAttribute(EXPECTED_VERSION_ID);
		try {
			navbarRepository.down(parentId, id, expectedVersionId);
		} catch (OutdatedEditeeException e) {
			// TODO Auto-generated catch block
			return "redirect:/admin/nav?error=edition_is_outdated";
		}
		request.getSession().setAttribute(EXPECTED_VERSION_ID, navbarRepository.getVersionId());
		return "redirect:/admin/nav";
	}

	@PostMapping("/admin/nav/remove")
	public String remove(@RequestParam String id,
			@RequestParam(value = "parent_id", required = false) String parentId, HttpServletRequest request) {
		Long expectedVersionId = (Long) request.getSession().getAttribute(EXPECTED_VERSION_ID);
		try {
			navbarRepository.remove(parentId, id, expectedVersionId);
		} catch (OutdatedEditeeException e) {
			// TODO Auto-generated catch block
			return "redirect:/admin/nav?error=edition_is_outdated";
		}
		request.getSession().setAttribute(EXPECTED_VERSION_ID, navbarRepository.getVersionId());
		return "redirect:/admin/nav";
	}

	@PostMapping("/admin/nav/add/form")
	public String add(@RequestParam String id, 
			@RequestParam(value = "parent_id", required = false) String parentId,
			Model model) {
		model.addAttribute("navItems", navbarRepository.getReadOnly());
		model.addAttribute("parentId", parentId);
		model.addAttribute("elderBrotherId", navbarRepository.getElderBrotherId(parentId, id));
		model.addAttribute("formTitle", "Add Navbar Item");
		model.addAttribute("tags", tagPool.getAllTags());
		return "nav_item_form";
	}

	@PostMapping("/admin/nav/edit/form")
	public String edit(@RequestParam String id, 
			@RequestParam(value = "parent_id", required = false) String parentId,
			Model model) {
		model.addAttribute("navItems", navbarRepository.getReadOnly());
		model.addAttribute("parentId", parentId);
		model.addAttribute("elderBrotherId", navbarRepository.getElderBrotherId(parentId, id));
		model.addAttribute("formTitle", "Edit Navbar Item");
		model.addAttribute("navItem", navbarRepository.getNavItem(parentId, id));
		model.addAttribute("tags", tagPool.getAllTags());
		return "nav_item_form";
	}

	@PostMapping("/admin/nav/update")
	public String updateNavItem(@ModelAttribute NavItem navItem,
			@RequestParam String parentId,
			@RequestParam String elderBrotherId, 
			HttpServletRequest request) {
		Long expectedVersionId = (Long) request.getSession().getAttribute(EXPECTED_VERSION_ID);
		if (navItem.getId() == null || navItem.getId().isEmpty()) {
			navItem.setId(UUID.randomUUID().toString());
		}
		try {
			navbarRepository.addOrUpdate(navItem, parentId, elderBrotherId, expectedVersionId);
		} catch (OutdatedEditeeException e) {
			// TODO Auto-generated catch block
			return "redirect:/admin/nav?error=edition_is_outdated";
		}
		request.getSession().setAttribute(EXPECTED_VERSION_ID, navbarRepository.getVersionId());
		return "redirect:/admin/nav";
	}
}
