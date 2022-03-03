import java.awt.font.ImageGraphicAttribute;
import java.io.File;

/**
 * @Author HuangChuan
 * @Create in 2022/02/24 17:38
 */
public class CompilationEngine {
	private VMWriter vmWriter;
	private JackTokenizer jackTokenizer;
	private SymbolTable symbolTable;
	private String currentClass;
	private String currentSubroutine;
	private int labelIndex;

	public CompilationEngine(File inputFile, File outputFile) {
		jackTokenizer = new JackTokenizer(inputFile);
		vmWriter = new VMWriter(outputFile);
		symbolTable = new SymbolTable();
		labelIndex = 0;
	}

	/*
	 * @Author HuangChuan
	 * @Description // Compile whole class
	 * @Date 21:41 2022/2/26
	 * @Param []
	 * @return void
	 **/
	public void compileClass() {
		//class
		jackTokenizer.advance();
		Type tokenType = jackTokenizer.tokenType();
		Keyword keyword = jackTokenizer.keyword();
		if (tokenType != Type.KEYWORD || keyword != Keyword.CLASS) {
			error("class");
		}
		//className
		jackTokenizer.advance();
		tokenType = jackTokenizer.tokenType();
		if (tokenType != Type.IDENTIFIER) {
			error("className");
		}
		currentClass = jackTokenizer.identifier();
		//{
		requireSymbol('{');
		//classVarDec
		compileClassVarDec();
		//subroutineDec
		compileSubroutine();
		//}
		requireSymbol('}');
		if (jackTokenizer.hasMoreTokens()) {
			throw new IllegalStateException("Unexpected token");
		}
		vmWriter.close();
	}

	/*
	 * @Author HuangChuan
	 * @Description // Compile class var declaration
	 * @Date 14:31 2022/2/28
	 * @Param []
	 * @return void
	 **/
	private void compileClassVarDec() {
		jackTokenizer.advance();
		Type tokenType = jackTokenizer.tokenType();
		//current token is '}', no class var declaration
		if (tokenType == Type.SYMBOL && jackTokenizer.symbol() == '}') {
			jackTokenizer.indexBack();
			return;
		}
		//current token must be a keyword
		if (tokenType != Type.KEYWORD) {
			error("Keyword");
		}
		Keyword keyword = jackTokenizer.keyword();
		//current token starts subroutineDec
		if (keyword == Keyword.CONSTRUCTOR || keyword == Keyword.FUNCTION || keyword == Keyword.METHOD) {
			jackTokenizer.indexBack();
			return;
		}
		//classVarDec
		if (keyword != Keyword.STATIC && keyword != Keyword.FIELD) {
			error("static or field");
		}
		Kind kind = null;
		String type;
		String name;
		switch (keyword) {
			case STATIC:
				kind = Kind.STATIC;
				break;
			case FIELD:
				kind = Kind.FIELD;
				break;
		}
		type = compileType();
		do {
			//varName
			jackTokenizer.advance();
			Type tokenType1 = jackTokenizer.tokenType();
			if (tokenType1 != Type.IDENTIFIER) {
				error("identifier");
			}
			name = jackTokenizer.identifier();
			symbolTable.define(name, type, kind);
			//',' or ';'
			jackTokenizer.advance();
			tokenType1 = jackTokenizer.tokenType();
			if (tokenType1 != Type.SYMBOL || (jackTokenizer.symbol() != ',' && jackTokenizer.symbol() != ';')) {
				error("',' or ';'");
			}
		} while (jackTokenizer.symbol() != ';');
		compileClassVarDec();
	}

	/*
	 * @Author HuangChuan
	 * @Description // Compile a subroutine
	 * @Date 15:04 2022/2/28
	 * @Param []
	 * @return void
	 **/
	private void compileSubroutine() {
		jackTokenizer.advance();
		Type tokenType = jackTokenizer.tokenType();
		//current token is '}', no subroutine
		if (tokenType == Type.SYMBOL && jackTokenizer.symbol() == '}') {
			jackTokenizer.indexBack();
			return;
		}
		//start subroutine, current token must be constructor|function|method
		if (tokenType != Type.KEYWORD || (jackTokenizer.keyword() != Keyword.CONSTRUCTOR && jackTokenizer.keyword() != Keyword.FUNCTION && jackTokenizer.keyword() != Keyword.METHOD)) {
			error("constructor|function|method");
		}
		Keyword keyword = jackTokenizer.keyword();
		symbolTable.startSubroutine();
		//"this" is the first argument of method
		if (keyword == Keyword.METHOD) {
			symbolTable.define("this", currentClass, Kind.ARG);
		}
		String type = "";
		//"void" or type
		jackTokenizer.advance();
		tokenType = jackTokenizer.tokenType();
		if (tokenType == Type.KEYWORD && jackTokenizer.keyword() == Keyword.VOID) {
			type = "void";
		} else {
			jackTokenizer.indexBack();
			type = compileType();
		}
		//subroutineName
		jackTokenizer.advance();
		tokenType = jackTokenizer.tokenType();
		if (tokenType != Type.IDENTIFIER) {
			error("subroutineName");
		}
		currentSubroutine = jackTokenizer.identifier();
		//(
		requireSymbol('(');
		//parameterList
		compileParameterList();
		//)
		requireSymbol(')');
		compileSubroutineBody(keyword);
		compileSubroutine();
	}


	/*
	 * @Author HuangChuan
	 * @Description // Compile subroutine body
	 * @Date 15:11 2022/2/28
	 * @Param [keyword]
	 * @return void
	 **/
	private void compileSubroutineBody(Keyword keyword) {
		//{
		requireSymbol('{');
		//varDec
		compileVarDec();
		writeFunctionDec(keyword);
		//statements
		compileStatements();
		//}
		requireSymbol('}');
	}


	/*
	 * @Author HuangChuan
	 * @Description // Write function declaration, and init pointer if the keyword is "method" or "constructor"
	 * @Date 15:56 2022/2/28
	 * @Param [keyword]
	 * @return void
	 **/
	private void writeFunctionDec(Keyword keyword) {
		vmWriter.writeFunction(currentFunction(), symbolTable.varCount(Kind.VAR));
		if (keyword == Keyword.METHOD) {
			//"this" pointer is the first argument of a method
			vmWriter.writePush(Segment.ARG, 0);
			vmWriter.writePop(Segment.POINTER, 0);
		} else if (keyword == Keyword.CONSTRUCTOR) {
			//call VM function Memory.alloc to allocate a memory block for the new object and let the
			//"this" pointer point to this address
			vmWriter.writePush(Segment.CONST, symbolTable.varCount(Kind.FIELD));
			vmWriter.writeCall("Memory.alloc", 1);
			vmWriter.writePop(Segment.POINTER, 0);
		}
	}


	/*
	 * @Author HuangChuan
	 * @Description // Compile parameter list
	 * @Date 16:39 2022/2/28
	 * @Param []
	 * @return void
	 **/
	private void compileParameterList() {
		jackTokenizer.advance();
		Type tokenType = jackTokenizer.tokenType();
		//end parameter list
		if (tokenType == Type.SYMBOL && jackTokenizer.symbol() == ')') {
			jackTokenizer.indexBack();
			return;
		}
		String type;
		jackTokenizer.indexBack();
		do {
			//type
			type = compileType();
			//varName
			jackTokenizer.advance();
			tokenType = jackTokenizer.tokenType();
			if (tokenType != Type.IDENTIFIER) {
				error("identifier");
			}
			symbolTable.define(jackTokenizer.identifier(), type, Kind.ARG);
			//',' or ';'
			jackTokenizer.advance();
			tokenType = jackTokenizer.tokenType();
			if (tokenType != Type.SYMBOL || (jackTokenizer.symbol() != ',' && jackTokenizer.symbol() != ')')) {
				error("',' or ')'");
			}
			if (jackTokenizer.symbol() == ')') {
				jackTokenizer.indexBack();
				break;
			}
		} while (true);
	}

	/*
	 * @Author HuangChuan
	 * @Description // Compile var declaration
	 * @Date 16:15 2022/2/28
	 * @Param []
	 * @return void
	 **/
	private void compileVarDec() {
		//var
		jackTokenizer.advance();
		Type tokenType = jackTokenizer.tokenType();
		//end var declaration
		if (tokenType != Type.KEYWORD || jackTokenizer.keyword() != Keyword.VAR) {
			jackTokenizer.indexBack();
			return;
		}
		//type
		String type = compileType();
		do {
			//varName
			jackTokenizer.advance();
			tokenType = jackTokenizer.tokenType();
			if (tokenType != Type.IDENTIFIER) {
				error("identifier");
			}
			symbolTable.define(jackTokenizer.identifier(), type, Kind.VAR);
			//',' or ';'
			jackTokenizer.advance();
			tokenType = jackTokenizer.tokenType();
			if (tokenType != Type.SYMBOL || (jackTokenizer.symbol() != ',' && jackTokenizer.symbol() != ';')) {
				error("',' or ';'");
			}
		} while (jackTokenizer.symbol() != ';');
		compileVarDec();
	}

	/*
	 * @Author HuangChuan
	 * @Description // Compile a series of statements
	 * @Date 16:52 2022/2/28
	 * @Param []
	 * @return void
	 **/
	private void compileStatements() {
		jackTokenizer.advance();
		Type tokenType = jackTokenizer.tokenType();
		//end statements
		if (tokenType == Type.SYMBOL && jackTokenizer.symbol() == '}') {
			jackTokenizer.indexBack();
			return;
		}
		if (tokenType != Type.KEYWORD) {
			error("keyword");
		} else {
			switch (jackTokenizer.keyword()) {
				case DO:
					compileDo();
					break;
				case LET:
					compileLet();
					break;
				case IF:
					compileIf();
					break;
				case WHILE:
					compileWhile();
					break;
				case RETURN:
					compileReturn();
					break;
				default:
					error("do|let|if|while|return");
			}
		}
		compileStatements();
	}

	/*
	 * @Author HuangChuan
	 * @Description // Compile do statement
	 * @Date 17:08 2022/2/28
	 * @Param []
	 * @return void
	 **/
	private void compileDo() {
		//subroutineCall
		compileSubroutineCall();
		//';'
		requireSymbol(';');
		//return value
		vmWriter.writePop(Segment.TEMP, 0);
	}

	/*
	 * @Author HuangChuan
	 * @Description // Compile subroutine call
	 * @Date 17:12 2022/3/2
	 * @Param []
	 * @return void
	 **/
	private void compileSubroutineCall() {
		//subroutineName or className or varName
		jackTokenizer.advance();
		Type tokenType = jackTokenizer.tokenType();
		if (tokenType != Type.IDENTIFIER) {
			error("identifier");
		}
		String name = jackTokenizer.identifier();
		int nArgs = 0;
		jackTokenizer.advance();
		tokenType = jackTokenizer.tokenType();
		//'('
		if (tokenType == Type.SYMBOL && jackTokenizer.symbol() == '(') {
			//push "this" pointer
			vmWriter.writePush(Segment.POINTER, 0);
			//expressionList
			nArgs = compileExpressionList() + 1;
			//')'
			requireSymbol(')');
			//call subroutine
			vmWriter.writeCall(currentClass + "." + name, nArgs);
			//'.'
		} else if (tokenType == Type.SYMBOL && jackTokenizer.symbol() == '.') {
			String classOrVarName = name;
			//subroutineName
			jackTokenizer.advance();
			tokenType = jackTokenizer.tokenType();
			if (tokenType != Type.IDENTIFIER) {
				error("identifier");
			}
			name = jackTokenizer.identifier();
			String type = symbolTable.typeOf(classOrVarName);
			if (type.equals("")) {
				name = classOrVarName + "." + name;
			} else if (type.equals("int") || type.equals("boolean") || type.equals("char") || type.equals("void")) {
				error("no built-in type");
			} else {
				nArgs = 1;
				vmWriter.writePush(getSegment(symbolTable.kindOf(classOrVarName)), symbolTable.indexOf(classOrVarName));
				name = symbolTable.typeOf(classOrVarName) + "." + name;
			}
			//'('
			requireSymbol('(');
			//expressionList
			nArgs += compileExpressionList();
			//')'
			requireSymbol(')');
			//call subroutine
			vmWriter.writeCall(name, nArgs);
		} else {
			error("',' | '.'");
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Return the corresponding segment of kind
	 * @Date 16:54 2022/3/2
	 * @Param [kind]
	 * @return Segment
	 **/
	private Segment getSegment(Kind kind) {
		switch (kind) {
			case VAR:
				return Segment.LOCAL;
			case STATIC:
				return Segment.STATIC;
			case ARG:
				return Segment.ARG;
			case FIELD:
				return Segment.THIS;
			default:
				return Segment.NONE;
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Compile let statement
	 * @Date 17:47 2022/3/2
	 * @Param []
	 * @return void
	 **/
	private void compileLet() {
		//varName
		jackTokenizer.advance();
		Type tokenType = jackTokenizer.tokenType();
		if (tokenType != Type.IDENTIFIER) {
			error("identifier");
		}
		String varName = jackTokenizer.identifier();
		//'[' or '='
		jackTokenizer.advance();
		tokenType = jackTokenizer.tokenType();
		if (tokenType != Type.SYMBOL && (jackTokenizer.symbol() != '[' && jackTokenizer.symbol() != '=')) {
			error("'[' | '='");
		}
		boolean isExp = false;
		if (jackTokenizer.symbol() == '[') {
			isExp = true;
			//push base address of array variable
			vmWriter.writePush(getSegment(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));
			//expression, offset
			compileExpression();
			//']'
			requireSymbol(']');
			//calculate the address of array element which is accessed
			vmWriter.writeArithmetic(Command.ADD);
		}
		if (isExp) {
			//'='
			jackTokenizer.advance();
		}
		//expression
		compileExpression();
		//';'
		requireSymbol(';');
		if (isExp) {
			//pop expression value to temp
			vmWriter.writePop(Segment.LOCAL, 0);
			//pop base+offset address to that
			vmWriter.writePop(Segment.POINTER, 1);
			//pop expression value to *(base+offset)
			vmWriter.writePush(Segment.TEMP, 0);
			vmWriter.writePop(Segment.THAT, 0);
		} else {
			//pop expression value directly
			vmWriter.writePop(getSegment(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Compile while statement
	 * @Date 17:58 2022/3/2
	 * @Param []
	 * @return void
	 **/
	private void compileWhile() {
		String whileLabel = newLabel();
		String nextLabel = newLabel();
		//label of while loop
		vmWriter.writeLabel(whileLabel);
		//'('
		requireSymbol('(');
		//expression
		compileExpression();
		//')'
		requireSymbol(')');
		//if the condition expression is false, goto next label
		vmWriter.writeArithmetic(Command.NOT);
		vmWriter.writeIf(nextLabel);
		//'{'
		requireSymbol('{');
		//statements
		compileStatements();
		//'}'
		requireSymbol('}');
		//the condition expression is true, goto while label
		vmWriter.writeGoto(whileLabel);
		//next label
		vmWriter.writeLabel(nextLabel);
	}

	/*
	 * @Author HuangChuan
	 * @Description // Return a label
	 * @Date 17:51 2022/3/2
	 * @Param []
	 * @return java.lang.String
	 **/
	private String newLabel() {
		return "LABEL_" + labelIndex++;
	}

	/*
	 * @Author HuangChuan
	 * @Description // Compile return statement
	 * @Date 21:42 2022/3/2
	 * @Param []
	 * @return void
	 **/
	private void compileReturn() {
		jackTokenizer.advance();
		Type tokenType = jackTokenizer.tokenType();
		//no expression, end return
		if (tokenType == Type.SYMBOL && jackTokenizer.symbol() == ';') {
			//no return expression, return 0
			vmWriter.writePush(Segment.CONST, 0);
		} else {
			jackTokenizer.indexBack();
			//expression
			compileExpression();
			//';'
			requireSymbol(';');
		}
		vmWriter.writeReturn();
	}


	/*
	 * @Author HuangChuan
	 * @Description // Compile if statement
	 * @Date 22:18 2022/3/2
	 * @Param []
	 * @return void
	 **/
	private void compileIf() {
		String elseLabel = newLabel();
		String endLabel = newLabel();
		//'('
		requireSymbol('(');
		//expression
		compileExpression();
		//')'
		requireSymbol(')');
		//if the condition expression is false, goto else label
		vmWriter.writeArithmetic(Command.NOT);
		vmWriter.writeIf(elseLabel);
		//'{'
		requireSymbol('{');
		//statements
		compileStatements();
		//'}'
		requireSymbol('}');
		//if the condition expression is true, goto end label
		vmWriter.writeGoto(endLabel);
		vmWriter.writeLabel(elseLabel);
		jackTokenizer.advance();
		Type tokenType = jackTokenizer.tokenType();
		//else
		if (tokenType == Type.KEYWORD && jackTokenizer.keyword() == Keyword.ELSE) {
			//'{'
			requireSymbol('{');
			//statements
			compileStatements();
			//'}'
			requireSymbol('}');
			//no else
		} else {
			jackTokenizer.indexBack();
		}
		vmWriter.writeLabel(endLabel);
	}

	/*
	 * @Author HuangChuan
	 * @Description // Compile expression
	 * @Date 22:31 2022/3/2
	 * @Param []
	 * @return void
	 **/
	private void compileExpression() {
		//term
		compileTerm();
		do {
			jackTokenizer.advance();
			Type tokenType = jackTokenizer.tokenType();
			//op
			if (tokenType == Type.SYMBOL && jackTokenizer.isOp()) {
				String op = "";
				switch (jackTokenizer.symbol()) {
					case '+':
						op = "add";
						break;
					case '-':
						op = "sub";
						break;
					case '*':
						op = "call Math.multiply 2";
						break;
					case '/':
						op = "call Math.divide 2";
						break;
					case '<':
						op = "lt";
						break;
					case '>':
						op = "gt";
						break;
					case '=':
						op = "eq";
						break;
					case '&':
						op = "and";
						break;
					case '|':
						op = "or";
						break;
					default:
						error("unknown op");
				}
				//term
				compileTerm();
				vmWriter.writeCommand(op, "", "");
				//no op
			} else {
				jackTokenizer.indexBack();
				break;
			}

		} while (true);
	}

	/*
	 * @Author HuangChuan
	 * @Description // Compile expression list and return the num of expressions
	 * @Date 16:19 2022/3/2
	 * @Param []
	 * @return int
	 **/
	private int compileExpressionList() {
		int nArgs = 0;
		jackTokenizer.advance();
		Type tokenType = jackTokenizer.tokenType();
		//expression list is empty
		if (tokenType == Type.SYMBOL && jackTokenizer.symbol() == ')') {
			jackTokenizer.indexBack();
		} else {
			nArgs = 1;
			jackTokenizer.indexBack();
			//expression
			compileExpression();
			do {
				jackTokenizer.advance();
				tokenType = jackTokenizer.tokenType();
				//','
				if (tokenType == Type.SYMBOL && jackTokenizer.symbol() == ',') {
					//expression
					compileExpression();
					nArgs++;
					//end expression list
				} else {
					jackTokenizer.indexBack();
					break;
				}
			} while (true);
		}
		return nArgs;
	}

	private void compileTerm() {
		jackTokenizer.advance();
		Type tokenType = jackTokenizer.tokenType();
		//varName|varName'['expression']'|subroutineCall
		if (tokenType == Type.IDENTIFIER) {
			String identifier = jackTokenizer.identifier();
			jackTokenizer.advance();
			tokenType = jackTokenizer.tokenType();
			//'['
			if (tokenType == Type.SYMBOL && jackTokenizer.symbol() == '[') {
				//push array variable base address
				vmWriter.writePush(getSegment(symbolTable.kindOf(identifier)), symbolTable.indexOf(identifier));
				//expression
				compileExpression();
				//']'
				requireSymbol(']');
				//add base+offset address
				vmWriter.writeArithmetic(Command.ADD);
				//pop to that pointer
				vmWriter.writePop(Segment.POINTER, 1);
				//push *(base+offset)
				vmWriter.writePush(Segment.THAT, 0);
				//subroutineCall
			} else if (tokenType == Type.SYMBOL && (jackTokenizer.symbol() == '(' || jackTokenizer.symbol() == '.')) {
				jackTokenizer.indexBack();
				jackTokenizer.indexBack();
				compileSubroutineCall();
				//this term has only a varName
			} else {
				jackTokenizer.indexBack();
				//push variable directly
				vmWriter.writePush(getSegment(symbolTable.kindOf(identifier)), symbolTable.indexOf(identifier));
			}
		} else {
			//integerConstant|stringConstant|keywordConstant|'('expression')'|unaryOp term
			tokenType = jackTokenizer.tokenType();
			if (tokenType == Type.INT_CONST) {
				//push integerConstant
				vmWriter.writePush(Segment.CONST, jackTokenizer.intVal());
			} else if (tokenType == Type.STRING_CONST) {
				String s = jackTokenizer.stringVal();
				//new a string and append chars to it
				vmWriter.writePush(Segment.CONST, s.length());
				vmWriter.writeCall("String.new", 1);
			} else if (tokenType == Type.KEYWORD && jackTokenizer.keyword() == Keyword.TRUE) {
				//true is ~0
				vmWriter.writePush(Segment.CONST, 0);
				vmWriter.writeArithmetic(Command.NOT);
			} else if (tokenType == Type.KEYWORD && (jackTokenizer.keyword() == Keyword.FALSE || jackTokenizer.keyword() == Keyword.NULL)) {
				//0 represents false and null
				vmWriter.writePush(Segment.CONST, 0);
			} else if (tokenType == Type.KEYWORD && jackTokenizer.keyword() == Keyword.THIS) {
				vmWriter.writePush(Segment.POINTER, 0);
			} else if (tokenType == Type.SYMBOL && jackTokenizer.symbol() == '(') {
				//expression
				compileExpression();
				//')'
				requireSymbol(')');
			} else if (tokenType == Type.SYMBOL && (jackTokenizer.symbol() == '-' || jackTokenizer.symbol() == '~')) {
				char symbol = jackTokenizer.symbol();
				//term
				compileTerm();
				if (symbol == '-') {
					vmWriter.writeArithmetic(Command.NEG);
				} else {
					vmWriter.writeArithmetic(Command.NOT);
				}
			} else {
				error("integerConstant|stringConstant|keywordConstant|'('expression')'|unaryOp term");
			}
		}
	}


	/*
	 * @Author HuangChuan
	 * @Description // return function name
	 * @Date 15:13 2022/2/28
	 * @Param []
	 * @return java.lang.String
	 **/
	private String currentFunction() {
		if (currentClass.length() != 0 && currentSubroutine.length() != 0) {
			return currentClass + "." + currentSubroutine;
		}
		return "";
	}

	/*
	 * @Author HuangChuan
	 * @Description // The current token must be specified symbol
	 * @Date 21:36 2022/2/26
	 * @Param [c]
	 * @return void
	 **/
	private void requireSymbol(char c) {
		jackTokenizer.advance();
		if (jackTokenizer.tokenType() != Type.SYMBOL || jackTokenizer.symbol() != c) {
			error("'" + c + "'");
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Throw an exception
	 * @Date 21:35 2022/2/26
	 * @Param [s]
	 * @return void
	 **/
	private void error(String s) {
		throw new IllegalStateException("Expected token missing: " + s + " Current token: " + jackTokenizer.getCurrentToken());
	}

	/*
	 * @Author HuangChuan
	 * @Description // Compile a type
	 * @Date 10:56 2022/2/28
	 * @Param []
	 * @return java.lang.String
	 **/
	private String compileType() {
		jackTokenizer.advance();
		Type tokenType = jackTokenizer.tokenType();
		//user-defined type
		if (tokenType == Type.IDENTIFIER) {
			return jackTokenizer.identifier();
		}
		if (tokenType == Type.KEYWORD) {
			Keyword keyword = jackTokenizer.keyword();
			if (keyword == Keyword.INT || keyword == Keyword.CHAR || keyword == Keyword.BOOLEAN) {
				return jackTokenizer.getCurrentToken();
			}
		}
		error("int|char|boolean|className");
		return "";
	}
}
