function OnMult(m_ar, m_br) {

    let pha = new Array();
    let phb = new Array();
    let phc = new Array();


    for (let i = 0; i < m_ar; i++) {
        for (let j = 0; j < m_ar; j++) {
            pha[i * m_ar + j] = 1;
            phb[i * m_br + j] = i + 1;
            phc[i * m_br + j] = 0;
        }
    }

    const start = Date.now();

    for (let i = 0; i < m_ar; i++) {
        for (let j = 0; j < m_br; j++) {
            for (let k = 0; k < m_ar; k++) {
                phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
            }
        }
    }

    const end = Date.now();
    const elapsed = (end - start) * 0.001
    console.log(`OnMult: ${m_ar}x${m_ar}`);
    console.log(`Time: ${elapsed.toFixed(3)} seconds`);

    // display 10 elements of the result matrix to verify correctness
    process.stdout.write("Result matrix: ");
    for (let i = 0; i < 1; i++) {
        for (let j = 0; j < Math.min(10, m_br); j++) {
            process.stdout.write(`${phc[j]} `);
        }
    }

    console.log("\n");

    pha = null;
    phb = null;
    phc = null;
    return elapsed
}

function OnMultLine(m_ar, m_br) {

    let pha = new Array();
    let phb = new Array();
    let phc = new Array();

    for (let i = 0; i < m_ar; i++) {
        for (let j = 0; j < m_ar; j++) {
            pha[i * m_ar + j] = 1;
            phb[i * m_br + j] = i + 1;
            phc[i * m_br + j] = 0;
        }
    }

    const start = Date.now();


    for (let i = 0; i < m_ar; i++) {
        for (let k = 0; k < m_br; k++) {
            for (let j = 0; j < m_ar; j++) {
                phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_ar + j];
            }
        }
    }

    const end = Date.now();
    const elapsed = (end - start) * 0.001
    console.log(`OnMultLine: ${m_ar}x${m_ar}`);
    console.log(`Time: ${elapsed.toFixed(3)} seconds`);

    // display 10 elements of the result matrix to verify correctness
    process.stdout.write("Result matrix: ");
    for (let i = 0; i < 1; i++) {
        for (let j = 0; j < Math.min(10, m_br); j++) {
            process.stdout.write(`${phc[j]} `);
        }
    }

    console.log("\n");

    pha = null;
    phb = null;
    phc = null;
    return elapsed
}

function OnMultBlock(m_ar, m_br, bkSize) {

    let pha = new Array();
    let phb = new Array();
    let phc = new Array();


    for (let i = 0; i < m_ar; i++) {
        for (let j = 0; j < m_ar; j++) {
            pha[i * m_ar + j] = 1;
            phb[i * m_br + j] = i + 1;
            phc[i * m_br + j] = 0;
        }
    }

    const start = Date.now();

    for (let x = 0; x < m_ar; x += bkSize) {
        for (let y = 0; y < m_ar; y += bkSize) {
            for (let z = 0; z < m_ar; z += bkSize) {
                for (let i = x; i < x + bkSize; i++) {
                    for (let j = y; j < y + bkSize; j++) {
                        for (let k = z; k < z + bkSize; k++) {
                            phc[i * m_ar + k] += pha[i * m_ar + j] * phb[j * m_ar + k];
                        }
                    }
                }
            }
        }
    }

    const end = Date.now();
    const elapsed = (end - start) * 0.001
    console.log(`OnMultBlock: ${m_ar}x${m_ar}`);
    console.log(`Time: ${elapsed.toFixed(3)} seconds`);

    // display 10 elements of the result matrix to verify correctness
    process.stdout.write("Result matrix: ");
    for (let i = 0; i < 1; i++) {
        for (let j = 0; j < Math.min(10, m_br); j++) {
            process.stdout.write(`${phc[j]} `);
        }
    }

    console.log("\n");

    pha = null;
    phb = null;
    phc = null;
    return elapsed
}

function RunAll(){

    const fs = require('fs');

    let content = "";

    content += OnMult(600, 600) + "\n";
    content += OnMult(1000, 1000) + "\n";
    content += OnMult(1400, 1400) + "\n";
    content += OnMult(1800, 1800) + "\n";
    content += OnMult(2200, 2200) + "\n";
    content += OnMult(2600, 2600) + "\n";
    content += OnMult(3000, 3000) + "\n";

    content += OnMultLine(600, 600) + "\n";
    content += OnMultLine(1000, 1000) + "\n";
    content += OnMultLine(1400, 1400) + "\n";
    content += OnMultLine(1800, 1800) + "\n";
    content += OnMultLine(2200, 2200) + "\n";
    content += OnMultLine(2600, 2600) + "\n";
    content += OnMultLine(3000, 3000) + "\n";
    content += OnMultLine(4096, 4096) + "\n";
    content += OnMultLine(6144, 6144) + "\n";
    content += OnMultLine(8192, 8192) + "\n";
    content += OnMultLine(10240, 10240) + "\n";


    content += OnMultBlock(4096, 4096, 128) + "\n";
    content += OnMultBlock(4096, 4096, 256) + "\n";
    content += OnMultBlock(4096, 4096, 512) + "\n";
    content += OnMultBlock(4096, 4096, 1024) + "\n";

    content += OnMultBlock(6144, 6144, 128) + "\n";
    content += OnMultBlock(6144, 6144, 256) + "\n";
    content += OnMultBlock(6144, 6144, 512) + "\n";
    content += OnMultBlock(6144, 6144, 1024) + "\n";

    content += OnMultBlock(8192, 8192, 128) + "\n";
    content += OnMultBlock(8192, 8192, 256) + "\n";
    content += OnMultBlock(8192, 8192, 512) + "\n";
    content += OnMultBlock(8192, 8192, 1024) + "\n";

    content += OnMultBlock(10240, 10240, 128) + "\n";
    content += OnMultBlock(10240, 10240, 256) + "\n";
    content += OnMultBlock(10240, 10240, 512) + "\n";
    content += OnMultBlock(10240, 10240, 1024) + "\n";

    fs.appendFile('../doc/data/JS/js_data.txt', content, err => {
        if (err) {
            console.error(err);
        }
    });

}

function main()
{   
    if(process.argv.length < 3)
        console.error("ERROR: Unspecified algorithm");

    switch(process.argv[2]){
        //Default
        case "Default":
            if(Number(process.argv[3]) < 1 || isNaN(process.argv[3])){
                console.error("ERROR: Invalid Matrix Size");
            }
            lin = Number(process.argv[3])
            col = lin
            OnMult(lin, col)
            break;
        //Line By Line
        case "Line":
            if(Number(process.argv[3]) < 1 || isNaN(process.argv[3])){
                console.error("ERROR: Invalid Matrix Size");
            }
            lin = Number(process.argv[3])
            col = lin
            OnMultLine(lin, col)
            break;
        //Block
        case "Block":
            if(Number(process.argv[3]) < 1 || isNaN(process.argv[3])){
                console.error("ERROR: Invalid Matrix Size");
            }
            lin = Number(process.argv[3])
            col = lin

            if(Number(process.argv[4]) < 1 || isNaN(process.argv[4])){
                console.error("ERROR: Invalid Block Size");
            }
            bkSize = Number(process.argv[4])
            OnMultBlock(lin, col, bkSize)
            break;
        //All
        case "All":
            RunAll();
            break;
        default:
            console.error("ERROR: Invalid algorithm");
            break;
    }
    
	
}; main();
