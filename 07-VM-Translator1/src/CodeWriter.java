import java.io.*;

/**
 * @Author HuangChuan
 * @Create in 2021/12/09 20:52
 */
public class CodeWriter {
	//the source file
	private File file;
	private File outputFile;
	BufferedWriter bufferedWriter;
	//this variable is used to distinguish the L-Command
	private int i = 0;

	public BufferedWriter getBufferedWriter() {
		return bufferedWriter;
	}

	public CodeWriter(File file) throws FileNotFoundException {
		this.file = file;
		setFileName(file.getPath());
		this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
	}

	/*
	 * @Author HuangChuan
	 * @Description //get the output file path by input file path
	 * @Date 21:35 2021/12/9
	 * @Param [fileName]
	 * @return void
	 **/
	public void setFileName(String fileName) {
		String outputFilePath = fileName.substring(0, fileName.lastIndexOf("\\")) + "\\" + fileName.substring(fileName.lastIndexOf("\\") + 1, fileName.indexOf(".")) + ".asm";
		this.outputFile = new File(outputFilePath);
	}

	/*
	 * @Author HuangChuan
	 * @Description //the assemble code of get top value of stack
	 * @Date 11:38 2021/12/11
	 * @Param []
	 * @return void
	 **/
	private void pop() throws IOException {
		this.bufferedWriter.write("//get the top value of stack\r\n"
				+ "@SP\r\n"
				+ "M=M-1\r\n"
				+ "A=M\r\n"
				+ "D=M\r\n");

	}

	/*
	 * @Author HuangChuan
	 * @Description //the assemble code of push value into stack
	 * @Date 12:42 2021/12/11
	 * @Param [value]
	 * @return void
	 **/
	private void push(String value) throws IOException {
		this.bufferedWriter.write("//push the value into stack\r\n"
				+ "@SP\r\n"
				+ "A=M\r\n"
				+ "M=" + value + "\r\n"
				+ "@SP\r\n"
				+ "M=M+1\r\n");
	}

	/*
	 * @Author HuangChuan
	 * @Description //compute the address by basic address, and get the value in the unit by address
	 * @Date 18:57 2021/12/11
	 * @Param [arg1: which segment, arg2: the offset]
	 * @return void
	 **/
	private void pushByArg(String arg1, String arg2) throws IOException {
		this.bufferedWriter.write("@" + arg1 + "\r\n"
				+ "D=M\r\n"
				+ "@" + arg2 + "\r\n"
				+ "A=A+D\r\n"
				+ "D=M\r\n");
		push("D");
	}

	/*
	 * @Author HuangChuan
	 * @Description //store the temporary value int general register
	 * @Date 13:13 2021/12/11
	 * @Param [reg]
	 * @return void
	 **/
	private void storeInReg(String reg) throws IOException {
		this.bufferedWriter.write("//store the result temporarily\r\n"
				+ "@" + reg + "\r\n"
				+ "M=D\r\n");
	}

	/*
	 * @Author HuangChuan
	 * @Description //get the two top values of stack and store them in R13 and R14
	 * @Date 13:17 2021/12/11
	 * @Param []
	 * @return void
	 **/
	private void getTwoTopElemAndStoreInReg() throws IOException {
		//get the first value
		pop();
		storeInReg("R14");
		//get the second value
		pop();
		storeInReg("R13");
	}

	/*
	 * @Author HuangChuan
	 * @Description //the R13 store the address of the value, and D store the data, so this
	 * method is to store the top value into memory
	 * @Date 13:23 2021/12/11
	 * @Param []
	 * @return void
	 **/
	private void storeRegValueInMem() throws IOException {
		this.bufferedWriter.write("//store the top value into memory\r\n"
				+ "@R13\r\n"
				+ "A=M\r\n"
				+ "M=D\r\n");
	}

	/*
	 * @Author HuangChuan
	 * @Description //get the value of R13
	 * @Date 13:43 2021/12/11
	 * @Param []
	 * @return void
	 **/
	private void getValueOfR13() throws IOException {
		this.bufferedWriter.write("@R13\r\n"
				+ "D=M\r\n");
	}

	/*
	 * @Author HuangChuan
	 * @Description //add value in R13 and value in R14
	 * @Date 14:11 2021/12/11
	 * @Param []
	 * @return void
	 **/
	private void R13AddR14() throws IOException {
		getValueOfR13();
		this.bufferedWriter.write("@R14\r\n"
				+ "D=D+M\r\n");
	}

	/*
	 * @Author HuangChuan
	 * @Description //let value in R13 minus value in R14
	 * @Date 14:19 2021/12/11
	 * @Param []
	 * @return void
	 **/
	private void R13MinusR14() throws IOException {
		getValueOfR13();
		this.bufferedWriter.write("@R14\r\n"
				+ "D=D-M\r\n");
	}

	/*
	 * @Author HuangChuan
	 * @Description //the repeating code of writeArithmetic() method
	 * @Date 16:24 2021/12/11
	 * @Param [parser, type]
	 * @return void
	 **/
	private void switchEQ(Parser parser, String type) throws IOException {
		this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
		getTwoTopElemAndStoreInReg();
		R13MinusR14();
		this.bufferedWriter.write("@" + type + i + "\r\n"
				+ "D;J" + type + "\r\n");
		push("0");
		this.bufferedWriter.write("@END" + type + i + "\r\n"
				+ "0;JMP\r\n"
				+ "(" + type + i + ")\r\n");
		//1 represents true, 0 represents false
		push("1");
		this.bufferedWriter.write("(END" + type + i + ")\r\n");
		i++;
		this.bufferedWriter.write("\r\n");
	}

	private void switchAndOrNot(Parser parser, String type) throws IOException {
		this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
		getTwoTopElemAndStoreInReg();
		getValueOfR13();
		this.bufferedWriter.write("@R14\r\n"
				+ "D=D" + type + "M\r\n");
		push("D");
		this.bufferedWriter.write("\r\n");
	}

	/*
	 * @Author HuangChuan
	 * @Description //write arithmetic command
	 * @Date 17:38 2021/12/11
	 * @Param [parser]
	 * @return void
	 **/
	public void writeArithmetic(Parser parser) throws IOException {
		//get the command type
		String type = parser.arg1();
		switch (type) {
			case "add":
				this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
				getTwoTopElemAndStoreInReg();
				R13AddR14();
				push("D");
				this.bufferedWriter.write("\r\n");
				break;
			case "sub":
				this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
				getTwoTopElemAndStoreInReg();
				R13MinusR14();
				push("D");
				this.bufferedWriter.write("\r\n");
				break;
			case "eq":
				switchEQ(parser, "EQ");
				break;
			case "gt":
				switchEQ(parser, "GT");
				break;
			case "lt":
				switchEQ(parser, "LT");
				break;
			case "and":
				switchAndOrNot(parser, "&");
				break;
			case "or":
				switchAndOrNot(parser, "|");
				break;
			case "not":
				switchAndOrNot(parser, "!");
				break;
			case "neg":
				this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
				pop();
				this.bufferedWriter.write("@0\r\n"
						+ "D=A-D\r\n");
				push("D");
				this.bufferedWriter.write("\r\n");
				break;
			default:
				break;
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description //write pop and push command
	 * @Date 13:33 2021/12/12
	 * @Param [parser]
	 * @return void
	 **/
	public void writePushPop(Parser parser) throws IOException {
		CommandType commandType = parser.getCommandType();
		if (commandType.equals(CommandType.C_PUSH)) {
			String arg1 = parser.arg1();
			String arg2 = parser.arg2();
			switch (arg1) {
				case "argument":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					pushByArg("ARG", arg2);
					this.bufferedWriter.write("\r\n");
					break;
				case "local":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					pushByArg("LCL", arg2);
					this.bufferedWriter.write("\r\n");
					break;
				case "static":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					this.bufferedWriter.write("@" + this.file.getName().substring(0,
							this.file.getName().indexOf(".") + 1) + arg2 + "\r\n"
							+ "D=M\r\n");
					push("D");
					this.bufferedWriter.write("\r\n");
					break;
				case "constant":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					this.bufferedWriter.write("@" + arg2 + "\r\n"
							+ "D=A\r\n");
					push("D");
					this.bufferedWriter.write("\r\n");
					break;
				case "this":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					pushByArg("THIS", arg2);
					this.bufferedWriter.write("\r\n");
					break;
				case "that":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					pushByArg("THAT", arg2);
					this.bufferedWriter.write("\r\n");
					break;
				case "pointer":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					this.bufferedWriter.write("@THIS\r\n"
							+ "D=A\r\n"
							+ "@" + arg2 + "\r\n"
							+ "A=A+D\r\n"
							+ "D=M\r\n");
					push("D");
					this.bufferedWriter.write("\r\n");
					break;
				case "temp":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					this.bufferedWriter.write("@5\r\n"
							+ "D=A\r\n"
							+ "@" + arg2 + "\r\n"
							+ "A=A+D\r\n"
							+ "D=M\r\n");
					push("D");
					this.bufferedWriter.write("\r\n");
					break;
				default:
					break;
			}
		} else if (commandType.equals(CommandType.C_POP)) {
			String arg1 = parser.arg1();
			String arg2 = parser.arg2();
			switch (arg1) {
				case "argument":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					this.bufferedWriter.write("@ARG\r\n"
							+ "D=M\r\n"
							+ "@" + arg2 + "\r\n"
							+ "D=D+A\r\n");
					storeInReg("R13");
					pop();
					storeRegValueInMem();
					this.bufferedWriter.write("\r\n");
					break;
				case "local":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					this.bufferedWriter.write("@LCL\r\n"
							+ "@" + arg2 + "\r\n"
							+ "D=D+A\r\n");
					storeInReg("R13");
					pop();
					storeRegValueInMem();
					this.bufferedWriter.write("\r\n");
					break;
				case "static":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					pop();
					this.bufferedWriter.write("@" + this.file.getName().substring(0,
							this.file.getName().indexOf(".") + 1) + arg2 + "\r\n"
							+ "M=D\r\n");
					this.bufferedWriter.write("\r\n");
					break;
				case "constant":
					//pop constant is not exist
					break;
				case "this":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					this.bufferedWriter.write("@THIS\r\n"
							+ "D=M\r\n"
							+ "@" + arg2 + "\r\n"
							+ "D=D+A\r\n");
					storeInReg("R13");
					pop();
					storeRegValueInMem();
					this.bufferedWriter.write("\r\n");
					break;
				case "that":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					this.bufferedWriter.write("@THAT\r\n"
							+ "D=M\r\n"
							+ "@" + arg2 + "\r\n"
							+ "D=D+A\r\n");
					storeInReg("R13");
					pop();
					storeRegValueInMem();
					this.bufferedWriter.write("\r\n");
					break;
				case "pointer":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					this.bufferedWriter.write("@THIS\r\n"
							+ "D=A\r\n"
							+ "@" + arg2 + "\r\n"
							+ "D=D+A\r\n");
					storeInReg("R13");
					pop();
					storeRegValueInMem();
					this.bufferedWriter.write("\r\n");
					break;
				case "temp":
					this.bufferedWriter.write("//vm command:" + parser.getCurrentCommand() + "\r\n");
					this.bufferedWriter.write("@5\r\n"
							+ "D=A\r\n"
							+ "@" + arg2 + "\r\n"
							+ "D=D+A\r\n");
					storeInReg("R13");
					pop();
					storeRegValueInMem();
					this.bufferedWriter.write("\r\n");
					break;
				default:
					break;
			}
		} else {
			System.out.println("this command is not push or pop");
		}
	}
	/*
	 * @Author HuangChuan
	 * @Description //close the output file stream
	 * @Date 13:56 2021/12/12
	 * @Param []
	 * @return void
	 **/
	public void close() throws IOException {
		this.bufferedWriter.close();
	}
}
