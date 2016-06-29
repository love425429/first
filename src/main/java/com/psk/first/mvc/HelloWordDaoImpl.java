package com.psk.first.mvc;

import java.util.List;

import org.springframework.stereotype.Component;

import com.psk.first.common.BaseDao;
import com.psk.first.common.model.TestUser;

@Component("helloWordDao")
public class HelloWordDaoImpl extends BaseDao implements HelloWordDao
{

	@Override
	public List<TestUser> getAllUserInfo()
	{
		return this.getSqlSession().selectList("TestUser.selectAllUser");
	}
}
