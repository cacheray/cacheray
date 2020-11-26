#ifndef __MATRIXER__H__
#define __MATRIXER__H__

double determinant(double **mat, int dim, int n);
void free_matrix(double **mat, int dim);
void get_cofactor(double **mat, double **temp, int p, int q, int n);
double **give_matrix(int dim);
// int[][] give_int_matrix(int dim);
void invert_matrix(double **mat, int dim);

#endif
