package com.spDeveloper.hongpajee.video.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApsaraVideo{
	private String vid;
	private String playAuth;
	private String cover;
	private String createTime;
	private String title;
	
}