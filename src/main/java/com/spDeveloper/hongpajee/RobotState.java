package com.spDeveloper.hongpajee;

import org.springframework.data.redis.connection.ReturnType;

public enum RobotState{
	On{
		
	}, Off{
		@Override
		public RobotState cook() {
			System.out.println("Warning: Robot is Off");
			return RobotState.Off;
		}
		@Override
		public RobotState off() {
			System.out.println("Warning: Robot is Off");
			return RobotState.Off;
		}	
	}, Cook{
		@Override
		public RobotState off() {
			System.out.println("Warning: Robot is cooking");
			return RobotState.Cook;
		}	
	};	
	
	public RobotState walk() {
		System.out.println("walks");
		return RobotState.On;
	}
	public RobotState cook() {
		System.out.println("cooks");
		return RobotState.Cook;
	}
	public RobotState off() {
		System.out.println("off");
		return RobotState.Off;
	}
}