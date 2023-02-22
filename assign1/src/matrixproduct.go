package main

import (
    "fmt"
    "time"
)

func OnMult(n int) {
    start := time.Now()

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

    for i := 0; i < n; i++ {
        for j := 0; j < n; j++ {
            for k := 0; k < n; k++ {
                matrix3[i*n +j] += matrix1[i*n + k] * matrix2[k*n + j]
            }
        }
    }


    for i := 0; i < 20; i++ {
        fmt.Println(matrix3[i])
    }
    
    elapsed := time.Since(start)
    fmt.Printf("Binomial took %s\n", elapsed)
}

func OnMultLine(n int) {
    start := time.Now()

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

    for i := 0; i < n; i++ {
        for k := 0; k < n; k++ {
            for j := 0; j < n; j++ {
                matrix3[i*n +j] += matrix1[i*n + k] * matrix2[k*n + j]
            }
        }
    }


    for i := 0; i < 20; i++ {
        fmt.Println(matrix3[i])
    }
    
    elapsed := time.Since(start)
    fmt.Printf("Binomial took %s\n", elapsed)
}

func main() {
    fmt.Println("What option?")
    fmt.Println("1 - Multiplication")
    fmt.Println("2 - Line Multiplication")
    fmt.Println("3 - Exit")
    var option int
    var sz int
    fmt.Scanln(&option)
    fmt.Println("Matrix Size:")
    fmt.Scanln(&sz)
    if option == 1 {
        OnMult(sz)
    }
    if option == 2 {
        OnMultLine(sz)
    }
}