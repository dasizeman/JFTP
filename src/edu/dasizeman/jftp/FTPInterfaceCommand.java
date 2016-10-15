package edu.dasizeman.jftp;

import java.util.HashMap;
import java.util.Map;

public enum FTPInterfaceCommand {
	LOGIN_CMD("login", " -u <username> -p <password> : Log in to the server.\n"),
	CD_CMD("cd", " <path> : Change directory.\n"),
	CDUP_CMD("cdup", " : go up a directory.\n"),
	QUIT_CMD("quit", " : exit JFTP.\n"),
	PASV_CMD("passive", " -p <port> : enter passive mode with selected data port.\n"),
	ACTV_CMD("active"),
	GET_CMD("get"),
	PWD_CMD("pwd"),
	LS_CMD("ls"),
	HELP_CMD("help");
	
	private final String alias, helpString;
	
	FTPInterfaceCommand(String alias, String help) {
		this.alias = alias;
		this.helpString = help;
	}
	
	private static final Map<String, FTPInterfaceCommand> commandMap;
	
	static {
		commandMap = new HashMap<String, FTPInterfaceCommand>();
		for (FTPInterfaceCommand cmd : FTPInterfaceCommand.values() ) {
			commandMap.put(cmd.alias, cmd);
		}
	}
	
	public static FTPInterfaceCommand getByAlias(String alias) {
		return commandMap.get(alias);
	}
	
	
}
