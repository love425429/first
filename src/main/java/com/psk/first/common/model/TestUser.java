package com.psk.first.common.model;

import java.io.Serializable;

public class TestUser implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3781584086817143890L;
	private long userId;
	private String name;
	private int age;

	public long getUserId()
	{
		return userId;
	}

	public void setUserId(long userId)
	{
		this.userId = userId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getAge()
	{
		return age;
	}

	public void setAge(int age)
	{
		this.age = age;
	}
}
