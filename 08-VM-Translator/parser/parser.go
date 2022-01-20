/**
  @Author: HuangChuan
  @Date: 2021/12/26 22:10
  @Description:// The parser module, parses the source file
**/
package parser

import (
	"bufio"
	"io"
	"strconv"
	"strings"
)

type CommandType int

const (
	_ CommandType = iota
	C_ARITHMETIC
	C_PUSH
	C_POP
	C_LABEL
	C_GOTO
	C_IF
	C_FUNCTION
	C_RETURN
	C_CALL
	C_NONE
)

type Parser struct {
	scanner        *bufio.Scanner
	currentCommand string
}

//
//  NewParser
//  @Description: Constructor
//  @param r
//  @return *Parser
//
func NewParser(r io.Reader) *Parser {
	scanner := bufio.NewScanner(r)
	return &Parser{scanner: scanner}
}

//
//  HasMoreCommands
//  @Description: if the file has more commands?
//  @receiver p
//  @return bool
//
func (p *Parser) HasMoreCommands() bool {
	return p.scanner.Scan()
}

//
//  Advance
//  @Description: read the next line, and process the current line to get a valid command
//  @receiver p
//
func (p *Parser) Advance() {
	line := p.scanner.Text()
	// if the current line is null or comment, return ""
	if line == "" || strings.HasPrefix(line, "//") {
		p.currentCommand = ""
		return
	}
	// remove the comment of current line
	if strings.Contains(line, "//") {
		line = line[:strings.Index(line, "//")]
	}
	// remove the spaces of current line
	line = strings.TrimSpace(line)
	p.currentCommand = line
}

//
//  GetCommandType
//  @Description: get the command type of current command
//  @receiver p
//  @return CommandType
//
func (p *Parser) GetCommandType() CommandType {
	slice := strings.Split(p.currentCommand, " ")
	switch slice[0] {
	case "add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not":
		return C_ARITHMETIC
	case "push":
		return C_PUSH
	case "pop":
		return C_POP
	case "label":
		return C_LABEL
	case "goto":
		return C_GOTO
	case "if-goto":
		return C_IF
	case "function":
		return C_FUNCTION
	case "return":
		return C_RETURN
	case "call":
		return C_CALL
	default:
		return C_NONE // invalid vm command
	}
}

//
//  Args1
//  @Description: get the first argument of current command
//  @receiver p
//  @return string
//
func (p *Parser) Args1() string {
	slice := strings.Split(p.currentCommand, " ")
	switch slice[0] {
	case "add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not":
		return slice[0]
	default:
		return slice[1]
	}
}

//
//  Args2
//  @Description: get the second argument of current command
//  @receiver p
//  @return int
//  @return error
//
func (p *Parser) Args2() (int, error) {
	slice := strings.Split(p.currentCommand, " ")
	args2, err := strconv.Atoi(slice[2])
	if err != nil {
		return 0, err
	}
	return args2, nil
}
