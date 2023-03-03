#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>
#include "xlsxwriter.h"

using namespace std;

#define SYSTEMTIME clock_t

int EventSet = PAPI_NULL;
long long values[4];
int ret;

double OnMult(int m_ar, int m_br)
{

	SYSTEMTIME Time1, Time2;

	char st[100];
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
		{
			pha[i * m_ar + j] = (double)1.0;
			phb[i * m_br + j] = (double)(i + 1);
			phc[i * m_ar + j] = (double)0.0;
		}

	Time1 = clock();

	for (i = 0; i < m_ar; i++)
	{
		for (j = 0; j < m_br; j++)
		{
			for (k = 0; k < m_ar; k++)
			{
				phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
			}
		}
	}

	Time2 = clock();
	double elapsed = (double)(Time2 - Time1) / CLOCKS_PER_SEC;

	cout << "OnMult: " << m_ar << "x" << m_ar << endl;
	sprintf(st, "Time: %3.3f seconds\n", elapsed);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: ";
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

	free(pha);
	free(phb);
	free(phc);

	return elapsed;
}

double OnMultLine(int m_ar, int m_br)
{
	SYSTEMTIME Time1, Time2;

	char st[100];
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
		{
			pha[i * m_ar + j] = (double)1.0;
			phb[i * m_br + j] = (double)(i + 1);
			phc[i * m_ar + j] = (double)0.0;
		}

	Time1 = clock();

	for (i = 0; i < m_ar; i++)
	{
		for (k = 0; k < m_br; k++)
		{
			for (j = 0; j < m_ar; j++)
			{
				phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_ar + j];
			}
		}
	}

	Time2 = clock();

	double elapsed = (double)(Time2 - Time1) / CLOCKS_PER_SEC;
	cout << "OnMultLine: " << m_ar << "x" << m_ar << endl;
	sprintf(st, "Time: %3.3f seconds\n", elapsed);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: ";
	for (i = 0; i < 1; i++)
	{
		for (j = 0; j < min(10, m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl; 

	free(pha);
	free(phb);
	free(phc);

	return elapsed;
}

double OnMultBlock(int m_ar, int bkSize)
{
	SYSTEMTIME Time1, Time2;

	char st[100];
	int i, j, k;
	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
		{
			pha[i * m_ar + j] = (double)1.0;
			phb[i * m_ar + j] = (double)(i + 1);
			phc[i * m_ar + j] = (double)0.0;
		}

	Time1 = clock();

	for (int x = 0; x < m_ar; x += bkSize)
	{
		for (int y = 0; y < m_ar; y += bkSize)
		{
			for (int z = 0; z < m_ar; z += bkSize)
			{
				for (int i = x; i < x + bkSize; i++)
				{
					for (int j = y; j < y + bkSize; j++)
					{
						for (int k = z; k < z + bkSize; k++)
						{
							phc[i * m_ar + k] += pha[i * m_ar + j] * phb[j * m_ar + k];
						}
					}
				}
			}
		}
	}

	Time2 = clock();

	double elapsed = (double)(Time2 - Time1) / CLOCKS_PER_SEC;

	cout << "OnMultBlock: " << m_ar << "x" << m_ar << ", bkSize = " << bkSize << endl;
	sprintf(st, "Time: %3.3f seconds\n", elapsed);
	cout << st;

	cout << "Result matrix: ";
	for (int i = 0; i < 1; i++)
	{
		for (int j = 0; j < min(10, m_ar); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

	free(pha);
	free(phb);
	free(phc);

	return elapsed;
}

void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
	printf("PAPI library version mismatch!\n");
	exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
			<< " MINOR: " << PAPI_VERSION_MINOR(retval)
			<< " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}

typedef double (*f)(int, int); 

f func[3] = { &OnMult, &OnMultLine, &OnMultBlock};

struct params{
	int p1;
	int p2;
};

void RunAll(){

	lxw_workbook *workbook = workbook_new("unformated_data.xlsx");
	lxw_worksheet *worksheet = workbook_add_worksheet(workbook, NULL);
	
	struct params paramsV[38] = {
		//OnMult
		{600, 600}, 
		{1000, 1000},
		{1400, 1400},
		{1800, 1800},
		{2200, 2200},
		{2600, 2600},
		{3000, 3000},
		{4096, 4096},
		{6144, 6144},
		{8192, 8192},
		{10240, 10240},

		//OnMultLine
		{600, 600}, 
		{1000, 1000},
		{1400, 1400},
		{1800, 1800},
		{2200, 2200},
		{2600, 2600},
		{3000, 3000},
		{4096, 4096},
		{6144, 6144},
		{8192, 8192},
		{10240, 10240},

		//OnMultBlock
		{4096,128},
		{4096,256},
		{4096,512},
		{4096,1024},
		{6144,128},
		{6144,256},
		{6144,512},
		{6144,1024},
		{8192,128},
		{8192,256},
		{8192,512},
		{8192,1024},
		{10240,128},
		{10240,256},
		{10240,512},
		{10240,1024}
	};

	
	int i = 0, col = 1;
	for (int j = 0; j < 38; j++){
		if(j == 11 || j == 22) i++;

		// Start counting
		ret = PAPI_start(EventSet);
		if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

		double value = func[i](paramsV[j].p1, paramsV[j].p2);

		ret = PAPI_stop(EventSet, values);
		if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
		printf("L1 DCM: %lld \n",values[0]);
        printf("L1 DCA: %lld \n",values[1]);
		printf("L2 DCM: %lld \n",values[2]);
        printf("L2 DCH: %lld \n\n",values[3]);

        ret = PAPI_reset( EventSet );
        if ( ret != PAPI_OK ) cout << "FAIL reset" << endl;

		worksheet_write_number(worksheet, j+1, 2, value, NULL);

		worksheet_write_string(worksheet, col, 3, "L1 DCM", NULL);
		worksheet_write_number(worksheet, col, 4, values[0], NULL);
		col++;

		worksheet_write_string(worksheet, col, 3, "L1 DCA", NULL);
		worksheet_write_number(worksheet, col, 4, values[1], NULL);
		col++;

		worksheet_write_string(worksheet, col, 3, "L2 DCM", NULL);
		worksheet_write_number(worksheet, col, 4, values[2], NULL);
		col++;

		worksheet_write_string(worksheet, col, 3, "L2 DCH", NULL);
		worksheet_write_number(worksheet, col, 4, values[3], NULL);
		col++;

		worksheet_write_string(worksheet, col, 3, "", NULL);
		worksheet_write_string(worksheet, col, 4, "", NULL);
		col++;
	}	

	workbook_close(workbook);

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
    if ( ret != PAPI_OK )
        std::cout << "FAIL remove event L1_DCM" << endl; 

    ret = PAPI_remove_event( EventSet, PAPI_L1_DCA );
    if ( ret != PAPI_OK )
        std::cout << "FAIL remove event L1_DCA" << endl; 

    ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
    if ( ret != PAPI_OK )
        std::cout << "FAIL remove event L2_DCM" << endl; 

    ret = PAPI_remove_event( EventSet, PAPI_L2_DCH );
    if ( ret != PAPI_OK )
        std::cout << "FAIL remove event L2_DCH" << endl; 

    ret = PAPI_destroy_eventset( &EventSet );
    if ( ret != PAPI_OK )
        std::cout << "FAIL destroy" << endl;
}

int main(int argc, char *argv[])
{

	char c;
	int lin, col, bkSize;
	int op;


	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;

	ret = PAPI_create_eventset(&EventSet);
	if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;

    ret = PAPI_add_event(EventSet,PAPI_L1_DCM);
    if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;

    ret = PAPI_add_event(EventSet,PAPI_L1_DCA);
    if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCA" << endl;

    ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
    if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;

    ret = PAPI_add_event(EventSet,PAPI_L2_DCH);
    if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCH" << endl;

	op = 1;
	do
	{
		cout << endl;
		cout << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "4. Extract Data" << endl;
		cout << "0. Exit" << endl;
		cout << "Selection?: ";
		cin >> op;
		if (op == 0)
			break;

		if(op != 4){
			// Start counting
			ret = PAPI_start(EventSet);
			if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;
		}

		switch (op)
		{
		case 1:
			printf("Dimensions: lins=cols ? ");
			cin >> lin;
			col = lin;
			OnMult(lin, col);
			break;
		case 2:
			printf("Dimensions: lins=cols ? ");
			cin >> lin;
			col = lin;
			OnMultLine(lin, col);
			break;
		case 3:
			printf("Dimensions: lins=cols ? ");
			cin >> lin;
			col = lin;
			cout << "Block Size? ";
			cin >> bkSize;
			OnMultBlock(lin, bkSize);
			break;
		case 4:
			RunAll();
			break;
		}

		if (op == 4)
			break;

		ret = PAPI_stop(EventSet, values);
		if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
		printf("L1 DCM: %lld \n",values[0]);
        printf("L1 DCA: %lld \n",values[1]);
		printf("L2 DCM: %lld \n",values[2]);
        printf("L2 DCH: %lld \n",values[3]);

        ret = PAPI_reset( EventSet );
        if ( ret != PAPI_OK ) cout << "FAIL reset" << endl;

	} while (op != 0);

	if(op != 4){

		ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
		if ( ret != PAPI_OK )
			std::cout << "FAIL remove event L1_DCM" << endl; 

		ret = PAPI_remove_event( EventSet, PAPI_L1_DCA );
		if ( ret != PAPI_OK )
			std::cout << "FAIL remove event L1_DCA" << endl; 

		ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
		if ( ret != PAPI_OK )
			std::cout << "FAIL remove event L2_DCM" << endl; 

		ret = PAPI_remove_event( EventSet, PAPI_L2_DCH );
		if ( ret != PAPI_OK )
			std::cout << "FAIL remove event L2_DCH" << endl; 

		ret = PAPI_destroy_eventset( &EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL destroy" << endl;
	}
}