/**
  @Author: HuangChuan
  @Date: 2022/1/30 14:19
  @Description:// Main function, get the input file and create the output file
**/
package main

import (
	"fmt"
	"os"
	"path/filepath"
	"strings"
)

func main() {
	input := `D:\GoogleBrowserDownload\nand2tetris\projects\10\ArrayTest\Main.jack`
	inputFile, _ := os.Open(input)
	path, name := filepath.Split(input)
	output := fmt.Sprintf("%s%sX.xml", path, name[:strings.Index(name, ".")])
	outputFile, _ := os.Create(output)
	Tokenize(inputFile, outputFile)
}
