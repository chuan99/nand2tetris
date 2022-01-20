/**
  @Author: HuangChuan
  @Date: 2021/12/27 15:32
  @Description:// Translate the VM command to assemble code
**/
package codeWriter

import (
	"bufio"
	"fmt"
	"io"
	"nand2tetris/08/parser"
)

type CodeWriter struct {
	writer   io.Writer
	addr     int
	callcnt  int
	filename string
}

//
//  NewCodeWriter
//  @Description: Constructor
//  @param w
//  @return *CodeWriter
//
func NewCodeWriter(w io.Writer) *CodeWriter {
	cw := &CodeWriter{
		writer: w,
	}
	cw.writeInit()
	return cw
}

//
//  SetFileName
//  @Description: set the filename of output file
//  @receiver cw
//  @param filename
//
func (cw *CodeWriter) SetFileName(filename string) {
	cw.filename = filename
}

//
//  WriteArithmetic
//  @Description: write assemble code of arithmetic commands
//  @receiver cw
//  @param cmd
//
func (cw *CodeWriter) WriteArithmetic(cmd string) {
	switch cmd {
	case "add":
		cw.writeAddSubAndOr("M=D+M")
	case "sub":
		cw.writeAddSubAndOr("M=M-D")
	case "and":
		cw.writeAddSubAndOr("M=D&M")
	case "or":
		cw.writeAddSubAndOr("M=D|M")
	case "neg":
		cw.writeNegNot("M=-M")
	case "not":
		cw.writeNegNot("M=!M")
	case "eq":
		cw.writeEqGtLt("JEQ")
	case "gt":
		cw.writeEqGtLt("JGT")
	case "lt":
		cw.writeEqGtLt("JLT")
	}
}

//
//  WritePushPop
//  @Description: write assemble code of push and pop commands
//  @receiver cw
//  @param cmd
//  @param segment
//  @param index
//
func (cw *CodeWriter) WritePushPop(cmd parser.CommandType, segment string, index int) {
	switch cmd {
	case parser.C_PUSH:
		switch segment {
		case "argument":
			cw.writePushSymbol("ARG", index)
		case "local":
			cw.writePushSymbol("LCL", index)
		case "this":
			cw.writePushSymbol("THIS", index)
		case "that":
			cw.writePushSymbol("THAT", index)
		case "constant":
			cw.writePushConstant(index)
		case "pointer":
			cw.writePushRegister(index + 3)
		case "temp":
			cw.writePushRegister(index + 5)
		case "static":
			cw.writePushStatic(index)
		}
	case parser.C_POP:
		switch segment {
		case "argument":
			cw.writePopSymbol("ARG", index)
		case "local":
			cw.writePopSymbol("LCL", index)
		case "this":
			cw.writePopSymbol("THIS", index)
		case "that":
			cw.writePopSymbol("THAT", index)
		case "pointer":
			cw.writePopRegister(index + 3)
		case "temp":
			cw.writePopRegister(index + 5)
		case "static":
			cw.writePopStatic(index)
		}
	}

}

//
//  WriteLabel
//  @Description: write assemble code of label command
//  @receiver cw
//  @param label
//
func (cw *CodeWriter) WriteLabel(label string) {
	symbol := fmt.Sprintf("%s$%s", cw.filename, label)
	asm := `
(%s)
`
	asm = fmt.Sprintf(asm, symbol)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}

//
//  WriteIf
//  @Description: write assemble code of if-goto
//  @receiver cw
//  @param label
//
func (cw *CodeWriter) WriteIf(label string) {
	symbol := fmt.Sprintf("%s$%s", cw.filename, label)
	asm := `
@SP
M=M-1
@SP
A=M
D=M
@%s
D;JNE
`
	asm = fmt.Sprintf(asm, symbol)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}

//
//  WriteGoto
//  @Description: write assemble code of goto command
//  @receiver cw
//  @param label
//
func (cw *CodeWriter) WriteGoto(label string) {
	symbol := fmt.Sprintf("%s$%s", cw.filename, label)
	asm := `
@%s
0;JMP
`
	asm = fmt.Sprintf(asm, symbol)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}

//
//  WriteReturn
//  @Description: write assemble code of return command
//  @receiver cw
//
func (cw *CodeWriter) WriteReturn() {
	asm := `
@LCL
D=M
@R13
M=D
@5
D=A
@R13
A=M-D
D=M
@R14
M=D
@SP
M=M-1
@SP
A=M
D=M
@ARG
A=M
M=D
@ARG
D=M+1
@SP
M=D
@1
D=A
@R13
A=M-D
D=M
@THAT
M=D
@2
D=A
@R13
A=M-D
D=M
@THIS
M=D
@3
D=A
@R13
A=M-D
D=M
@ARG
M=D
@4
D=A
@R13
A=M-D
D=M
@LCL
M=D
@R14
A=M
0;JMP
`
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}

//
//  WriteFunction
//  @Description: write assemble code of function
//  @receiver cw
//  @param functionName
//  @param numArgs
//
func (cw *CodeWriter) WriteFunction(functionName string, numArgs int) {
	asm := `
(%s)
`
	asm = fmt.Sprintf(asm, functionName)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
	// init local variable to 0
	for i := 0; i < numArgs; i++ {
		cw.writePushConstant(0)
	}
}

//
//  WriteCall
//  @Description: write assemble code of call command
//  @receiver cw
//  @param functionName
//  @param numArgs
//
func (cw *CodeWriter) WriteCall(functionName string, numArgs int) {
	rAddr := fmt.Sprintf("%s$%d", functionName, cw.callcnt)
	asm := `
@%s
D=A
@SP
A=M
M=D
@SP
M=M+1
`
	asm = fmt.Sprintf(asm, rAddr)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
	// push LCL, ARG, THIS, THAT
	cw.writePushRegisterByName("LCL")
	cw.writePushRegisterByName("ARG")
	cw.writePushRegisterByName("THIS")
	cw.writePushRegisterByName("THAT")
	asm2 := `
@SP
D=M
@%d
D=D-A
@5
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@%s
0;JMP
(%s)
`
	asm2 = fmt.Sprintf(asm2, numArgs, functionName, rAddr)
	w = bufio.NewWriter(cw.writer)
	w.WriteString(asm2)
	w.Flush()
	cw.callcnt++
}

//
//  writeInit
//  @Description: init code, init the stack pointer
//  @receiver cw
//
func (cw *CodeWriter) writeInit() {
	asm := `
@256
D=A
@SP
M=D
`
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
	cw.WriteCall("Sys.init", 0)
}

//
//  writeAddSub
//  @Description: write assemble code of add, sub, and, or command
//  @receiver cw
//  @param op
//
func (cw *CodeWriter) writeAddSubAndOr(op string) {
	/*
		1. pop to M register, decrease stack pointer by one.
			- @SP
			- M=M-1
		2. push to D register from M register, get the second num.
			- A=M
			- D=M
		3. pop to M register, decrease stack pointer by one.
			- @SP
			- M=M-1
		4. Add M register and D register, push to M register, get the first num
			- A=M
			- M=D+M
		5. increase stack pointer by one.（Initialize stack pointer）
			- @SP
			- M=M+1
	*/
	asm := `
@SP
M=M-1
A=M
D=M
@SP
M=M-1
A=M
%s
@SP
M=M+1
`
	asm = fmt.Sprintf(asm, op)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}

//
//  writeNegNot
//  @Description: write assemble code of neg, not command.
//  @receiver cw
//  @param op
//
func (cw *CodeWriter) writeNegNot(op string) {
	/*
		1. pop to M register, decrease stack pointer by one.
			- @SP
			- M=M-1
		2. Make it a negative number at M register.
			- A=M
			- M=-M
		3. increase stack pointer by one.（Initialize stack pointer）
			- @SP
			- M=M+1
	*/
	asm := `
@SP
M=M-1
A=M
%s
@SP
M=M+1
`
	asm = fmt.Sprintf(asm, op)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}

//
//  writeEqGtLt
//  @Description: write assemble code of eq, gt, lt command
//  @receiver cw
//  @param op
//
func (cw *CodeWriter) writeEqGtLt(op string) {
	/*
		1. pop to M register, decrease stack pointer by one.
			- @SP
			- M=M-1
		2. push to D register from M register.
			- A=M
			- D=M
		3. pop to M register, decrease stack pointer by one.
			- @SP
			- M=M-1
		4. subtract M register from D register, push to D register.
			- A=M
			- D=M-D
		5. Set TRUE to the memory pointed to by the stack pointer
			- @SP
			- A=M
			- M=-1 //0xFFFF
		6. if D register is 0, jump to LABEL1
			- @LABEL1
			- D;JEQ
		7. Set FALSE to the memory pointed to by the stack pointer
			- @SP
			- A=M
			- M=0 //0x0000
		8. Jump destination label
			- (LABEL1)
		9. increase stack pointer by one.（Initialize stack pointer）
			- @SP
			- M=M+1
	*/
	asm := `
@SP
M=M-1
A=M
D=M
@SP
M=M-1
A=M
D=M-D
@SP
A=M
M=-1
@LABEL%d
D;%s
@SP
A=M
M=0
(LABEL%d)
@SP
M=M+1
`
	asm = fmt.Sprintf(asm, cw.addr, op, cw.addr)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}

//
//  writePushSymbol
//  @Description: write assemble code of push symbol command
//  @receiver cw
//  @param symbol
//  @param index
//
func (cw *CodeWriter) writePushSymbol(symbol string, index int) {
	/*
		1. put 0 in D register
			- @0
			- D=A
		2. Add the stack area pointed to by LCL(local) and the D register
			- @LCL
			- D=D+M
		3. Save D register in the stack area pointed to by R13(temp)
			- @R13
			- M=D
		4. Set the stack area pointed to by R13 in the A register
			- A=M
			- D=M
		5. Save D register in the stack area pointed to by stack pointer
			- @SP
			- A=M
			- M=D
		6. increase stack pointer by one.（Initialize stack pointer）
			- @SP
			- M=M+1
	*/
	asm := `
@%d
D=A
@%s
D=D+M
@R13
M=D
A=M
D=M
@SP
A=M
M=D
@SP
M=M+1
`
	asm = fmt.Sprintf(asm, index, symbol)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}

//
//  writePushRegister
//  @Description: write the assemble code of push register value into stack pointer
//  @receiver cw
//  @param r
//
func (cw *CodeWriter) writePushRegister(r int) {
	asm := `
@R%d
D=M
@SP
A=M
M=D
@SP
M=M+1
`
	asm = fmt.Sprintf(asm, r)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()

}

//
//  writePushRegisterByName
//  @Description: write assemble code of push register value into stack pointer
//  @receiver cw
//  @param register
//
func (cw *CodeWriter) writePushRegisterByName(register string) {
	asm := `
@%s
D=M
@SP
A=M
M=D
@SP
M=M+1
`
	asm = fmt.Sprintf(asm, register)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}

//
//  writePushConstant
//  @Description: write assemble code of push constant value into stack pointer
//  @receiver cw
//  @param index
//
func (cw *CodeWriter) writePushConstant(index int) {
	/*
		1. put 0 in D register
			- @0
			- D=A
		2. pop to M register from D register. M register is top +1 element in stack
			- @SP
			- M=A //Set empty value, using stack pointer
			- M=D
		3. increase stack pointer by one.（Initialize stack pointer）
			- @SP
			- M=M+1
	*/
	asm := `
@%d
D=A
@SP
A=M
M=D
@SP
M=M+1
`
	asm = fmt.Sprintf(asm, index)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}

//
//  writePushStatic
//  @Description: write assemble code of push static value into stack pointer
//  @receiver cw
//  @param index
//
func (cw *CodeWriter) writePushStatic(index int) {
	/*
		1. Put Static value in D register.
			- @StaticTest.1
			- D=M
		2. Add D register in the stack area pointed to by stack pointer
			- @SP
			- A=M
			- M=D
		3. increase stack pointer by one.（Initialize stack pointer）
			- @SP
			- M=M+1
	*/
	// set the name of static value
	static := fmt.Sprintf("%s.%d", cw.filename, index)
	asm := `
@%s
D=M
@SP
A=M
M=D
@SP
M=M+1
`
	asm = fmt.Sprintf(asm, static)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}

//
//  writePopSymbol
//  @Description: pop the data at the top of stack and store in segment[index]
//  @receiver cw
//  @param symbol
//  @param index
//
func (cw *CodeWriter) writePopSymbol(symbol string, index int) {
	/*
		1. pop to M register, decrease stack pointer by one.
			- @SP
			- M=M-1
		2. put 0 in D register
			- @0
			- D=A
		3. Add the stack area pointed to by LCL(local) and the D register
			- @LCL
			- D=D+M
		4. save the pointer value in R13 register
			- @R13
			- M=D
		5. put the data at the top of the stack in D register
			- @SP
			- A=M
			- D=M
		6. Add D register in the stack area pointed to by R13(temp)
			- @R13
			- A=M
			- M=D
	*/
	asm := `
@SP
M=M-1
@%d
D=A
@%s
D=D+M
@R13
M=D
@SP
A=M
D=M
@R13
A=M
M=D
`
	asm = fmt.Sprintf(asm, index, symbol)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}

//
//  writePopRegister
//  @Description: write assemble code of pop the data at the top of stack and store in register
//  @receiver cw
//  @param index
//
func (cw *CodeWriter) writePopRegister(index int) {
	/*
		1. pop to M register, decrease stack pointer by one.
			- @SP
			- M=M-1
		2. put the data at the top of the stack in D register
			- @SP
			- A=M
			- D=M
		3. Add D register in the stack area pointed to by R11. R11 is 5 + index(=6).
			- @R11
			- M=D
	*/
	asm := `
@SP
M=M-1
@SP
A=M
D=M
@R%d
M=D
`
	asm = fmt.Sprintf(asm, index)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}

//
//  writePopStatic
//  @Description: write assemble code of pop the data at the top of stack and store in static value
//  @receiver cw
//  @param index
//
func (cw *CodeWriter) writePopStatic(index int) {
	// set the name of static value
	static := fmt.Sprintf("%s.%d", cw.filename, index)
	/*
		1. pop to M register, decrease stack pointer by one.
			- @SP
			- M=M-1
		2. put the data at the top of the stack in D register
			- @SP
			- A=M
			- D=M
		3. Add D register in the stack area pointed to by static.
			- @StaticTest.0
			- M=D
	*/
	asm := `
@SP
M=M-1
@SP
A=M
D=M
@%s
M=D
`
	asm = fmt.Sprintf(asm, static)
	w := bufio.NewWriter(cw.writer)
	w.WriteString(asm)
	w.Flush()
}
