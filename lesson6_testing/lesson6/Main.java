package java3.lesson6;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
//1-е задание:
        int[] arr1 = {1, 2, 4, 4, 2, 3, 4, 1, 7, 4, 5, 4, 8, 8, 8, 9};
        System.out.printf(Arrays.toString(methodAfterLast4(arr1)));

//2-е задание
        int[] arr2 = {1,  4, 4, 4, 1, 4};
        System.out.println("\n" + check1And4(arr2));
        System.out.println(check1And4(arr1));
    }

//Метод для 1-го задания:
    public static int[] methodAfterLast4 (int[] arr) throws RuntimeException {
        int n = 0;
        String str = Arrays.toString(arr);
        if (str.lastIndexOf(String.valueOf(4)) != -1) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == 4) n = i;
            }
            int[] arr1 = new int[arr.length - n - 1];
            System.arraycopy(arr, n + 1, arr1, 0, arr1.length);
            return arr1;
        } else {
            throw new RuntimeException("4 нет");
        }
    }

//Метод для 2-го задания:
        public static boolean check1And4(int[] arr) {
            boolean a = false, b = false;
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == 1) {
                    a = true;
                }
                else if (arr[i] == 4) {
                    b = true;
                }
                else return false;
            }
            return a && b;
        }

}


