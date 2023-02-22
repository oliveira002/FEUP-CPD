package main

import (
    "fmt"
    "time"
)

func makeMatrix(n int) [][]int {
    m := make([][]int, n)
    for i := range m {
        m[i] = make([]int, n)
    }
    return m
}

func OnMult(n int, x int) {
    start := time.Now()

    var temp int
    matrix1 := makeMatrix(n)
    matrix2 := makeMatrix(n)
    matrix3 := makeMatrix(n)

    for i := 0; i < n; i++ {
        for j := 0; j < n; j++ {
            matrix1[i][j] = 1
        }
    }

    for i := 0; i < n; i++ {
        for j := 0; j < n; j++ {
            matrix2[i][j] = (i + 1)
        }
    }

    for j := 0; j < n; j++ {
        for i := 0; i < n; i++ {
            temp = 0
            for k := 0; k < n; k++ {
                temp += matrix1[i][k] * matrix2[k][j]
            }
            matrix3[i][j] = temp
        }
    }

    //fmt.Println(matrix3[0])
    elapsed := time.Since(start)
    fmt.Printf("Binomial took %s\n", elapsed)
}

func main() {
    OnMult(600, 600);
    OnMult(1000, 1000);
    OnMult(1400, 1400);
    OnMult(1800, 1800);
    OnMult(2200, 2200);
    OnMult(2600, 2600);
    OnMult(3000, 3000);
}