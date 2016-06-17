package com.psk.first.utils;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.psk.first.common.LogUtil;

public class ConfigUtil {
	private static final Properties configProp = new Properties();
	private  static final String config = "config.properties";
	private static Log log = LogFactory.getLog("__ConfigUtil__");
	
	static{
		load(config,configProp);
	}
	
	public static void load(final String config,final Properties p){
		try{
			InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
			p.load(input);
		}catch(Exception e){
			LogUtil.ERROR(log, e, "ConfigUtil", "load", "initConfig", null);
		}
	}
	
	public static String getStringValueFromConfig(String key,String defaultValue){
		try{
			String value = String.valueOf(configProp.getProperty(key, defaultValue));
			return value;
		}catch(Exception e){
			LogUtil.ERROR(log, e, "ConfigUtil", "getStringValueFromConfig", "getValue", null);
		}
		return defaultValue;
	}
	
	public static Integer getIntValueFromConfig(String key,Integer defaultValue){
		try{
			Integer value = Integer.valueOf((String) configProp.getProperty(key, defaultValue==null?null:""+defaultValue));
			return value;
		}catch(Exception e){
			LogUtil.ERROR(log, e, "ConfigUtil", "getIntValueFromConfig", "getValue", null);
		}
		return defaultValue;
	}
}
