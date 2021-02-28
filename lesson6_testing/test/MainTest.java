package test;

import java3.lesson6.Main;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class MainTest {
    private Main main;

    @BeforeEach
    public void startUp() {
        main = new Main();
        System.out.println("Тест начался");
    }

    @AfterEach
    public void shutdown() {
        System.out.println("Тест завершен");
    }


    static Stream<Arguments> dataTask1() {
        return Stream.of(
                Arguments.arguments(new int[] {1, 2, 4, 4, 2, 3, 4, 1, 7, 4, 5, 4, 8, 8, 8, 9}, new int[] { 8, 8, 8, 9}),
                Arguments.arguments(new int[] {1, 2, 4, 4, 2, 3, 4, 1, 7}, new int[] { 1, 7}),
                Arguments.arguments(new int[] {1, 2, 4, 4, 2, 3}, new int[] {2,3})

//неверные аргументы (тест в данном случае не будет пройден):
//                Arguments.arguments(new int[] {1, 2, 4, 4, 2, 3, 4, 1, 7}, new int[] { 5, 6}),
//                Arguments.arguments(new int[] {1, 2, 4, 4, 2, 3}, new int[] {9,8})
        );
    }
    static Stream<Arguments> dataTask2() {
        return Stream.of(
                Arguments.arguments(new int[] {1, 1, 1, 4, 4, 1, 4, 4}, true),
                Arguments.arguments(new int[] {1, 1, 1, 1, 1, 1}, false),
                Arguments.arguments(new int[] {4, 4, 4, 4, 4, 4}, false),
                Arguments.arguments(new int[] {1, 4, 4, 1, 1, 4, 3}, false)

//неверные аргументы (тест в данном случае не будет пройден):
//                Arguments.arguments(new int[] {4, 4, 4, 4, 4, 4}, true),
//                Arguments.arguments(new int[] {1, 4, 4, 1, 1, 4, 3}, true)

        );

    }

    @ParameterizedTest
    @MethodSource ("dataTask1")
    public void massTestTask1( int[] arr, int[] arrResult) {
        try {
            Assertions.assertArrayEquals(arrResult, Main.methodAfterLast4(arr));
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }
    @ParameterizedTest
    @MethodSource ("dataTask2")
    public void massTestTask2( int[] arr, boolean arrResult) {
        try {
            Assertions.assertEquals(arrResult, Main.check1And4(arr));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}