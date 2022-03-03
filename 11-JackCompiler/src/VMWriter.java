import java.io.*;
import java.util.HashMap;

/**
 * @Author HuangChuan
 * @Create in 2022/02/22 21:08
 */
public class VMWriter {

	private static HashMap<Segment, String> SegmentMap = new HashMap<>();
	private static HashMap<Command, String> CommandMap = new HashMap<>();
	private BufferedWriter writer;

	static {
		SegmentMap.put(Segment.CONST, "constant");
		SegmentMap.put(Segment.ARG, "argument");
		SegmentMap.put(Segment.LOCAL, "local");
		SegmentMap.put(Segment.STATIC, "static");
		SegmentMap.put(Segment.THIS, "this");
		SegmentMap.put(Segment.THAT, "that");
		SegmentMap.put(Segment.POINTER, "pointer");
		SegmentMap.put(Segment.TEMP, "temp");
		CommandMap.put(Command.ADD, "add");
		CommandMap.put(Command.SUB, "sub");
		CommandMap.put(Command.NEG, "neg");
		CommandMap.put(Command.EQ, "eq");
		CommandMap.put(Command.GT, "gt");
		CommandMap.put(Command.LT, "lt");
		CommandMap.put(Command.AND, "and");
		CommandMap.put(Command.OR, "or");
		CommandMap.put(Command.NOT, "not");
	}

	public VMWriter(File outputFile) {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Write push Command
	 * @Date 16:29 2022/2/23
	 * @Param [Segment, index]
	 * @return void
	 **/
	public void writePush(Segment Segment, int index) {
		writeCommand("push", SegmentMap.get(Segment), String.valueOf(index));
	}

	/*
	 * @Author HuangChuan
	 * @Description // Write pop Command
	 * @Date 16:30 2022/2/23
	 * @Param [Segment, index]
	 * @return void
	 **/
	public void writePop(Segment Segment, int index) {
		writeCommand("pop", SegmentMap.get(Segment), String.valueOf(index));
	}

	/*
	 * @Author HuangChuan
	 * @Description // Write arithmetic Command
	 * @Date 16:33 2022/2/23
	 * @Param [Command]
	 * @return void
	 **/
	public void writeArithmetic(Command Command) {
		writeCommand(CommandMap.get(Command), "", "");
	}

	/*
	 * @Author HuangChuan
	 * @Description // Write label Command
	 * @Date 16:34 2022/2/23
	 * @Param [label]
	 * @return void
	 **/
	public void writeLabel(String label) {
		writeCommand("label", label, "");
	}

	/*
	 * @Author HuangChuan
	 * @Description // Write goto Command
	 * @Date 16:35 2022/2/23
	 * @Param [label]
	 * @return void
	 **/
	public void writeGoto(String label) {
		writeCommand("goto", label, "");
	}

	/*
	 * @Author HuangChuan
	 * @Description // Write if-goto Command
	 * @Date 16:46 2022/2/23
	 * @Param [label]
	 * @return void
	 **/
	public void writeIf(String label) {
		writeCommand("if-goto", label, "");
	}

	/*
	 * @Author HuangChuan
	 * @Description // Write call Command
	 * @Date 16:49 2022/2/23
	 * @Param [name, nArgs]
	 * @return void
	 **/
	public void writeCall(String name, int nArgs) {
		writeCommand("call", name, String.valueOf(nArgs));
	}

	/*
	 * @Author HuangChuan
	 * @Description // Write function Command
	 * @Date 16:50 2022/2/23
	 * @Param [name, nArgs]
	 * @return void
	 **/
	public void writeFunction(String name, int nArgs) {
		writeCommand("function", name, String.valueOf(nArgs));
	}

	/*
	 * @Author HuangChuan
	 * @Description // Write return Command
	 * @Date 16:58 2022/2/23
	 * @Param []
	 * @return void
	 **/
	public void writeReturn() {
		writeCommand("return", "", "");
	}

	/*
	 * @Author HuangChuan
	 * @Description // Close the output file
	 * @Date 17:01 2022/2/23
	 * @Param []
	 * @return void
	 **/
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Write vm Command into output file
	 * @Date 16:25 2022/2/23
	 * @Param [cmd, Segment, index]
	 * @return void
	 **/
	public void writeCommand(String cmd, String Segment, String index) {
		try {
			writer.write(cmd + " " + Segment + " " + index + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
