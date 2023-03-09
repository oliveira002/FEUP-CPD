# CPD Project 1

CPD Project 1 of group T04G13.

## Installation and compilation instructions

Since C++ and Go are compiled languages and JS is interpreted, in order to run the programs, we need to install their compilers/interpreters and all the required dependencies. <br>
**Note**: If you do not wish to install all the dependencies, C++ and Go programs without them are available at [/src/nolib](https://git.fe.up.pt/cpd/2223/t04/g13/-/tree/main/assign1/src/nolib)

### C++
To compile the C++ code we need a compiler like g++. It usually comes installed by default in Unix systems. To check this, type: `g++ --version`. <br>
If it is not installed, try installing it with: `sudo apt install g++`. <br>
Before we compile the C++ code, we need to install two dependencies (libraries), whose installation guide can be found at:
- Papi: https://moodle.up.pt/mod/page/view.php?id=115178
- xlsxwriter: http://libxlsxwriter.github.io/getting_started.html

Now, we need to find the required arguments and paths for the xlsxwriter lib linking. This can be achieved with the command: `pkg-config --cflags --libs xlsxwriter`. <br>
After that, go to the folder where the C++ file is located and compile it with the following command, appending at the end the result of the previous command (i.e.): `g++ -O2 matrixproduct.cpp -o matrixproduct -lpapi -I/usr/local/include -L/usr/local/lib -lxlsxwriter -lz`.

### Go
To install the Go compiler, follow this tutorial: https://go.dev/doc/install <br>
Then, set the GO111MODULE variable to off with: `go env -w GO111MODULE=off`. <br>
Now, we can install the required excelize library with: `go get github.com/xuri/excelize`. <br>
Finally, to compile and run the code do: `go run <file path>`.

### JavaScript
To successfully run this JavaScript code, we'll need to have the node.js console, so that we can run JS code without a browser, which would cause unexpected issues. <br>
To install node.js we'll follow this guide up until "Install Node.js using NodeSouce repository": https://www.geeksforgeeks.org/installation-of-node-js-on-linux/ <br>
Then check that node is installed by running: `node --version`. <br>
Since we're not using the browser to run our code, we do not have a GUI and packages to read input from the console during execution will, for some reason, slow down the code tenfold, so, to execute the code, just run the following command, taking into account the command line arguments you want: `node <file path> <Algorithm> [Matrix Size] [Block Size]`.

| Algorithm | Description | 
| --- | --- |
| Default | Standard multiplication algorithm |
| Line | Line multiplication algorithm |
| Block | Block multiplication algorithm |
| All | Run all multiplication algorithms (to extract data) |


**Matrix Size**: integer > 1 <br>
**Block Size**: integer > 1

**Example 1**: `node matrixproduct.js All` <br>
**Example 2**: `node matrixproduct.js Line 3000` <br>
**Example 3**: `node matrixproduct.js Block 4096 1024` <br>


**NOTE**: While running some of the calculations, it may happen that an error is thrown: `FATAL ERROR: CALL_AND_RETRY_LAST Allocation failed - JavaScript heap out of memory`. <br>
This may or may not happen because node limits the amount of memory a process can use. To bypass this limit you need to run the following command: `export NODE_OPTIONS=--max_old_space_size=4096`.

## Data and testing

All the collected data is available at (/doc/data/all_data.xlsx)[https://git.fe.up.pt/cpd/2223/t04/g13/-/blob/main/assign1/doc/data/all_data.xlsx] in the sheet "Folha1". <br>
Some extra charts that didn't make it to the final report can be seen in the sheet "Graficos" of the excel document and the conclusions to be taken from them are left as an exercise to the reader.

## Group members:

1. Diogo Babo (up202004950@up.pt)
2. Gustavo Costa (up202004187@up.pt)
3. João Oliveira (up20204407@up.pt)
4. José Araújo (up202007921@up.pt)
