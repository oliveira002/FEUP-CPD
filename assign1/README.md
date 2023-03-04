# CPD Project 1

CPD Project 1 of group T04G13.

## Instalation instructions

### C++ Code

To compile the C++ code we need a compiler like g++. It usually comes by default in Unix systems. To check this, type: g++ --version
If it not installed yet, install it.

Before we compile the C++ code, we need to install two libraries, whose installation guide can be found at:
    - Papi: https://moodle.up.pt/mod/page/view.php?id=115178
    - xlsxwriter: http://libxlsxwriter.github.io/getting_started.html

Now, we need to find the required arguments and paths for the xlsxwriter lib linking. This can be achieved with the command: pkg-config --cflags --libs xlsxwriter
After that, go to the folder where the C++ file is located and compile it with the following command, appending at the end the result of the previous command (i.e.): g++ -O2 matrixproduct.cpp -o matrixproduct -lpapi -I/usr/local/include -L/usr/local/lib -lxlsxwriter -lz

### GoLang code

First, we need to install the golang compiler. To do so, follow the following tutorial: https://go.dev/doc/install
Then, set the GO111MODULE variable to off with: go env -w GO111MODULE=off 
Now, we can install the required excelize library with: go get github.com/xuri/excelize
Finally, we can compile and run the code using: go run <path to code>

### JavaScript code

To successfully run this JavaScript code, we'll need to have the node.js console, so that we can run JS code without a browser, which would cause unexpected issues.
To install node.js we'll follow this guide **up until** "Install Node.js using NodeSouce repository": https://www.geeksforgeeks.org/installation-of-node-js-on-linux/
Then check that node is installed by running: node --version
Since we're not using the browser to run our code, we do not have a GUI and Node's packages to read input from the console during execution, for some reason, slow down the code tenfold, so, to execute the code, just run the following command, taking into account the command line arguments you want: node <path to code> <algorithm> [Matrix Size] [Block Size]

Command line arguments:

- Algorithm:
    - Default
    - Line
    - Block
    - All

- Matrix Size:
    - integer > 1

- Block Size:
    - integer > 1

NOTE: While running some of the calculations, it may happen that an error is thrown "FATAL ERROR: CALL_AND_RETRY_LAST Allocation failed - JavaScript heap out of memory". This may or may not happen because node limits the ammount of memory a process can use. To bypass this limit you need to run the following command: export NODE_OPTIONS=--max_old_space_size=4096

## Group members:

1. Diogo Babo (up202004950@up.pt)
2. Gustavo Costa (up202004187@up.pt)
3. João Oliveira (up20204407@up.pt)
4. José Araújo (up202007921@up.pt)
