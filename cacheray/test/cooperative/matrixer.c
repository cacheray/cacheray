#include "include/matrixer.h"
#include <stdlib.h>

double **give_matrix(int dim) {
  double **mat = (double **)calloc(dim, sizeof(double *));
  for (int i = 0; i < dim; i++) {
    mat[i] = (double *)calloc(dim, sizeof(double));
  }
  return mat;
}
/*
int[]give_int_matrix(int dim)
{
    int **mat = (int**)malloc(dim*sizeof(int*));
    for(int i = 0; i < dim; i++)
    {
        mat[i] = (int*)malloc(dim*sizeof(int));
    }
    return mat;
}
*/

void invert_matrix(double **mat, int dim) {
}

void get_cofactor(double **mat, double **temp, int p, int q, int n) {
  int i = 0, j = 0;

  for (int row = 0; row < n; row++) {
    for (int col = 0; col < n; col++) {
      if (row != p && col != q) {
        temp[i][j++] = mat[row][col];

        if (j == n - 1) {
          j = 0;
          i++;
        }
      }
    }
  }
}

double determinant(double **mat, int dim, int n) {
  double res = 0;

  if (dim == 1) {
    return mat[0][0];
  }

  double **temp = give_matrix(dim);
  double sign = 1;

  for (int i = 0; i < n; i++) {
    get_cofactor(mat, temp, 0, i, n);
    res += sign * mat[0][i] * determinant(temp, dim, n - 1);
    sign = -sign;
  }

  free(temp);
  return res;
}

void free_matrix(double **mat, int dim) {
  for (int i = 0; i < dim; i++) {
    free(mat[i]);
  }
  free(mat);
}
