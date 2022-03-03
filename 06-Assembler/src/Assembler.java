import java.io.*;

/**
 * @Author HuangChuan
 * @Create in 2021/12/04 15:27
 */
public class Assembler {
	//store compiling file
	private File file;
	//store compiled file
	private File binFile;
	private Parser parser;
	private Code code;
	private SymbolTable symbolTable;
	private int freeVarAddress = 16;
	//the stream to read/write the program file
	BufferedWriter bufferedWriter;

	/*
	 * @Author HuangChuan
	 * @Description //We think that one file has one symbol table, initialize the symbol table in
	 *  the constructor
	 * @Date 22:24 2021/12/4
	 * @Param [file]
	 * @return
	 **/
	public Assembler(File file) throws IOException {
		this.file = file;
		String fileName = file.getName();
		String binFilePath = file.getParent() + "\\" +
				fileName.substring(0, fileName.indexOf(".")) + "cmp" +
				".hack";
		this.bufferedWriter =
				new BufferedWriter(new OutputStreamWriter(new FileOutputStream(binFilePath)));
		this.code = new Code();
		this.symbolTable = new SymbolTable();
	}

	/*
	 * @Author HuangChuan
	 * @Description //the first scan of the file, collect the symbol of L-Command and add them to
	 *  symbol table
	 * @Date 22:42 2021/12/4
	 * @Param []
	 * @return void
	 **/
	public void firstScanFile() throws FileNotFoundException {
		this.parser = new Parser(this.file, new FileInputStream(this.file));
		do {
			parser.advance();
			CommandType commandType = parser.getCommandType();
			if (commandType.equals(CommandType.L_Command)) {
				String command = parser.getCurrentCommand().substring(1,
						parser.getCurrentCommand().length() - 1);
				if (!parser.isDigit(command)) {
					this.symbolTable.addEntry(command, parser.getAddress());
				}
			}
		} while (parser.hasMoreCommands());
	}

	/*
	 * @Author HuangChuan
	 * @Description //the second scan of the file, in this method, we translate the program file.
	 * Remember that the L-Command needn't to translate.
	 * @Date 11:43 2021/12/9
	 * @Param []
	 * @return void
	 **/
	public void secondScanFile() throws IOException {
		this.parser = new Parser(this.file, new FileInputStream(this.file));
		do {
			parser.advance();
			CommandType commandType = parser.getCommandType();
			switch (commandType) {
				case A_Command:
					String symbol = parser.getCurrentCommand().substring(1);
					if (parser.isDigit(symbol)) {
						System.out.println(parser.getCurrentCommand() + "\t\t" + parser.symbol());
						bufferedWriter.write(parser.symbol() + "\n");
					} else {
						int address = 0;
						if (this.symbolTable.contains(symbol)) {
							address = this.symbolTable.getAddress(symbol);
						} else {
							this.symbolTable.addEntry(symbol, this.freeVarAddress);
							address = this.freeVarAddress;
							freeVarAddress++;
						}
						String addressStr = parser.paddingZero(Integer.toBinaryString(address));
						System.out.println(parser.getCurrentCommand() + "\t\t" + addressStr);
						bufferedWriter.write(addressStr + "\n");
					}
					break;
				case C_Command:
					System.out.println(parser.getCurrentCommand() + "\t\t");
					String dest = code.dest(parser.dest());
					String comp = code.comp(parser.comp());
					String jump = code.jump(parser.jump());
					String binCmd = "111" + comp + dest + jump;
					System.out.println(binCmd);
					bufferedWriter.write(binCmd + "\n");
					break;
				case L_Command:
					System.out.println(parser.getCurrentCommand());
					break;
				default:
					break;
			}
		} while (parser.hasMoreCommands());
		this.bufferedWriter.flush();
	}

	public static void main(String[] args) throws IOException {
		File file = new File("D:\\GoogleBrowserDownload\\nand2tetris\\projects\\06\\pong\\Pong" +
				".asm");
		Assembler assembler = new Assembler(file);
		assembler.firstScanFile();
		assembler.secondScanFile();
	}
}
