package br.com.ipnetsolucoes.util;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import br.com.ipnetsolucoes.beans.ActionStatus;

public class GlobalLog {
	private Logger logGlobal;

	public GlobalLog() {
	}

	public GlobalLog(Logger logGlobal) {
		this.logGlobal = logGlobal;
	}

	public Logger getLogGlobal() {
		return logGlobal;
	}

	public void setLogGlobal(Logger logGlobal) {
		this.logGlobal = logGlobal;
	}


	public void insertLog(String description, ActionStatus actionStatus, Exception e){
		ArrayList<String> list = new ArrayList<String>();
		list.add("ERRO: "+logExceptions(e));
		String  textLog = "| [AÇÃO: "+((description!=null)?description:"")+ " ] | [STATUS: "+actionStatus+"] | [ERROS: "+((list!=null)?list.toString():"")+" ]";
		this.logGlobal.fatal(textLog);
	}

	public static Logger generateLog(String logName, String logPath) {
		Logger log = Logger.getLogger(logName);
		Properties props = new Properties();
		props.setProperty("log4j.rootLogger", "TRACE, stdout");
		props.setProperty("log4j.appender.file", "org.apache.log4j.RollingFileAppender");
		props.setProperty("log4j.appender.file.maxFileSize", "100MB");
		props.setProperty("log4j.appender.file.maxBackupIndex", "100");
		props.setProperty("log4j.appender.file.File", logPath + logName + ".log");
		props.setProperty("log4j.appender.file.threshold", "debug");
		props.setProperty("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
		props.setProperty("log4j.appender.file.layout.ConversionPattern",
				"%d{dd/MM/yyyy HH:mm:ss,SSS} %5p [%-20c] %m%n");
		props.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
		props.setProperty("log4j.appender.stdout.Target", "System.out");
		props.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
		props.setProperty("log4j.appender.stdout.layout.ConversionPattern",
				"%d{dd/MM/yyyy HH:mm:ss,SSS} %5p [%-20c] %m%n");
		props.setProperty("log4j.logger." + logName, "TARCE, file");
		PropertyConfigurator.configure(props);
		return log;
	}
	
	public  String logExceptions(Exception e) {
		if (e == null)
			return "";
		String txtError = "  Causa: "  +	e.getCause()+" -- ";
		txtError += "Mensagem: "+	e.getMessage()+" -- ";
		txtError += "Mensagem Localizada: "+	e.getLocalizedMessage()+" -- ";
		
		for (StackTraceElement  element : e.getStackTrace()) {
			txtError += "Elemento Stack: "+	element+" -- ";
		}
		return txtError;
	}
	
	public static String getJarPath(@SuppressWarnings("rawtypes") Class classe) {
		String path = "";
		String decodedPath = "";
		
		try {
			path = classe.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()+"";
			decodedPath = URLDecoder.decode(path, "UTF-8");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if(decodedPath.contains("bin/")){
			decodedPath = decodedPath.replace("bin/", "");
		}else{
			int length = decodedPath.split("/").length;
			String removeTxt = decodedPath.split("/")[length-1];
			decodedPath = decodedPath.replace(removeTxt, "");
		}
		decodedPath = decodedPath.replace("file:\\", "");
		if (decodedPath.startsWith("\\")) {
			decodedPath  = decodedPath.substring(1,decodedPath.length());
		}
		
		decodedPath = decodedPath.replace("target//", "");
		System.out.println(decodedPath);
		return decodedPath;
	}

}
