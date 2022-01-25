/**
  @Author: HuangChuan
  @Date: 2021/12/27 16:25
  @Description:// The VM translator main function
**/
package main

import (
	"fmt"
	"nand2tetris/08/codeWriter"
	"nand2tetris/08/parser"
	"os"
	"path/filepath"
	"strings"
)

func main() {
	inputFile := "D:\\GoogleBrowserDownload\\nand2tetris\\projects\\08\\FunctionCalls\\FibonacciElement\\Main.vm"
	input, _ := os.Open(inputFile)
	defer input.Close()
	outputPath, name := filepath.Split(inputFile)
	outputFile := fmt.Sprintf("%s%s.asm", outputPath, name[:strings.Index(name, ".")])
	fmt.Println(outputFile)
	filename, _ := os.Create(outputFile)
	defer filename.Close()
	p := parser.NewParser(input)
	cw := codeWriter.NewCodeWriter(filename)
	for p.HasMoreCommands() {
		p.Advance()
		switch p.GetCommandType() {
		case parser.C_ARITHMETIC:
			cw.WriteArithmetic(p.Args1())
		case parser.C_POP, parser.C_PUSH:
			index, _ := p.Args2()
			cw.WritePushPop(p.GetCommandType(), p.Args1(), index)
		case parser.C_LABEL:
			cw.WriteLabel(p.Args1())
		case parser.C_IF:
			cw.WriteIf(p.Args1())
		case parser.C_GOTO:
			cw.WriteGoto(p.Args1())
		case parser.C_FUNCTION:
			num, _ := p.Args2()
			cw.WriteFunction(p.Args1(), num)
		case parser.C_RETURN:
			cw.WriteReturn()
		case parser.C_CALL:
			num, _ := p.Args2()
			cw.WriteCall(p.Args1(), num)
		}
	}
}
