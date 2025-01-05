package main.java.ru.lexawin;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.*;

public class Hangman {
    private static final String[] HANGMAN_STATES = {
            """
        ╔═══╕
        ║
        ║
        ║
        ║
        ╩""",
            """
        ╔═══╕
        ║   0
        ║
        ║
        ║
        ╩""",
            """
        ╔═══╕
        ║   0
        ║   │
        ║
        ║
        ╩""",
            """
        ╔═══╕
        ║   0
        ║  /│
        ║
        ║
        ╩""",
            """
        ╔═══╕
        ║   0
        ║  /│\\
        ║
        ║
        ╩""",
            """
        ╔═══╕
        ║   0
        ║  /│\\
        ║  /
        ║
        ╩""",
            """
        ╔═══╕
        ║   0
        ║  /│\\
        ║  / \\
        ║
        ╩""",
    };

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Random RANDOM = new Random();

    private static final String GAME_STATUS_WIN = "ПОБЕДА";
    private static final String GAME_STATUS_LOSE = "ПОРАЖЕНИЕ";
    private static final String GAME_STATUS_NOT_FINISHED = "ИГРА НЕ ЗАКОНЧЕНА";

    public static void main(String[] args) {
        runGame();
    }

    private static void runGame() {
        List<String> wordList = getWordList();

        if (wordList == null) {
            System.err.println("ОШИБКА! Не найден файл со словами! Продолжение игры невозможно.");

            return;
        }

        if (wordList.isEmpty()) {
            System.err.println("ОШИБКА! Файл не содержит слов! Продолжение игры невозможно.");

            return;
        }

        do {
            printMainMenu();

            String userInput = SCANNER.nextLine();

            if (userInput.equalsIgnoreCase("н")) {
                String word = getRandomWord(wordList);

                runGameRound(word);
            } else if (userInput.equalsIgnoreCase("в")) {
                SCANNER.close();

                return;
            }
        } while (true);
    }

    private static List<String> getWordList() {
        File file = Paths.get("resources", "words").toFile();
        Scanner fileScanner;

        try {
            fileScanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            return null;
        }

        List<String> wordList = new ArrayList<>();

        while (fileScanner.hasNextLine()) {
            wordList.add(fileScanner.nextLine());
        }

        fileScanner.close();

        return wordList;
    }

    private static String getRandomWord(List<String> wordList) {
        int index = RANDOM.nextInt(wordList.size());

        return wordList.get(index);
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("ГЛАВНОЕ МЕНЮ                       ");
        System.out.println("-----------------------------------");
        System.out.println("Начать новую игру - введите Н или н");
        System.out.println("Выход - введите В или в            ");
        System.out.println("-----------------------------------");
        System.out.print("Ваш выбор: ");
    }

    private static void runGameRound(String word) {
        StringBuilder maskedWord = getMaskedWord(word);
        int errorsCount = 0;
        int guessedCount = 0;
        Set<String> wrongLetters = new LinkedHashSet<>();
        String gameStatus;

        do {
            printGameStatus(maskedWord, errorsCount, wrongLetters);

            gameStatus = checkGameStatus(errorsCount, guessedCount, word);

            if (gameStatus.equals(GAME_STATUS_NOT_FINISHED)) {
                System.out.println();
                System.out.print("Буква: ");

                String enteredLetter = SCANNER.nextLine().toLowerCase();

                if (isEnteredLetterValid(enteredLetter)) {
                    if (word.contains(enteredLetter) && maskedWord.indexOf(enteredLetter) == -1) {
                        guessedCount += openLetter(enteredLetter, word, maskedWord);
                    } else if (!word.contains(enteredLetter) && !wrongLetters.contains(enteredLetter)) {
                        errorsCount++;
                        wrongLetters.add(enteredLetter);
                    }
                } else {
                    System.out.println();
                    System.out.println("Введите одну букву русского алфавита!");
                }
            } else {
                break;
            }
        } while (true);

        System.out.println(gameStatus + "! Было загадано слово: " + word.toUpperCase() + ".");
    }

    private static StringBuilder getMaskedWord(String word) {
        String MASK_SYMBOL = "_";

        StringBuilder maskedWord = new StringBuilder(word);

        for (int i = 0; i < maskedWord.length(); i++) {
            maskedWord.replace(i, i + 1, MASK_SYMBOL);
        }

        return maskedWord;
    }

    private static boolean isEnteredLetterValid(String enteredLetter) {
        if (enteredLetter.length() != 1) {
            return false;
        }

        char character = enteredLetter.charAt(0);

        return character >= 'а' && character <= 'я' || character == 'ё';
    }

    /**
     * Открывает букву в маскированном загаданном слове.
     * @param enteredLetter - введенная буква
     * @param word - загаданное слово
     * @param maskedWord - маскированное загаданное слово
     * @return - количество открытых букв
     */
    private static int openLetter(String enteredLetter, String word, StringBuilder maskedWord) {
        int openCount = 0;
        int letterIndex = 0;

        do {
            letterIndex = word.indexOf(enteredLetter, letterIndex);

            if (letterIndex == -1) break;

            maskedWord.replace(letterIndex, letterIndex + 1, enteredLetter);
            letterIndex++;
            openCount++;
        } while (true);

        return openCount;
    }

    private static void printGameStatus(StringBuilder maskedWord, int errorsCount, Set<String> wrongLetters) {
        System.out.println();
        System.out.println("Слово: " + maskedWord.toString().toUpperCase());
        System.out.println("Ошибки (" + errorsCount + "): " + String.join(", ", wrongLetters));
        System.out.println(HANGMAN_STATES[errorsCount]);
    }

    private static String checkGameStatus(int errorsCount, int guessedCount, String word) {
        int MAX_ERRORS_COUNT = 6;

        if (errorsCount == MAX_ERRORS_COUNT) {
            return GAME_STATUS_LOSE;
        }

        if (guessedCount == word.length()) {
            return GAME_STATUS_WIN;
        }

        return GAME_STATUS_NOT_FINISHED;
    }
}
