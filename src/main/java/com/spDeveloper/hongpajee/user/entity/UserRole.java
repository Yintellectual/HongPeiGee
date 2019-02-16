package com.spDeveloper.hongpajee.user.entity;

public enum UserRole {
	ROLE_ADMIN, ROLE_USER, ROLE_OWNER;
	
	private String[] authorities;
	
    static {
        ROLE_USER.authorities = new String[]{"ROLE_USER"};
        ROLE_ADMIN.authorities = new String[] {"ROLE_USER", "ROLE_ADMIN"};
        ROLE_OWNER.authorities = new String[] {"ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER"};
    }

    public String[] getAuthorities(){
        return authorities;
    }
}
