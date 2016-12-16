package br.com.ipnetsolucoes.main;

import java.io.InputStream;
import java.util.Properties;

public class Properties2 {
	private Properties props;
	
	public Properties2(){
		InputStream is = null;
		try{
			this.props = new Properties();
			is = this.getClass().getResourceAsStream("config.properties");
			props.load(is);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	public Properties getProps(){
		return props;
	}
}
