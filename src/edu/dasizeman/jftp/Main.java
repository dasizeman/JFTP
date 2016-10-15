package edu.dasizeman.jftp;

public class Main {

	public static void main(String[] args) {
		FTPShell shell = new FTPShell("ftp.log");
		shell.run();
	}

}
