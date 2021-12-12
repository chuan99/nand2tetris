import java.util.HashMap;

/**
 * @Author HuangChuan
 * @Create in 2021/12/04 15:01
 */
public class SymbolTable {
	//the String represent the symbol, the Integer represent the decimal address of the symbol
	private HashMap<String, Integer> table;

	/*
	 * @Author HuangChuan
	 * @Description //When we construct the symbol table, we need to add the predefined symbol
	 * @Date 15:19 2021/12/4
	 * @Param []
	 * @return
	 **/
	public SymbolTable() {
		this.table = new HashMap<>();
		addEntry("SP", 0);
		addEntry("LCL", 1);
		addEntry("ARG", 2);
		addEntry("THIS", 3);
		addEntry("THAT", 4);
		for (int i = 0; i < 16; i++) {
			addEntry("R" + i, i);
		}
		addEntry("SCREEN", 16384);
		addEntry("KBD", 24576);
	}

	/*
	 * @Author HuangChuan
	 * @Description //add an entry to symbol table
	 * @Date 15:15 2021/12/4
	 * @Param [symbol, address]
	 * @return void
	 **/
	public void addEntry(String symbol, Integer address) {
		this.table.put(symbol, address);
	}

	/*
	 * @Author HuangChuan
	 * @Description //Judge whether the symbol table contains the symbol
	 * @Date 15:23 2021/12/4
	 * @Param [symbol]
	 * @return boolean
	 **/
	public boolean contains(String symbol) {
		if (this.table.containsKey(symbol)) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * @Author HuangChuan
	 * @Description //get the address of the symbol
	 * @Date 15:25 2021/12/4
	 * @Param [symbol]
	 * @return int
	 **/
	public int getAddress(String symbol) {
		if (contains(symbol)) {
			return this.table.get(symbol);
		}
		return -1;
	}
}
