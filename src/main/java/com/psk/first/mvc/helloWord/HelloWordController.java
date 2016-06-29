package com.psk.first.mvc.helloWord;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.psk.first.common.BaseController;
import com.psk.first.common.LogUtil;
import com.psk.first.utils.ConfigUtil;

@Controller
public class HelloWordController extends BaseController
{

	private Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private HelloWordService helloWordService;

	@RequestMapping(value =
	{ "hello.html" })
	@ResponseBody
	public Map<String, Object> hello(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
	{
		Map<String, Object> result = new HashMap<String, Object>();
		String user = request.getParameter("user");
		result.put("user", user);
		result.put("say", "hello world");
		result.put("testConfig", ConfigUtil.getStringValueFromConfig("redis.hosts", ""));
		LogUtil.INFO(log, "HelloWordController", "hello", "param", "info", "user", user);
		try
		{
			String i = "-x1";
			Integer in = Integer.valueOf(i);
			in.equals(1);
		}
		catch (Exception e)
		{
			//test log context
			LogUtil.ERROR(log, e, "HelloWordController", "hello", "check", "user", user);
		}
		return result;
	}

	@RequestMapping(value =
	{ "hellojsp.html" })
	public String hellojsp(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
	{
		String user = request.getParameter("user");
		modelMap.put("user", user);
		modelMap.put("say", "hello world");
		return "index";
	}

	@RequestMapping(value =
	{ "mybatis.html" })
	@ResponseBody
	public Map<String, Object> mybatis(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
	{
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("users", helloWordService.getAllUserInfo());
		return result;
	}
}
