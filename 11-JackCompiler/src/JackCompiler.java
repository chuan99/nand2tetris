import java.io.File;
import java.util.ArrayList;

/**
 * @Author HuangChuan
 * @Create in 2022/03/03 15:12
 */
public class JackCompiler {

	private static ArrayList<File> getJackFiles(File dir) {
		File[] files = dir.listFiles();
		ArrayList<File> res = new ArrayList<>();
		if (files == null) {
			return res;
		}
		for (File f : files) {
			if (f.getName().endsWith(".jack")) {
				res.add(f);
			}
		}
		return res;
	}

	public static void main(String[] args) {
		if (args.length != 1){
			System.out.println("Usage:java JackCompiler [filename|directory]");
		}else {
			String fileInName = args[0];
			File fileIn = new File(fileInName);
			String fileOutPath = "";
			File fileOut;
			ArrayList<File> jackFiles = new ArrayList<File>();
			if (fileIn.isFile()) {
				//if it is a single file, see whether it is a vm file
				String path = fileIn.getAbsolutePath();
				if (!path.endsWith(".jack")) {
					throw new IllegalArgumentException(".jack file is required!");
				}
				jackFiles.add(fileIn);
			} else if (fileIn.isDirectory()) {
				//if it is a directory get all jack files under this directory
				jackFiles = getJackFiles(fileIn);
				//if no vn file in this directory
				if (jackFiles.size() == 0) {
					throw new IllegalArgumentException("No jack file in this directory");
				}
			}
			for (File f: jackFiles) {
				fileOutPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")) + ".vm";
				fileOut = new File(fileOutPath);
				CompilationEngine compilationEngine = new CompilationEngine(f,fileOut);
				compilationEngine.compileClass();
				System.out.println("File created : " + fileOutPath);
			}
		}
	}
}
