package main

import (
    "fmt"
    "time"
)

func OnMult(n int) {

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
    fmt.Printf("Binomial took %s\n", elapsed)

    for i := 0; i < 20; i++ {
        fmt.Println(matrix3[i])
    }
}

func OnMultLine(n int) {

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
    fmt.Printf("Time: %s\n", elapsed)

    for i := 0; i < 20; i++ {
        fmt.Println(matrix3[i])
    }
}

func OnMultBlock(n int, bkSize int){

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
    fmt.Printf("Time: %s\n", elapsed)

    for i := 0; i < 20; i++ {
        fmt.Println(matrix3[i])
    }

}


func main() {
    fmt.Println("1. Multiplication")
    fmt.Println("2. Line Multiplication")    
    fmt.Println("3. Block Multiplication")
    fmt.Println("0. Exit")
    var option int
    var sz int
    var bkSize int
    fmt.Println("Selection?: ")
    fmt.Scanln(&option)
    fmt.Println("Dimensions: lins=cols ? ")
    fmt.Scanln(&sz)
    if option == 1 {
        OnMult(sz)
    }
    if option == 2 {
        OnMultLine(sz)
    }
    if option == 3 {
        fmt.Println("Block Size?: ")
        fmt.Scanln(&bkSize)
        OnMultBlock(sz, bkSize)
    }
}