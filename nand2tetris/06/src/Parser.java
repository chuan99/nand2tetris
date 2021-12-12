import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

/**
 * @Author HuangChuan
 * @Create in 2021/12/03 17:31
 */
public class Parser {
	//the file will be parsed
	private File file;
	private Scanner scanner;
	private String currentCommand;
	//count the address of the instructions
	private int address;

	public int getAddress() {
		return address;
	}

	public String getCurrentCommand() {
		return currentCommand;
	}

	/*
	 * @Author HuangChuan
	 * @Description //the constructor
	 * @Date 18:38 2021/12/3
	 * @Param [file, fis]
	 * @return
	 **/
	public Parser(File file, FileInputStream fis) {
		this.file = file;
		this.scanner = new Scanner(fis);
	}

	/*
	 * @Author HuangChuan
	 * @Description //if the file has more commands?
	 * @Date 18:25 2021/12/3
	 * @Param []
	 * @return boolean
	 **/
	public boolean hasMoreCommands() {
		if (this.scanner.hasNextLine()) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description //get a new line
	 * @Date 22:26 2021/12/3
	 * @Param []
	 * @return void
	 **/
	public void advance() {
		if (hasMoreCommands()) {
			//get valid command
			do {
				this.currentCommand = this.scanner.nextLine();
			} while (!getCommand());
			//when we get a A_Command or C_Command, let the address++
			if (this.getCommandType().equals(CommandType.A_Command) ||
					this.getCommandType().equals(CommandType.C_Command)) {
				this.address++;
			}
		} else {
			this.scanner.close();
		}
	}


	/*
	 * @Author HuangChuan
	 * @Description //When we get a line, in fact it's not a command, it contains comment line,
	 * space line, space, comment. So we process those chars in this method.
	 * @Date 22:39 2021/12/3
	 * @Param []
	 * @return boolean
	 **/
	private boolean getCommand() {
		String line = this.currentCommand;
		//remove the null line
		if (line.equals("")) {
			return false;
		}
		//remove the comment line
		String head1 = line.substring(0, 2);//get the first two chars
		String head2 = line.trim().substring(0, 2);//get the first two chars
		if (head1.equals("//") || head2.equals("//")) {
			return false;
		}
		//remove the comment part of current line
		if (line.contains("//")) {
			String temp = line.substring(0, line.indexOf("//"));
			this.currentCommand = temp.trim();
			return true;
		}
		//remove the spaces
		this.currentCommand = line.trim();
		return true;
	}

	/*
	 * @Author HuangChuan
	 * @Description //distinguish the command type by the first char
	 * @Date 23:00 2021/12/3
	 * @Param []
	 * @return CommandType
	 **/
	public CommandType getCommandType() {
		if (this.currentCommand.substring(0, 1).equals("@")) {
			return CommandType.A_Command;
		} else if (this.currentCommand.substring(0, 1).equals("(")) {
			return CommandType.L_Command;
		} else if (this.currentCommand.contains(";") || this.currentCommand.contains("=")) {
			return CommandType.C_Command;
		} else {
			return CommandType.UNKNOWN;
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description //Judge whether the symbol is integer symbol
	 * @Date 11:24 2021/12/4
	 * @Param [symbol]
	 * @return boolean
	 **/
	public boolean isDigit(String symbol) {
		for (int i = 0; i < symbol.length(); i++) {
			if (!Character.isDigit(symbol.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/*
	 * @Author HuangChuan
	 * @Description //Padding the string to get the 15 bit address
	 * @Date 11:41 2021/12/4
	 * @Param [str]
	 * @return java.lang.String
	 **/
	public String paddingZero(String str) {
		while (str.length() <= 15) {
			str = "0" + str;
		}
		return str;
	}

	/*
	 * @Author HuangChuan
	 * @Description //basically, whatever the command is a-command or l-command, if the symbol is
	 * represented by address, we return the binary form of the address, if the symbol is
	 * represented by string, we return the string.
	 * When we first scan the file, if we get the binary string of the address we don't do
	 * anything, if we get the string symbol we add the string and it's binary address to the
	 * symbol table.
	 * When we second scan the file, if we get the binary string of the address we translate it
	 * , if we get the string symbol we get it's address from hash table.
	 * @Date 12:46 2021/12/4
	 * @Param []
	 * @return java.lang.String
	 **/
	public String symbol() {
		if (getCommandType().equals(CommandType.A_Command)) {
			String command = this.currentCommand;
			String subStr = command.substring(1);
			//if the A-Command is represented by address, do the following thing
			if (isDigit(subStr)) {
				long address = Long.parseLong(subStr);
				String addressStr = Long.toBinaryString(address);
				return paddingZero(addressStr);
			} else {
				return subStr;
			}
		}
		if (getCommandType().equals(CommandType.L_Command)) {
			String command = this.currentCommand;
			String subStr = command.substring(1, command.length() - 1);
			//if the L-Command is represented by address, do the following thing
			if (isDigit(subStr)) {
				Long address = Long.parseLong(subStr);
				String addressStr = Long.toBinaryString(address);
				return paddingZero(addressStr);
			} else {
				return subStr;
			}
		}
		return null;
	}

	/*
	 * @Author HuangChuan
	 * @Description //get the dest of the C-Command,
	 * if the command has not the dest domain, return ""
	 * if the command is not the C-Command, return null
	 * @Date 13:30 2021/12/4
	 * @Param []
	 * @return java.lang.String
	 **/
	public String dest() {
		if (getCommandType().equals(CommandType.C_Command)) {
			String command = this.currentCommand;
			if (command.contains("=")) {
				return command.substring(0, command.indexOf("="));
			} else {
				return "";
			}
		} else {
			return null;
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description //get the comp domain of the C-Command
	 * if the command has not the comp domain, return ""
	 * if the command is not the C-Command, return null
	 * @Date 13:40 2021/12/4
	 * @Param []
	 * @return java.lang.String
	 **/
	public String comp() {
		if (getCommandType().equals(CommandType.C_Command)) {
			String command = this.currentCommand;
			if (command.contains("=") && command.contains(";")) {
				return command.substring(command.indexOf("=") + 1, command.indexOf(";"));
			} else if (command.contains("=")) {
				return command.substring(command.indexOf("=") + 1);
			} else if (command.contains(";")) {
				return command.substring(0, command.indexOf(";"));
			} else {
				return "";
			}
		} else {
			return null;
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description //get the jump domain of the C-Command
	 * if the command has not jump domain, return ""
	 * if the command is not C-Command, return null
	 * @Date 13:44 2021/12/4
	 * @Param []
	 * @return java.lang.String
	 **/
	public String jump() {
		if (getCommandType().equals(CommandType.C_Command)) {
			String command = this.currentCommand;
			if (command.contains(";")) {
				return command.substring(command.indexOf(";") + 1);
			} else {
				return "";
			}
		} else {
			return null;
		}
	}
}
