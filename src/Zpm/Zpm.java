package Zpm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Scanner;
 

public class Zpm {
	
	public static void main(String args[]) {
		File program;
		try {
			program = getFile(args);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		execute(program);
	}
	
	private static void execute(File program) {
		Hashtable<String, Object> memory = new Hashtable<>();
		int lineCounter = 0;
		try {
			String currentLine;
			Scanner reader = new Scanner(program);
			while(reader.hasNextLine()) {
				lineCounter++;
				currentLine = reader.nextLine();
				executeLine(currentLine, memory);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (Exception e) {
			System.out.println("RUNTIME ERROR: line "+lineCounter);
			return;
		}
	}

	private static void executeLine(String currentLine, Hashtable<String, Object> memory) throws Exception {
		currentLine = currentLine.trim();
		String lineType = getLineType(currentLine);
		switch(lineType) {
			case "print":
				executeStatement(currentLine, memory, lineType);
				break;
			case "for":
				executeForLoop(currentLine, memory);
				break;
			case "assignment":
				executeStatement(currentLine, memory, lineType);
				break;
			default:
				break;
		}
	}

	private static void executeForLoop(String currentLine, Hashtable<String, Object> memory) throws Exception {
		int forCounter = Integer.parseInt(currentLine.split(" ")[1]);
		String[] statements = currentLine.replaceAll("FOR [0-9]* ", "")
										 .replace(" ENDFOR", "")
										 .split(" ;");
		for(int i = 0; i < statements.length; i++) {
			statements[i] = statements[i].strip()+" ;";
		}
		for(int i = 0; i < forCounter; i++) {
			for(int j = 0; j < statements.length; j++) {
				executeStatement(statements[j], memory, getLineType(statements[j]));
			}
		}
	}

	private static void executeStatement(String statement, Hashtable<String, Object> memory, String instruction) throws Exception {
		switch(instruction) {
			case "assignment":
				memory.put(getVariable(statement), doOperations(statement, memory));
				break;
			case "print":
				printVariable(statement.split(" ")[1], memory);
				break;
		}
	}
	
	private static void printVariable(String name, Hashtable<String, Object> memory) throws Exception {
		String out;
		Object var = getValue(name, memory);
		if(var.getClass().getName().equals("java.lang.String")) {
			out = (String)var;
		} else {
			out = ""+(int)var;
		}
		System.out.println(name+"="+out);
	}

	private static Object doOperations(String statement, Hashtable<String, Object> memory) throws Exception {
		String[] arr = statement.split(" ");
		switch(arr[1]) {
			case "=":
				return getValue(arr[2], memory);
			case "+=":
				return executeOperation(arr[0], arr[2], "+", memory);
			case "-=":
				return executeOperation(arr[0], arr[2], "-", memory);
			case "*=":
				return executeOperation(arr[0], arr[2], "*", memory);
			default:
				throw new Exception("Invalid assignment operation");
		}
	}

	private static String getVariable(String statement) {
		return statement.substring(0, 1);
	}

	private static Object executeOperation(String firstVal, String secondVal, String operation, Hashtable<String, Object> memory) throws Exception {
		Object first = getValue(firstVal, memory);
		Object second = getValue(secondVal, memory);
		
		if(!first.getClass().equals(second.getClass())) {
			throw new Exception("Type mismatch");
		}
		
		switch(operation) {
			case "+":
				if(first.getClass().getName().equals("java.lang.String")) {
					return ((String)first) + ((String)second);
				}
				else {
					return ((int)first) + ((int)second);
				}
			case "-":
				return ((int)first) - ((int)second);
			case "*":
				return ((int)first) * ((int)second);
			default:
				throw new Exception("Unexpected operation");
		}
	}
	
	private static Object getValue(String value, Hashtable<String, Object> memory) throws Exception {
		if(value.matches("[A-Z]")) {
			return memory.get(value);
		} else if(value.matches("-?[0-9]*\\.?[0-9]*")) {
			return Integer.parseInt(value);
		} else if(value.startsWith("\"") && value.endsWith("\"")) {
			return value.substring(1, value.length() - 1);
		} else {
			throw new Exception("Value "+value+" not able to be parsed.");
		}
	}
	
	private static String getLineType(String currentLine) {
		if(currentLine.startsWith("PRINT ") && currentLine.endsWith(" ;")) {
			return "print";
		}
		else if(currentLine.startsWith("FOR ") && currentLine.endsWith(" ENDFOR")) {
			return "for";
		}
		else if(currentLine.matches("[A-Z] .*") && currentLine.endsWith(" ;")) {
			return "assignment";
		}
		else {
			return "error";
		}
	}

	private static File getFile(String[] args) throws Exception {
		/// Argument check
		if(args.length < 1) {
			throw new Exception("Too few arguments; no file provided.");
		}
		String filename = args[0];
		File f = new File(filename);
		return fileChecks(f);
	}

	private static File fileChecks(File file) throws Exception {
		// Extension Check
		if(!file.getName().endsWith(".zpm")) {
			throw new Exception("File is not a Zpm file.");
		}
		// Checks if the file exists and is not a directory
		if(!file.exists()) {
			throw new FileNotFoundException("Could not find file "+file.getName());
		} else if(file.isDirectory()) {
			throw new Exception("Attempting to read from a directory.");
		} else if(!file.canRead()) {
			throw new Exception("File "+file.getAbsolutePath()+" is unreadable.");
		}
		return file;
	}
}
