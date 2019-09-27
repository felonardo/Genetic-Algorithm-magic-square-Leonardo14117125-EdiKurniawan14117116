# Genetic-Algorithm-magic-square-Leonardo14117125-EdiKurniawan14117116


### Population

1. ** Ukuran Square: ** berapa banyak jumlah kotak Square di soal 3x3

2. ** Banyak Populasi: ** jumlah individu dalam populasi. Angka ini ditetapkan untuk semua generasi dari algoritma genetika.

### Crossover

1. **Nilai Minimal dan Nilai Maksimal** 

2. **Mutasi :** persentasi masing masing individual dari crossover yang bermutasi, semakin tinggi tingkat persentasinya maka semakin tinggi pula tingkat randomnese nya.


### Representasi individu

Setiap individu direpresentasikan menggunakan array polos dengan garis-garis matriks secara berurutan. Oleh karena itu array `[2, 7, 6, 9, 5, 1, 4, 3, 8]` mewakili kotak:

`` `
2 7 6
9 5 1
4 3 8
`` `

### Fungsi Fitness

1. * angka ajaib * dihitung dengan menggunakan rumus `(L + (L ^ 3)) / 2` di mana` L` adalah ukuran persegi. Ini adalah nilai yang diharapkan untuk jumlah setiap garis, kolom dan diagonal dari sebuah kotak agar dianggap sebagai kotak ajaib. Kami akan menyebut nilai ini `M` mulai sekarang.

2. Jumlah `S` untuk setiap baris, kolom dan diagonal dihitung. Juga, untuk setiap baris, itu dihitung `N = | M-S |` (* angka ajaib - jumlah *)

3. Semua yang dihitung `N`s diringkas menghasilkan nilai Fitness yang lebih baik semakin dekat ke 0. Sebuah kotak ajaib akan memiliki nilai Fitness` 0`, dan nilai Fitness meningkat secara proporsional dengan "jarak" yang diperlukan untuk itu menjadi sebuah kotak ajaib.

#### Contoh 

`` `
Ukuran: 3x3

Kotak:
1 2 3
4 5 6
7 8 9

L = 3
M = (3+ (3 ^ 3)) / 2 = 15

Baris 1:
S = 1 + 2 + 3 = 6
N = | M-S | = | 15-6 | = 9

Baris 2:
S = 4 + 5 + 6 = 15
N = | M-S | = | 15-15 | = 0

Baris 3:
S = 7 + 8 + 9 = 24
N = | M-S | = | 15-24 | = 9

Kolom 1:
S = 1 + 4 + 7 = 12
N = | M-S | = | 15-12 | = 3

Kolom 2:
S = 2 + 5 + 8 = 15
N = | M-S | = | 15-15 | = 0

Kolom 3:
S = 3 + 6 + 9 = 18
N = | M-S | = | 15-18 | = 3

Diagonal 1:
S = 1 + 5 + 9 = 15
N = | M-S | = | 15-15 | = 0

Diagonal 2:
S = 3 + 5 + 7 = 15
N = | M-S | = | 15-15 | = 0

Nilai Fitness = jumlah semua nilai N = 9 + 0 + 9 + 3 + 0 + 3 + 0 + 0 = 24
`` `

### Pilihan untuk crossover
Individu dipilih untuk crossover melalui [pemilihan turnamen] (https://en.wikipedia.org/wiki/Tournament_selection). Ukuran kolam kawin sama dengan setengah dari ukuran populasi.

Oleh karena itu jika ukuran populasi adalah 200, ukuran kolam kawin akan menjadi 100. Untuk setiap generasi baru, kolam kawin kosong baru dibuat. Dua individu baru dipilih secara acak dari populasi dan satu dengan skor kebugaran tertinggi ditambahkan ke kolam kawin. Dalam kasus dasi, salah satu individu ditambahkan ke kolam kawin. Prosedur ini diulangi hingga pool kawin mencapai ukuran yang diharapkan.

### Fungsi crossover
Semua angka dari kotak ajaib harus unik, oleh karena itu angka yang berulang dalam array yang mewakili kotak ajaib tidak diperbolehkan. Metode crossover sederhana seperti satu titik, titik N, cut-and-splice dan beberapa lainnya tidak layak untuk masalah ini karena mereka menghasilkan individu dengan angka berulang sangat sering. Karena itu, perlu untuk menggunakan metode crossover yang lebih canggih.
