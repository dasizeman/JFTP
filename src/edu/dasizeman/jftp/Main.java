package edu.dasizeman.jftp;

public class Main {

	public static void main(String[] args) {
		String[] requiredArgs = new String[]{"-log"};
		ParseMap parsedArgs = Parser.Parse(args, requiredArgs);
		
		if (parsedArgs == null) {
			System.out.println("Must specify path to log file with -log");
			return;
		}
		
		FTPShell shell = new FTPShell(parsedArgs.get("-log"));
		shell.run();
	}

}
