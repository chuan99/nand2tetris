import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author HuangChuan
 * @Create in 2022/02/23 17:34
 */
public class JackTokenizer {
	private String currentToken;
	private Type currentTokenType;
	private int index;
	private ArrayList<String> tokens;
	private static Pattern tokenPatterns;
	private static String keywordReg;
	private static String symbolReg;
	private static String intReg;
	private static String stringReg;
	private static String identifierReg;
	private static HashMap<String, Keyword> keywordMap = new HashMap<>();
	private static HashSet<Character> opSet = new HashSet<>();

	static {
		keywordMap.put("class", Keyword.CLASS);
		keywordMap.put("method", Keyword.METHOD);
		keywordMap.put("int", Keyword.INT);
		keywordMap.put("function", Keyword.FUNCTION);
		keywordMap.put("boolean", Keyword.BOOLEAN);
		keywordMap.put("constructor", Keyword.CONSTRUCTOR);
		keywordMap.put("char", Keyword.CHAR);
		keywordMap.put("void", Keyword.VOID);
		keywordMap.put("var", Keyword.VAR);
		keywordMap.put("static", Keyword.STATIC);
		keywordMap.put("field", Keyword.FIELD);
		keywordMap.put("let", Keyword.LET);
		keywordMap.put("do", Keyword.DO);
		keywordMap.put("if", Keyword.IF);
		keywordMap.put("else", Keyword.ELSE);
		keywordMap.put("while", Keyword.WHILE);
		keywordMap.put("return", Keyword.RETURN);
		keywordMap.put("true", Keyword.TRUE);
		keywordMap.put("false", Keyword.FALSE);
		keywordMap.put("null", Keyword.NULL);
		keywordMap.put("this", Keyword.THIS);
		opSet.add('+');
		opSet.add('-');
		opSet.add('*');
		opSet.add('/');
		opSet.add('|');
		opSet.add('&');
		opSet.add('<');
		opSet.add('>');
		opSet.add('=');
	}

	public JackTokenizer(File inputFile) {
		try {
			Scanner scanner = new Scanner(inputFile);
			String preprocessed = "";
			String line;
			while (scanner.hasNext()) {
				line = removeComments(scanner.nextLine()).trim();
				if (line.length() > 0) {
					preprocessed += line + "\n";
				}
			}
			preprocessed = removeBlockComments(preprocessed).trim();
			//init all regex
			initRegs();
			Matcher m = tokenPatterns.matcher(preprocessed);
			tokens = new ArrayList<String>();
			index = 0;
			while (m.find()) {
				tokens.add(m.group());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		currentToken = "";
		currentTokenType = Type.NONE;
	}

	/*
	 * @Author HuangChuan
	 * @Description // Init regexps
	 * @Date 20:52 2022/2/23
	 * @Param []
	 * @return void
	 **/
	private void initRegs() {
		keywordReg = "";
		for (String seg : keywordMap.keySet()) {
			keywordReg += seg + "|";
		}
		symbolReg = "[&*+()./,\\-\\];~}|{>=\\[<]";
		intReg = "[0-9]+";
		stringReg = "\"[^\"\n]*\"";
		identifierReg = "[a-zA-Z_]\\w*";

		tokenPatterns = Pattern.compile(identifierReg + "|" + keywordReg + symbolReg + "|" + intReg + "|" + stringReg);
	}

	/*
	 * @Author HuangChuan
	 * @Description // Check if more tokens remain
	 * @Date 20:54 2022/2/23
	 * @Param []
	 * @return boolean
	 **/
	public boolean hasMoreTokens() {
		return index < tokens.size();
	}

	/*
	 * @Author HuangChuan
	 * @Description // Read the next token from input and make it the current token
	 * @Date 17:05 2022/2/24
	 * @Param []
	 * @return void
	 **/
	public void advance() {
		if (hasMoreTokens()) {
			currentToken = tokens.get(index);
			index++;
		} else {
			throw new IllegalStateException("No more tokens");
		}
		if (currentToken.matches(keywordReg)) {
			currentTokenType = Type.KEYWORD;
		} else if (currentToken.matches(intReg)) {
			currentTokenType = Type.INT_CONST;
		} else if (currentToken.matches(stringReg)) {
			currentTokenType = Type.STRING_CONST;
		} else if (currentToken.matches(identifierReg)) {
			currentTokenType = Type.IDENTIFIER;
		} else if (currentToken.matches(symbolReg)) {
			currentTokenType = Type.SYMBOL;
		} else {
			throw new IllegalArgumentException("Undefined token: " + currentToken);
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Get current token
	 * @Date 17:10 2022/2/24
	 * @Param []
	 * @return java.lang.String
	 **/
	public String getCurrentToken() {
		return currentToken;
	}

	/*
	 * @Author HuangChuan
	 * @Description // Return the type of current token
	 * @Date 17:11 2022/2/24
	 * @Param []
	 * @return JackTokenizer.TYPE
	 **/
	public Type tokenType() {
		return currentTokenType;
	}

	/*
	 * @Author HuangChuan
	 * @Description // Return the keyword if current token is a keyword
	 * @Date 17:14 2022/2/24
	 * @Param []
	 * @return JackTokenizer.KEYWORD
	 **/
	public Keyword keyword() {
		if (currentTokenType == Type.KEYWORD) {
			return keywordMap.get(currentToken);
		} else {
			throw new IllegalStateException("Current token is not a keyword");
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Return the symbol if current token is a symbol
	 * @Date 17:16 2022/2/24
	 * @Param []
	 * @return char
	 **/
	public char symbol() {
		if (currentTokenType == Type.SYMBOL) {
			return currentToken.charAt(0);
		} else {
			throw new IllegalStateException("Current token is not a symbol");
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Return the identifier if current token is an identifier
	 * @Date 17:18 2022/2/24
	 * @Param []
	 * @return java.lang.String
	 **/
	public String identifier() {
		if (currentTokenType == Type.IDENTIFIER) {
			return currentToken;
		} else {
			throw new IllegalStateException("Current token is not an identifier");
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Return the int value if current token is an int constant
	 * @Date 17:30 2022/2/24
	 * @Param []
	 * @return int
	 **/
	public int intVal() {
		if (currentTokenType == Type.INT_CONST) {
			return Integer.parseInt(currentToken);
		} else {
			throw new IllegalStateException("Current token is not an integer constant");
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Return the string value of current token is a string constant
	 * @Date 17:32 2022/2/24
	 * @Param []
	 * @return java.lang.String
	 **/
	public String stringVal() {
		if (currentTokenType == Type.STRING_CONST) {
			return currentToken;
		} else {
			throw new IllegalStateException("Current token is not a string constant");
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Reduce the index
	 * @Date 17:34 2022/2/24
	 * @Param []
	 * @return void
	 **/
	public void indexBack() {
		if (index > 0) {
			index--;
			currentToken = tokens.get(index);
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Check if the symbol is an operator
	 * @Date 17:35 2022/2/24
	 * @Param []
	 * @return boolean
	 **/
	public boolean isOp() {
		return opSet.contains(symbol());
	}

	/*
	 * @Author HuangChuan
	 * @Description // Remove "//" comments
	 * @Date 19:20 2022/2/23
	 * @Param [line]
	 * @return java.lang.String
	 **/
	private String removeComments(String line) {
		int i = line.indexOf("//");
		if (i != -1) {
			line = line.substring(0, i);
		}
		return line;
	}

	/*
	 * @Author HuangChuan
	 * @Description // Remove block comment
	 * @Date 20:08 2022/2/23
	 * @Param [line]
	 * @return java.lang.String
	 **/
	private String removeBlockComments(String line) {
		int start = line.indexOf("/*");
		if (start == -1) {
			return line;
		}
		int end = line.indexOf("*/");
		String res = line;
		while (start != -1) {
			if (end == -1) {
				return line.substring(0, start);
			}
			res = res.substring(0, start) + res.substring(end + 2);
			start = res.indexOf("/*");
			end = res.indexOf("*/");
		}
		return res;
	}
}
