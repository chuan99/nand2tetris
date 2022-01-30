/**
  @Author: HuangChuan
  @Date: 2022/1/20 14:07
  @Description:// The Jack tokenizer, remove the comments and spaces of the input stream, and transform
the input stream into Jack tokens
**/
package main

import (
	"bufio"
	"os"
	"regexp"
	"strings"
)

//token type
const (
	TokenTypeKeyword     = "KEYWORD"
	TokenTypeSymbol      = "SYMBOL"
	TokenTypeIntConst    = "INT_CONST"
	TokenTypeStringConst = "STRING_CONST"
	TokenTypeIdentifier  = "IDENTIFIER"
)

//keywords
const (
	KeywordClass       = "class"
	KeywordConstructor = "constructor"
	KeywordFunction    = "function"
	KeywordMethod      = "method"
	KeywordField       = "field"
	KeywordStatic      = "static"
	KeywordVar         = "var"
	KeywordInt         = "int"
	KeywordChar        = "char"
	KeywordBoolean     = "boolean"
	KeywordVoid        = "void"
	KeywordTrue        = "true"
	KeywordFalse       = "false"
	KeywordNull        = "null"
	KeywordThis        = "this"
	KeywordLet         = "let"
	KeywordDo          = "do"
	KeywordIf          = "if"
	KeywordElse        = "else"
	KeywordWhile       = "while"
	KeywordReturn      = "return"
)

//
//  hasMoreTokens
//  @Description: Check the input stream has more tokens ?
//  @param scanner
//  @return bool
//
func hasMoreTokens(scanner *bufio.Scanner) bool {
	return scanner.Scan()
}

//
//  advance
//  @Description: Read the next line of input stream, and get the tokens of current line
//  @param curText
//  @param tokens
//  @return []string
//
func advance(curText string, tokens []string) []string {
	//remove space
	s := strings.TrimSpace(curText)
	//remove comments
	if strings.Contains(s, "//") {
		s = s[0:strings.Index(s, "//")]
	}
	var token string
	var tokeni int
	reg := regexp.MustCompile(`[{}\[\]().,;+\-*/&|<>=~]`)
	for i, c := range s {
		if tokeni != 0 && i <= tokeni {
			continue
		}
		strChar := string(c)
		//when traverse a char which can be matched by reg, add a token into tokens
		if reg.MatchString(strChar) {
			if token != "" {
				tokens = append(tokens, token)
				token = ""
			}
			tokens = append(tokens, strChar)
			continue
		}
		//when traverse a ", add a string into tokens
		if strChar == "\"" {
			tokens = append(tokens, s[i:strings.LastIndex(s, "\"")+1])
			token = ""
			tokeni = strings.LastIndex(s, "\"")
			continue
		}
		//when traverse a space, add a token into tokens
		if strChar == " " {
			if token != "" {
				trimToken := strings.TrimSpace(token)
				tokens = append(tokens, strings.TrimSpace(trimToken))
				token = ""
			}
			continue
		}
		token += string(c)
	}
	return tokens
}

//
//  TokenType
//  @Description: Get the type of current token
//  @param token
//  @return string
//
func TokenType(token string) string {
	//keywordList := []string{"class", "constructor", "function", "method", "field", "static", "var",
	//	"int", "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else",
	//	"while", "return"}
	//for _, v := range keywordList {
	//	if token == v {
	//		return TokenTypeKeyword
	//	}
	//}
	if regexp.MustCompile(`(?m)(^class$|^constructor$|^function$|^method$|^field$|^static$|^var$|^int$|^char$|^boolean$|^void$|^true$|^false$|^null$|^this$|^let$|^do$|^if$|^else$|^while$|^return$)`).MatchString(token) {
		return TokenTypeKeyword
	}
	if regexp.MustCompile(`[{}\[\]().,;+\-*/&|<>=~]`).MatchString(token) {
		return TokenTypeSymbol
	}
	if regexp.MustCompile(`^[0-9]+$`).MatchString(token) {
		return TokenTypeIntConst
	}
	if regexp.MustCompile(`"`).MatchString(token) {
		return TokenTypeStringConst
	}
	return TokenTypeIdentifier
}

//
//  Tokenize
//  @Description: The process of transforming input stream into tokens
//  @param inputFile
//  @param outputFile
//
func Tokenize(inputFile *os.File, outputFile *os.File) {
	scanner := bufio.NewScanner(inputFile)
	var tokens []string
	for hasMoreTokens(scanner) {
		curText := scanner.Text()
		//skip comment
		if strings.Contains(curText, "//") && strings.Index(curText, "//") == 0 {
			continue
		}
		if strings.Contains(curText, "/*") {
			continue
		}
		if strings.Contains(curText, "*") && strings.Index(curText, "*") == 1 {
			continue
		}
		//skip blank
		if !regexp.MustCompile(`.`).MatchString(curText) {
			continue
		}
		tokens = advance(curText, tokens)
	}
	CompileClass(tokens, outputFile)
}
