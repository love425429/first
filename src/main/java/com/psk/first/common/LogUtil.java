package com.psk.first.common;

import org.apache.commons.logging.Log;

import com.psk.first.utils.JacksonUtils;

public class LogUtil
{
	public static void INFO(Log log, String model, String action, String step, String level, Object... data)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[").append(model).append("]-[").append(action).append("]-[").append(step).append("]-[")
				.append(level).append("]");
		if (data != null)
		{
			sb.append("-[");
			for (int i = 0; i < data.length; i += 2)
			{
				if (data[i + 1] instanceof String)
				{
					sb.append(data[i]).append(":");
				}
				else
				{
					sb.append(JacksonUtils.obj2json(data[i])).append(":");
				}
				if (data[i + 1] instanceof String)
				{
					sb.append(data[i + 1]).append(";");
				}
				else
				{
					sb.append(JacksonUtils.obj2json(data[i + 1])).append(";");
				}
			}
			sb.append("]");
		}
		log.info(sb.toString());
	}

	public static void ERROR(Log log, Exception e, String model, String action, String step, Object... data)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[").append(model).append("]-[").append(action).append("]-[").append(step).append("]");
		if (data != null)
		{
			sb.append("-[");
			for (int i = 0; i < data.length; i += 2)
			{
				if (data[i + 1] instanceof String)
				{
					sb.append(data[i]).append(":");
				}
				else
				{
					sb.append(JacksonUtils.obj2json(data[i])).append(":");
				}
				if (data[i + 1] instanceof String)
				{
					sb.append(data[i + 1]).append(";");
				}
				else
				{
					sb.append(JacksonUtils.obj2json(data[i + 1])).append(";");
				}
			}
			sb.append("]");
		}
		log.error(sb.toString(), e);
	}
}
