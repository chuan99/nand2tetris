/**
  @Author: HuangChuan
  @Date: 2022/1/25 20:06
  @Description:// Get input from tokenizer, and export the results after analysis into output file
**/
package main

import (
	"fmt"
	"os"
	"regexp"
	"strings"
)

//
//  CompileClass
//  @Description: Compile the whole class
//  @param tokens
//  @param outputFile
//
func CompileClass(tokens []string, outputFile *os.File) {
	fmt.Fprintln(outputFile, "<class>")
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		if _type == TokenTypeKeyword {
			//start class var declaration
			if token == KeywordStatic || token == KeywordField {
				tokeni = i + compileClassVarDec(tokens[i:], outputFile)
				continue
			}
			//start subroutine
			if token == KeywordFunction || token == KeywordMethod ||
				token == KeywordConstructor {
				tokeni = i + compileSubroutine(tokens[i:], outputFile)
				continue
			}
			compileKeyword(token, outputFile)
		}
		if _type == TokenTypeSymbol {
			compileSymbol(token, outputFile)
		}
		if _type == TokenTypeIdentifier {
			compileIdentifier(token, outputFile)
		}
	}
	//end class
	fmt.Fprintln(outputFile, "</class>")
}

//
//  compileClassVarDec
//  @Description: Compile the static or field var declaration
//  @param tokens
//  @param outputFile
//  @return int
//
func compileClassVarDec(tokens []string, outputFile *os.File) int {
	fmt.Fprintln(outputFile, "<classVarDec>")
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		if _type == TokenTypeKeyword {
			compileKeyword(token, outputFile)
		}
		if _type == TokenTypeIdentifier {
			compileIdentifier(token, outputFile)
		}
		if _type == TokenTypeSymbol {
			compileSymbol(token, outputFile)
			//end class var declaration
			if token == ";" {
				fmt.Fprintln(outputFile, "</classVarDec>")
				return i
			}
		}
	}
	return 0
}

//
//  compileSubroutine
//  @Description: Compile the whole method, function or constructor
//  @param tokens
//  @param outputFile
//  @return int
//
func compileSubroutine(tokens []string, outputFile *os.File) int {
	fmt.Fprintln(outputFile, "<subroutineDec>")
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		if _type == TokenTypeKeyword {
			compileKeyword(token, outputFile)
		}
		if _type == TokenTypeIdentifier {
			compileIdentifier(token, outputFile)
		}
		if _type == TokenTypeSymbol {
			//start subroutine body
			if token == "{" {
				fmt.Fprintln(outputFile, "<subroutineBody>")
				var tokenii int
				subroutineTokens := tokens[i:]
				for ii, token := range subroutineTokens {
					if tokenii != 0 && ii <= tokenii {
						continue
					}
					_type_ := TokenType(token)
					//end subroutine body
					if _type_ == TokenTypeSymbol && token == "}" {
						compileSymbol(token, outputFile)
						fmt.Fprintln(outputFile, "</subroutineBody>")
						fmt.Fprintln(outputFile, "</subroutineDec>")
						return i + ii
					}
					if _type_ == TokenTypeSymbol {
						compileSymbol(token, outputFile)
					}
					if _type_ == TokenTypeKeyword {
						if token == KeywordVar {
							tokenii = ii + compileVarDec(subroutineTokens[ii:], outputFile)
							continue
						}
						if token == KeywordLet || token == KeywordIf ||
							token == KeywordWhile || token == KeywordReturn ||
							token == KeywordDo {
							tokenii = ii + compileStatements(subroutineTokens[ii:], outputFile)
							continue
						}
					}
				}
			}
			compileSymbol(token, outputFile)
			//start parameter list
			if token == "(" {
				tokeni = i + compileParameterList(tokens[i+1:], outputFile)
				continue
			}
		}
	}
	return 0
}

//
//  compileParameterList
//  @Description: Compile the parameter list, exclude "(" and ")"
//  @param tokens
//  @param outputFile
//  @return int
//
func compileParameterList(tokens []string, outputFile *os.File) int {
	//if tokens[0] == ")" {
	//	fmt.Fprint(outputFile, "<parameterList>")
	//	fmt.Fprintln(outputFile, " </parameterList>")
	//	return -1
	//}
	fmt.Fprintln(outputFile, "<parameterList>")
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		if _type == TokenTypeKeyword {
			compileKeyword(token, outputFile)
		}
		if _type == TokenTypeIdentifier {
			compileIdentifier(token, outputFile)
		}
		if _type == TokenTypeSymbol {
			//end parameter list
			if token == ")" {
				fmt.Fprintln(outputFile, "</parameterList>")
				return i
			}
			compileSymbol(token, outputFile)
		}
	}
	return 0
}

//
//  compileVarDec
//  @Description: Compile var declaration
//  @param tokens
//  @param outputFile
//  @return int
//
func compileVarDec(tokens []string, outputFile *os.File) int {
	fmt.Fprintln(outputFile, "<varDec>")
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		if _type == TokenTypeKeyword {
			compileKeyword(token, outputFile)
		}
		if _type == TokenTypeIdentifier {
			compileIdentifier(token, outputFile)
		}
		if _type == TokenTypeSymbol {
			compileSymbol(token, outputFile)
			//end var declaration
			if token == ";" {
				fmt.Fprintln(outputFile, "</varDec>")
				return i
			}
		}
	}
	return 0
}

//
//  compileStatements
//  @Description: Compile a series of statements, exclude "{" and "}"
//  @param tokens
//  @param outputFile
//  @return int
//
func compileStatements(tokens []string, outputFile *os.File) int {
	fmt.Fprintln(outputFile, "<statements>")
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		if _type == TokenTypeKeyword {
			//start let statement
			if token == KeywordLet {
				tokeni = i + compileLet(tokens[i:], outputFile)
				continue
			}
			//start do statement
			if token == KeywordDo {
				tokeni = i + compileDo(tokens[i:], outputFile)
				continue
			}
			//start while statement
			if token == KeywordWhile {
				tokeni = i + compileWhile(tokens[i:], outputFile)
				continue
			}
			//start if statement
			if token == KeywordIf {
				tokeni = i + compileIf(tokens[i:], outputFile)
				continue
			}
			//start return statement
			if token == KeywordReturn {
				tokeni = i + compileReturn(tokens[i:], outputFile)
				continue
			}
		}
		if _type == TokenTypeSymbol {
			//end statements
			if token == "}" {
				fmt.Fprintln(outputFile, "</statements>")
				return i - 1
			}
		}
	}

	return 0
}

//
//  compileDo
//  @Description: Compile do statement
//  @param tokens
//  @param outputFile
//  @return int
//
func compileDo(tokens []string, outputFile *os.File) int {
	fmt.Fprintln(outputFile, "<doStatement>")
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		if _type == TokenTypeKeyword {
			compileKeyword(token, outputFile)
		}
		if _type == TokenTypeIdentifier {
			compileIdentifier(token, outputFile)
		}
		if _type == TokenTypeSymbol {
			compileSymbol(token, outputFile)
			//start expression list
			if token == "(" {
				tokeni = i + 1 + compileExpressionList(tokens[i+1:], outputFile)
				continue
			}
			//end do statement
			if token == ";" {
				fmt.Fprintln(outputFile, "</doStatement>")
				return i
			}
		}
	}
	return 0
}

//
//  compileLet
//  @Description: Compile let statement
//  @param tokens
//  @param outputFile
//  @return int
//
func compileLet(tokens []string, outputFile *os.File) int {
	fmt.Fprintln(outputFile, "<letStatement>")
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		if _type == TokenTypeKeyword {
			compileKeyword(token, outputFile)
		}
		if _type == TokenTypeIdentifier {
			compileIdentifier(token, outputFile)
		}
		if _type == TokenTypeSymbol {
			compileSymbol(token, outputFile)
			//start expression
			if token == "=" {
				tokeni = i + 1 + compileExpression(tokens[i+1:], outputFile)
				continue
			}
			//start expression
			if token == "[" {
				tokeni = i + 1 + compileExpression(tokens[i+1:], outputFile)
				continue
			}
			//end let statement
			if token == ";" {
				fmt.Fprintln(outputFile, "</letStatement>")
				return i
			}
		}
	}
	return 0
}

//
//  compileWhile
//  @Description: Compile while statement
//  @param tokens
//  @param outputFile
//  @return int
//
func compileWhile(tokens []string, outputFile *os.File) int {
	fmt.Fprintln(outputFile, "<whileStatement>")
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		if _type == TokenTypeKeyword {
			compileKeyword(token, outputFile)
		}
		if _type == TokenTypeSymbol {
			compileSymbol(token, outputFile)
			//start expression
			if token == "(" {
				tokeni = i + 1 + compileExpression(tokens[i+1:], outputFile)
				continue
			}
			//start statements
			if token == "{" {
				tokeni = i + 1 + compileStatements(tokens[i+1:], outputFile)
				continue
			}
			//end while statement
			if token == "}" {
				fmt.Fprintln(outputFile, "</whileStatement>")
				return i
			}
		}
	}
	return 0
}

//
//  compileReturn
//  @Description: Compile return statement
//  @param tokens
//  @param outputFile
//  @return int
//
func compileReturn(tokens []string, outputFile *os.File) int {
	fmt.Fprintln(outputFile, "<returnStatement>")
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		if _type == TokenTypeKeyword && token == KeywordReturn {
			compileKeyword(token, outputFile)
			//end return statement
		} else if _type == TokenTypeSymbol && token == ";" {
			compileSymbol(token, outputFile)
			fmt.Fprintln(outputFile, "</returnStatement>")
			return i
			//start expression
		} else {
			tokeni = i + compileExpression(tokens[i:], outputFile)
			continue
		}

	}
	return 0
}

//
//  compileIf
//  @Description: Compile if statement
//  @param tokens
//  @param outputFile
//  @return int
//
func compileIf(tokens []string, outputFile *os.File) int {
	fmt.Fprintln(outputFile, "<ifStatement>")
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		if _type == TokenTypeKeyword {
			compileKeyword(token, outputFile)
		}
		if _type == TokenTypeSymbol {
			compileSymbol(token, outputFile)
			//start expression
			if token == "(" {
				tokeni = i + 1 + compileExpression(tokens[i+1:], outputFile)
				continue
			}
			//start statements
			if token == "{" {
				tokeni = i + 1 + compileStatements(tokens[i+1:], outputFile)
				continue
			}
			if token == "}" {
				nextToken := tokens[i+1]
				//start else
				if nextToken == KeywordElse {
					continue
				}
				//end if statement
				fmt.Fprintln(outputFile, "</ifStatement>")
				return i
			}
		}
	}
	return 0
}

//
//  compileExpression
//  @Description: Compile an expression
//  @param tokens
//  @param outputFile
//  @return int
//
func compileExpression(tokens []string, outputFile *os.File) int {
	fmt.Fprintln(outputFile, "<expression>")
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		//start a term
		if _type == TokenTypeIntConst {
			tokeni = i + compileTerm(tokens[i:], outputFile)
			continue
		}
		if _type == TokenTypeStringConst {
			tokeni = i + compileTerm(tokens[i:], outputFile)
			continue
		}
		if _type == TokenTypeKeyword {
			tokeni = i + compileTerm(tokens[i:], outputFile)
			continue
		}
		if _type == TokenTypeIdentifier {
			tokeni = i + compileTerm(tokens[i:], outputFile)
			continue
		}
		if _type == TokenTypeSymbol {
			//end expression
			if token == ")" || token == "]" || token == ";" || token == "," {
				fmt.Fprintln(outputFile, "</expression>")
				return i - 1
			}
			if token == "-" {
				//"-" op at start, start a term
				if i == 0 {
					tokeni = i + compileTerm(tokens[i:], outputFile)
					continue
					//"-" op is at start of a term of current expression
				} else if TokenType(tokens[i-1]) == TokenTypeSymbol &&
					isOp(tokens[i-1]) {
					tokeni = i + compileTerm(tokens[i:], outputFile)
					//"-" op is the op of current expression
				} else {
					compileSymbol(token, outputFile)
					continue
				}
			}
			//start a term
			if token == "(" {
				tokeni = i + compileTerm(tokens[i:], outputFile)
				continue
			}
			compileSymbol(token, outputFile)
		}
	}
	return 0
}

//
//  compileTerm
//  @Description: Compile a term
//  @param tokens
//  @param outputFile
//  @return int
//
func compileTerm(tokens []string, outputFile *os.File) int {
	fmt.Fprintln(outputFile, "<term>")
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		//the term is an integer constant
		if _type == TokenTypeIntConst {
			fmt.Fprint(outputFile, "<integerConstant> ")
			fmt.Fprint(outputFile, token)
			fmt.Fprintln(outputFile, " </integerConstant>")
			fmt.Fprintln(outputFile, "</term>")
			return i
		}
		//the term is a string constant
		if _type == TokenTypeStringConst {
			fmt.Fprint(outputFile, "<stringConstant> ")
			token = strings.Trim(token, "\"")
			fmt.Fprint(outputFile, token)
			fmt.Fprintln(outputFile, " </stringConstant>")
			fmt.Fprintln(outputFile, "</term>")
			return i
		}
		//the term is "true", "false", "null", "this"
		if _type == TokenTypeKeyword {
			compileKeyword(token, outputFile)
			fmt.Fprintln(outputFile, "</term>")
			return i
		}
		if _type == TokenTypeIdentifier {
			compileIdentifier(token, outputFile)
			nextToken := tokens[i+1]
			//the term contains array visit or function call
			if nextToken == "[" || nextToken == "(" || nextToken == "." {
				continue
			}
			//end term
			fmt.Fprintln(outputFile, "</term>")
			return i
		}
		if _type == TokenTypeSymbol {
			compileSymbol(token, outputFile)
			//start the expression in the array visit
			if token == "[" {
				tokeni = i + 1 + compileExpression(tokens[i+1:], outputFile)
				continue
			}
			//the term contains sub term
			if token == "(" && i == 0 {
				tokeni = i + 1 + compileExpression(tokens[i+1:], outputFile)
				continue
			}
			//start the expression list of function call
			if token == "(" && i > 0 {
				tokeni = i + 1 + compileExpressionList(tokens[i+1:], outputFile)
				continue
			}
			//end term
			if token == "]" || token == ")" {
				fmt.Fprintln(outputFile, "</term>")
				return i
			}
			//unaryOp term
			if token == "-" || token == "~" {
				res := compileTerm(tokens[i+1:], outputFile)
				fmt.Fprintln(outputFile, "</term>")
				return i + 1 + res
			}
		}
	}
	return 0
}

//
//  compileExpressionList
//  @Description: Compile expression list
//  @param tokens
//  @param outputFile
//  @return int
//
func compileExpressionList(tokens []string, outputFile *os.File) int {
	fmt.Fprintln(outputFile, "<expressionList>")
	//the expression list is blank
	if tokens[0] == ")" {
		fmt.Fprintln(outputFile, " </expressionList>")
		return -1
	}
	var tokeni int
	for i, token := range tokens {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		_type := TokenType(token)
		if _type == TokenTypeSymbol {
			//has next expression
			if token == "," {
				compileSymbol(token, outputFile)
				continue
			}
			//end expression list
			if token == ")" {
				fmt.Fprintln(outputFile, " </expressionList>")
				return i - 1
			}
		}
		//start expression
		tokeni = i + compileExpression(tokens[i:], outputFile)
		continue
	}
	return 0
}

//
//  compileKeyword
//  @Description: Compile a keyword to xml tag
//  @param token
//  @param outputFile
//
func compileKeyword(token string, outputFile *os.File) {
	fmt.Fprint(outputFile, "<keyword> ")
	fmt.Fprint(outputFile, token)
	fmt.Fprintln(outputFile, " </keyword>")
}

//
//  compileIdentifier
//  @Description: Compile an identifier to xml tag
//  @param token
//  @param outputFile
//
func compileIdentifier(token string, outputFile *os.File) {
	fmt.Fprint(outputFile, "<identifier> ")
	fmt.Fprint(outputFile, token)
	fmt.Fprintln(outputFile, " </identifier>")
}

//
//  compileSymbol
//  @Description: Compile a symbol to xml tag
//  @param token
//  @param outputFile
//
func compileSymbol(token string, outputFile *os.File) {
	fmt.Fprint(outputFile, "<symbol> ")
	if token == "<" {
		fmt.Fprint(outputFile, "&lt;")
	} else if token == ">" {
		fmt.Fprint(outputFile, "&gt;")
	} else if token == "&" {
		fmt.Fprint(outputFile, "&amp;")
	} else {
		fmt.Fprint(outputFile, token)
	}
	fmt.Fprintln(outputFile, " </symbol>")
}

//
//  isOp
//  @Description: Check the token is operator ?
//  @param token
//  @return bool
//
func isOp(token string) bool {
	if regexp.MustCompile(`[+\-*/&|<>=]`).MatchString(token) {
		return true
	}
	return false
}
