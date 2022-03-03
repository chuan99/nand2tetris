import java.util.HashMap;

/**
 * @Author HuangChuan
 * @Create in 2022/02/22 12:39
 */
public class SymbolTable {
	//symbol table under class scope
	private HashMap<String, Symbol> classTable;
	//symbol table under subroutine scope
	private HashMap<String, Symbol> subroutineTable;
	//records the number of variables defined in the current scope
	private HashMap<Kind, Integer> count;

	/*
	 * @Author HuangChuan
	 * @Description // Constructor of SymbolTable
	 * @Date 13:12 2022/2/22
	 * @Param []
	 * @return
	 **/
	public SymbolTable() {
		this.classTable = new HashMap<>();
		this.subroutineTable = new HashMap<>();
		this.count = new HashMap<>();
		count.put(Kind.STATIC, 0);
		count.put(Kind.FIELD, 0);
		count.put(Kind.ARG, 0);
		count.put(Kind.VAR, 0);
	}

	/*
	 * @Author HuangChuan
	 * @Description // Start a new subroutine, reset the subroutine table
	 * @Date 19:39 2022/2/22
	 * @Param []
	 * @return void
	 **/
	public void startSubroutine() {
		subroutineTable.clear();
		count.put(Kind.ARG, 0);
		count.put(Kind.VAR, 0);
	}

	/*
	 * @Author HuangChuan
	 * @Description // Define a new identifier given name, type and kind
	 * @Date 13:27 2022/2/22
	 * @Param [name, type, kind]
	 * @return void
	 **/
	public void define(String name, String type, Kind kind) {
		if (kind == Kind.STATIC || kind == Kind.FIELD) {
			int index = count.get(kind);
			Symbol symbol = new Symbol(type, kind, index);
			count.put(kind, index + 1);
			classTable.put(name, symbol);
		} else if (kind == Kind.ARG || kind == Kind.VAR) {
			int index = count.get(kind);
			Symbol symbol = new Symbol(type, kind, index);
			count.put(kind, index + 1);
			subroutineTable.put(name, symbol);
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description // Return the number of variables defined in current scope
	 * @Date 13:30 2022/2/22
	 * @Param [kind]
	 * @return int
	 **/
	public int varCount(Kind kind) {
		return count.get(kind);
	}

	/*
	 * @Author HuangChuan
	 * @Description // Return the kind of the symbol
	 * @Date 13:42 2022/2/22
	 * @Param [name]
	 * @return Symbol.KIND
	 **/
	public Kind kindOf(String name) {
		Symbol symbol = checkAndGetSymbol(name);
		if (symbol != null) {
			return symbol.getKind();
		}
		return Kind.NONE;
	}

	/*
	 * @Author HuangChuan
	 * @Description //Return the index of the symbol
	 * @Date 13:44 2022/2/22
	 * @Param [name]
	 * @return int
	 **/
	public int indexOf(String name) {
		Symbol symbol = checkAndGetSymbol(name);
		if (symbol != null) {
			return symbol.getIndex();
		}
		return -1;
	}

	/*
	 * @Author HuangChuan
	 * @Description // Return the type of the symbol
	 * @Date 13:45 2022/2/22
	 * @Param [name]
	 * @return java.lang.String
	 **/
	public String typeOf(String name) {
		Symbol symbol = checkAndGetSymbol(name);
		if (symbol != null) {
			return symbol.getType();
		}
		return "";
	}

	/*
	 * @Author HuangChuan
	 * @Description // Check if the symbol exists and get the symbol
	 * @Date 13:40 2022/2/22
	 * @Param [name]
	 * @return Symbol
	 **/
	private Symbol checkAndGetSymbol(String name) {
		if (classTable.get(name) != null) {
			return classTable.get(name);
		} else if (subroutineTable.get(name) != null) {
			return subroutineTable.get(name);
		} else {
			return null;
		}
	}
}
