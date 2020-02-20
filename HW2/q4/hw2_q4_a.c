#include <stdio.h>
#include <time.h>
#include <omp.h>
int omp_get_thread_num();
#include <stdlib.h>
#include <string.h>

void* fileToArray(char file1[], int* rows, int* cols){
  FILE* fptr = fopen(file1, "r");
  char* str = malloc(sizeof(char)*2048);
  char* token; 
  fgets(str, 2048, fptr);
  *rows = atoi(strtok(str, " "));
  *cols = atoi(strtok(NULL, " "));
//  printf("rows: %d, cols: %d\n", *rows, *cols);
  float* matrix = malloc(sizeof(float)*(*rows)*(*cols)); 
  //float matrix[rows][cols];
  char* saveptr;
  for(int i = 0; i < *rows; i++){
    fgets(str, 2048, fptr);
    int j = 0;
    token = strtok_r(str, " ", &saveptr);
    while(token != NULL){
        *(matrix + i*(*cols) + j) = atof(token);
        j++;
        token = strtok_r(NULL, " ", &saveptr);
    }
  }
 /*for (int i = 0; i < *rows; i++){
   for (int j = 0; j < *cols; j++){
     printf("%f ", *(matrix + i*(*cols) + j));
   }
   printf("\n");
 } */
 fclose(fptr);
 return matrix;
}

float product(int row, int col, float* matrix1, float* matrix2, int rows1, int rows2, int cols1, int cols2){
  float res1[cols1];
  float res2[rows2];
  for(int i = 0; i < cols1; i++){
    res1[i] = *(matrix1 + cols1*row + i);
  }
  for(int i = 0; i < rows2; i++){
    res2[i] = *(matrix2 + cols2*i + col);
  }
  float sum = 0;
  for(int i = 0; i < cols1; i++){
    sum += res1[i] * res2[i];
    //printf("%f\n", sum);
  }
  return sum;  
}

void MatrixMult(char file1[],char file2[],int T)
{
  omp_set_num_threads(T);
  int rows1;
  int cols1;
  int rows2;
  int cols2;
  float* matrix1 = fileToArray(file1, &rows1, &cols1);
  float* matrix2 = fileToArray(file2, &rows2, &cols2);

  float* result = malloc(sizeof(float) * rows1 * cols2);
#pragma omp parallel
{
  float sum;
  int i;
  int j;
  #pragma omp for private(sum, i, j) 
  for(i = 0; i < cols2; i++){
    for(j = 0; j < rows1; j++){
      sum = product(i, j, matrix1, matrix2, rows1, rows2, cols1, cols2);
      *(result + (cols2)*i + j) = sum;
//      printf("i: %d j: %dsum: %f, thread: %d\n",i,j, sum, omp_get_thread_num());
    }
  }   
}
//Write your code here
}


void main(int argc, char *argv[])
{
  time_t t = clock();
  char *file1, *file2;
  file1=argv[1];
  file2=argv[2];
  int T=atoi(argv[3]);
  MatrixMult(file1,file2,T);
  printf("Time elapsed: %lu", (clock() - t));
}


