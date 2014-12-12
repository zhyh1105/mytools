package com.amos.tool;

import java.util.Properties;

import com.amos.constants.Constants;


public class Config {
	private static Properties properties;
	static
	{
		if(properties == null)
			properties = PropertiesUtil.readPropertiesFile(Constants.CLIENT_CONFIG_FILE);	
	}
	
	
	public static String getProperty(String key)
	{
		return properties.getProperty(key);
	}
	
//	public static void main  (String args[]){
////		File file2 = new File("E:/workspace/PlaneTicketCollect/conf/conf.properties");
////		if(file2.exists()){
////			System.out.println(FileUtil.read(file2));
////		}
//		//FileUtil.writeStrings("1",Constants.FATHER_FILE_PATH+"/"+"1.txt",false);
//		
//		String s=	Config.getProperty("SITE_ID_CTRIP");
//		System.out.println(s);
//	}
}
