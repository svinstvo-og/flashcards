package com.example.flashcards.controller;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class FlashcardController {

    private List<Flashcard> flashcards;
    private List<Flashcard> incorrectFlashcards;
    private boolean showEnglish;

    @GetMapping("/")
    public String startGame() {
        return "start"; // Start page to choose language
    }

    @PostMapping("/start")
    public String initializeGame(@RequestParam("language") String language, Model model) {
        showEnglish = language.equals("english");
        loadFlashcards();
        model.addAttribute("flashcard", flashcards.get(0));
        model.addAttribute("showEnglish", showEnglish);
        return "game";
    }

    @PostMapping("/answer")
    public String answer(@RequestParam("id") int id, @RequestParam("correct") boolean correct, Model model) {
        Flashcard currentCard = flashcards.remove(0);
        if (!correct) {
            incorrectFlashcards.add(currentCard);
        }

        if (flashcards.isEmpty() && incorrectFlashcards.isEmpty()) {
            return "end"; // End page
        }

        if (flashcards.isEmpty()) {
            flashcards.addAll(incorrectFlashcards);
            incorrectFlashcards.clear();
        }

        model.addAttribute("flashcard", flashcards.get(0));
        model.addAttribute("showEnglish", showEnglish);
        return "game";
    }

    private void loadFlashcards() {
        flashcards = new ArrayList<>();
        incorrectFlashcards = new ArrayList<>();

        try (InputStream inputStream = getClass().getResourceAsStream("/words.xlsx");
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                int id = (int) row.getCell(0).getNumericCellValue();
                String english = row.getCell(1).getStringCellValue();
                String russian = row.getCell(2).getStringCellValue();
                flashcards.add(new Flashcard(id, english, russian));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.shuffle(flashcards);
    }

    static class Flashcard {
        private int id;
        private String english;
        private String russian;

        public Flashcard(int id, String english, String russian) {
            this.id = id;
            this.english = english;
            this.russian = russian;
        }

        public int getId() {
            return id;
        }

        public String getEnglish() {
            return english;
        }

        public String getRussian() {
            return russian;
        }
    }
}