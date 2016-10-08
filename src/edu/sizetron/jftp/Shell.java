package edu.sizetron.jftp;

import java.util.Scanner;

public abstract class Shell {
	
	public abstract void doCommand(String commandStr) throws Exception;
	
	protected abstract String welcomeMessage();
	
	protected abstract String helpMessage();
	
	public void run() {
		Scanner inputScanner = new Scanner(System.in);
		String inputString = "";

		System.out.println(welcomeMessage());
		System.out.println("Type 'help' for commands.");
		while (inputString.toLowerCase() != "exit") {
			System.out.print(">");
			inputString = inputScanner.nextLine(); 
			
			try {
				doCommand(inputString);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
		inputScanner.close();
		System.out.println("...bye :(");
	}
	
}
