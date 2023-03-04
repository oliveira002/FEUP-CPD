package main

import (
    "fmt"
    "time"

    "github.com/xuri/excelize"
)

func min(a, b int) int {
    if a < b {
        return a
    }
    return b
}

func OnMult(n int, n2 int) time.Duration{

    matrix1 := make([]int, n*n)
    matrix2 := make([]int, n*n)
    matrix3 := make([]int, n*n)
    

    for i := 0; i < n; i++ {
        for j := 0; j < n; j++ {
            matrix1[i*n + j] = 1.0
            matrix2[i*n + j] = (i + 1)
            matrix3[i*n + j] = 0
        }
    }

    start := time.Now()

    for i := 0; i < n; i++ {
        for j := 0; j < n; j++ {
            for k := 0; k < n; k++ {
                matrix3[i*n +j] += matrix1[i*n + k] * matrix2[k*n + j]
            }
        }
    }

    elapsed := time.Since(start)
    fmt.Printf("OnMult: %dx%d\n", n,n)
    fmt.Printf("Time: %.3f seconds\n", elapsed.Seconds())

    fmt.Printf("Result matrix: ")
    for j:= 0; j < 1; j++{
        for i := 0; i < min(10, n); i++ {
            fmt.Printf("%d ",matrix3[i])
        }
    }
    fmt.Printf("\n\n")
    return elapsed
}

func OnMultLine(n int, n2 int) time.Duration {

    matrix1 := make([]int, n*n)
    matrix2 := make([]int, n*n)
    matrix3 := make([]int, n*n)
    

    for i := 0; i < n; i++ {
        for j := 0; j < n; j++ {
            matrix1[i*n + j] = 1.0
            matrix2[i*n + j] = (i + 1)
            matrix3[i*n + j] = 0
        }
    }

    start := time.Now()

    for i := 0; i < n; i++ {
        for k := 0; k < n; k++ {
            for j := 0; j < n; j++ {
                matrix3[i*n +j] += matrix1[i*n + k] * matrix2[k*n + j]
            }
        }
    }
    
    elapsed := time.Since(start)
    fmt.Printf("OnMultLine: %dx%d\n", n,n)
    fmt.Printf("Time: %.3f seconds\n", elapsed.Seconds())

    fmt.Printf("Result matrix: ")
    for j:= 0; j < 1; j++{
        for i := 0; i < min(10, n); i++ {
            fmt.Printf("%d ",matrix3[i])
        }
    }
    fmt.Printf("\n\n")
    return elapsed
}

func OnMultBlock(n int, bkSize int) time.Duration{

    matrix1 := make([]int, n*n)
    matrix2 := make([]int, n*n)
    matrix3 := make([]int, n*n)
    

    for i := 0; i < n; i++ {
        for j := 0; j < n; j++ {
            matrix1[i*n + j] = 1.0
            matrix2[i*n + j] = (i + 1)
            matrix3[i*n + j] = 0
        }
    }

    start := time.Now()

    for x := 0; x < n; x += bkSize {
		for y := 0; y < n; y += bkSize {
			for z := 0; z < n; z += bkSize {
				for i := x; i < x + bkSize; i++ {
					for j := y; j < y + bkSize; j++ {
						for k := z; k < z + bkSize; k++ {
							matrix3[i*n+k] += matrix1[i*n+j] * matrix2[j*n+k];
						}
					}
				}
			}
		}
    }
    
    elapsed := time.Since(start)
    fmt.Printf("OnMultBlock: %dx%d, bkSize = %d\n", n,n, bkSize)
    fmt.Printf("Time: %.3f seconds\n", elapsed.Seconds())
    fmt.Printf("Result matrix: ")
    for j:= 0; j < 1; j++{
        for i := 0; i < min(10, n); i++ {
            fmt.Printf("%d ",matrix3[i])
        }
    }
    fmt.Printf("\n\n")
    return elapsed
}

func RunAll(){
    f, err := excelize.OpenFile("../doc/data/all_data.xlsx")
    if err != nil {
        fmt.Println(err)
        return
    }
    defer func() {
        // Close the spreadsheet.
        if err := f.Close(); err != nil {
            fmt.Println(err)
        }
    }()
    
    type params struct{
        p1 int
        p2 int
    }

    var i int = 0

    var paramsV []params = []params{
        //OnMult
		params{600, 600}, 
		params{1000, 1000},
		params{1400, 1400},
		params{1800, 1800},
		params{2200, 2200},
		params{2600, 2600},
		params{3000, 3000},

		//OnMultLine
		params{600, 600}, 
		params{1000, 1000},
		params{1400, 1400},
		params{1800, 1800},
		params{2200, 2200},
		params{2600, 2600},
		params{3000, 3000},
		params{4096, 4096},
		params{6144, 6144},
		params{8192, 8192},
		params{10240, 10240},

		//OnMultBlock
		params{4096,128},
		params{4096,256},
		params{4096,512},
		params{4096,1024},
		params{6144,128},
		params{6144,256},
		params{6144,512},
		params{6144,1024},
		params{8192,128},
		params{8192,256},
		params{8192,512},
		params{8192,1024},
		params{10240,128},
		params{10240,256},
		params{10240,512},
		params{10240,1024},
    }

    funcs := [3]func(int, int) time.Duration{
        OnMult,
        OnMultLine,
        OnMultBlock,
    }

    cells1 := [34]string{
        "X7",
        "X10",
        "X13",
        "X16",
        "X19",
        "X22",
        "X25",
        "AE7",
        "AE10",
        "AE13",
        "AE16",
        "AE19",
        "AE22",
        "AE25",
        "AE28",
        "AE31",
        "AE34",
        "AE37",
        "AM7",
        "AM10",
        "AM13",
        "AM16",
        "AM19",
        "AM22",
        "AM25",
        "AM28",
        "AM31",
        "AM34",
        "AM37",
        "AM40",
        "AM43",
        "AM46",
        "AM49",
        "AM52",
    }

    /*cells2 := [34]string{
        "X8",
        "X11",
        "X14",
        "X17",
        "X20",
        "X23",
        "X26",
        "AE8",
        "AE11",
        "AE14",
        "AE17",
        "AE20",
        "AE23",
        "AE26",
        "AE29",
        "AE32",
        "AE35",
        "AE38",
        "AM7",
        "AM10",
        "AM13",
        "AM16",
        "AM19",
        "AM22",
        "AM25",
        "AM28",
        "AM31",
        "AM34",
        "AM37",
        "AM40",
        "AM43",
        "AM46",
        "AM49",
        "AM53",
    }*/

    /*cells3 := [34]string{
        "X9",
        "X12",
        "X15",
        "X18",
        "X21",
        "X24",
        "X27",
        "AE9",
        "AE12",
        "AE15",
        "AE18",
        "AE21",
        "AE24",
        "AE27",
        "AE30",
        "AE33",
        "AE36",
        "AE39",
        "AM9",
        "AM12",
        "AM15",
        "AM18",
        "AM21",
        "AM24",
        "AM27",
        "AM30",
        "AM33",
        "AM36",
        "AM39",
        "AM42",
        "AM45",
        "AM48",
        "AM51",
        "AM54",
    }*/

	for j := 0; j < 3; j++{
		if j == 7 || j == 18 { i++ }
        
        f.SetCellValue("Folha1", cells1[j], funcs[i](paramsV[j].p1, paramsV[j].p2).Seconds())

	}	

    // Save spreadsheet by the given path.
    if err := f.SaveAs("../doc/data/all_data.xlsx"); err != nil {
        fmt.Println(err)
    }

}

func main() {
    fmt.Println("1. Multiplication")
    fmt.Println("2. Line Multiplication")    
    fmt.Println("3. Block Multiplication")
    fmt.Println("4. Extract Data")
    fmt.Println("0. Exit")
    var option int
    var sz int
    var bkSize int
    fmt.Println("Selection?: ")
    fmt.Scanln(&option)

    if option == 1 {
        fmt.Println("Dimensions: lins=cols ? ")
        fmt.Scanln(&sz)
        OnMult(sz, sz)
    }
    if option == 2 {
        fmt.Println("Dimensions: lins=cols ? ")
        fmt.Scanln(&sz)
        OnMultLine(sz, sz)
    }
    if option == 3 {
        fmt.Println("Dimensions: lins=cols ? ")
        fmt.Scanln(&sz)
        fmt.Println("Block Size?: ")
        fmt.Scanln(&bkSize)
        OnMultBlock(sz, bkSize)
    }
    if option == 4 {
        RunAll();
    }
}