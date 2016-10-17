package edu.dasizeman.jftp;

import java.util.HashMap;
import java.util.Map;

public enum FTPInterfaceCommand {
	CONNECT_CMD("connect", " <hostname>:<port> : connect to the specified server.  Default port is 21.\n"),
	LOGIN_CMD("login", " -u <username> -p <password> : Log in to the server.\n"),
	CD_CMD("cd", " <path> : Change directory.\n"),
	CDUP_CMD("cdup", " : go up a directory.\n"),
	QUIT_CMD("quit", " : exit JFTP.\n"),
	PASV_CMD("passive", " [-e]: enter PASV mode with selected data port.  Use the -e flag for EPSV\n"),
	//ACTV_CMD("active", " -p <port> : enter active mode with selected data port.\n"), // Not supporting ACTIVE transfer modes due to lack of time
	GET_CMD("get", " <filename> : download the selected file.\n"),
	PWD_CMD("pwd", " : print the current server directory.\n"),
	LS_CMD("ls", " [directory] : list the contents of the server directory.\n"),
	SERVERHELP_CMD("serverhelp", " [command] : show the server's help message (for the given command).\n"),
	HELP_CMD("help", " : show this message.\n");
	
	
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
	
	public static String GetHelpString() {
		String result = "";
		
		for (FTPInterfaceCommand cmd : FTPInterfaceCommand.values()) {
			result += cmd.alias + cmd.helpString;
		}
		
		return result;
		
	}
	
	
}
