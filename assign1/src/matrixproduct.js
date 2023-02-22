function OnMult(m_ar, m_br){

    let temp;
    let pha = new Array();
    let phb = new Array();
    let phc = new Array();


    for(let i = 0; i< m_ar; i++){
        for(let j = 0; j<m_ar; j++){
            pha[i*m_ar + j] = 1;
        }
    }

    for(let i = 0; i< m_br; i++){
        for(let j = 0; j<m_br; j++){
            phb[i*m_br + j] = i+1;
        }
    }

    const start = Date.now();

    for(let i = 0; i< m_ar; i++){
        for(let j = 0; j<m_br; j++){
            temp = 0;
            for(let k = 0; k<m_ar; k++){
                temp += pha[i*m_ar+k] * phb[k*m_br+j];
            }
            phc[i*m_ar+j]=temp;
        }
    }

    const end = Date.now();

    console.log(`Matrix: ${m_ar}x${m_ar}`);
    console.log(`Time: ${((end - start)*0.001).toFixed(3)} seconds\n`);

    document.getElementById(`std${m_ar}`).innerHTML = `${((end - start)*0.001).toFixed(3)}`;


    // display 10 elements of the result matrix to verify correctness
    console.log("Result matrix: \n");
    for(let i=0; i<1; i++){
        for(let j=0; j<Math.min(10,m_br); j++){
            console.log(`${phc[j]} `);
        }
    }

    console.log("\n");

    pha = null;
    phb = null;
    phc = null;

}

// add code here for line x line matriz multiplication
function OnMultLine(m_ar, m_br){

    let temp;
    let pha = new Array();
    let phb = new Array();
    let phc = new Array();


    for(let i = 0; i< m_ar; i++){
        for(let j = 0; j<m_ar; j++){
            pha[i*m_ar + j] = 1;
        }
    }

    for(let i = 0; i< m_br; i++){
        for(let j = 0; j<m_br; j++){
            phb[i*m_br + j] = i+1;
        }
    }

    const start = Date.now();

    for(let i = 0; i< m_ar; i++){
        for(let j = 0; j<m_br; j++){
            temp = 0;
            for(let k = 0; k<m_ar; k++){
                temp += pha[j*m_ar+k] * phb[k*m_br+i];
            }
            phc[j*m_ar+i]=temp;
        }
    }

    const end = Date.now();

    console.log(`Matrix: ${m_ar}x${m_ar}`);
    console.log(`Time: ${((end - start)*0.001).toFixed(3)} seconds\n`);

    document.getElementById(`lbl${m_ar}`).innerHTML = `${((end - start)*0.001).toFixed(3)}`;


    // display 10 elements of the result matrix to verify correctness
    console.log("Result matrix: \n");
    for(let i=0; i<1; i++){
        for(let j=0; j<Math.min(10,m_br); j++){
            console.log(`${phc[j]} `);
        }
    }
    
    console.log("\n");

    pha = null;
    phb = null;
    phc = null;

}

function main(){

    let op = 1;

    do{
        console.log("\n1. Multiplication\n");
        console.log("2. Line Multiplication\n");
        console.log("0. Exit\n");
        op = prompt("Selection?: ");
        if(op === 0){
            break;
        }
        const lin = prompt("Dimensions: lins=cols ? ");
        const col = lin;

        switch(op){
            case 1:
                OnMult(lin, col);
                break;
            case 2:
                OnMultLine(lin, col);
                break;
        }

    } while(op != 0);
}

document.getElementById("buttonSTD").addEventListener('click', function() {
    OnMult(600, 600);
    OnMult(1000, 1000);
    OnMult(1400, 1400);
    OnMult(1800, 1800);
    OnMult(2200, 2200);
    OnMult(2600, 2600);
    OnMult(3000, 3000);
});

document.getElementById("buttonLBL").addEventListener('click', function() {
    OnMultLine(600, 600);
    OnMultLine(1000, 1000);
    OnMultLine(1400, 1400);
    OnMultLine(1800, 1800);
    OnMultLine(2200, 2200);
    OnMultLine(2600, 2600);
    OnMultLine(3000, 3000);
});
