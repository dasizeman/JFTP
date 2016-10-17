package edu.dasizeman.jftp;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		String[] requiredArgs = new String[]{"-log", "-host"};
		ParseMap parsedArgs = Parser.Parse(args, requiredArgs);
		
		if (parsedArgs == null) {
			System.out.println("The following arguments are required:");
			System.out.println("-log : path to a log file.");
			System.out.println("-host : host to connect to in format <host>:<port>");
			System.out.println("    If port is omitted, port 21 is assumed");
			return;
		}
		
		// Prompt for username and pass
		Scanner inScanner = new Scanner(System.in);
		System.out.print("username: ");
		String user = inScanner.nextLine();
		System.out.print("password: ");
		String pass = inScanner.nextLine();
		
		// Build the connect command
		String connectCmd = "connect " + parsedArgs.get("-host");
		
		// Build login command
		String loginCmd = "login -u " + user + " -p " + pass;
		
		FTPShell shell = new FTPShell(parsedArgs.get("-log"));
		
		// Execute the commands
		try {
			shell.doCommand(connectCmd);
			shell.doCommand(loginCmd);
		} catch (Throwable e) {
			System.out.println(e.getClass().getName() + e.getMessage());
			inScanner.close();
			return;
		}
		
		// Start the shell
		shell.run(inScanner);
	}

}
