package com.psk.first.mvc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.psk.first.common.model.TestUser;

@Service("helloWordService")
public class HelloWordServiceImpl implements HelloWordService
{
	@Autowired
	private HelloWordDao helloWordDao;

	@Override
	public List<TestUser> getAllUserInfo()
	{
		List<TestUser> ret = helloWordDao.getAllUserInfo();
		if (ret == null)
		{
			ret = new ArrayList<TestUser>();
		}
		return ret;
	}

}
