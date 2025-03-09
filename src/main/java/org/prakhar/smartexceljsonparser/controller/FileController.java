package org.prakhar.smartexceljsonparser.controller;

import org.prakhar.smartexceljsonparser.service.ExcelService;
import org.prakhar.smartexceljsonparser.service.JsonService;
import org.prakhar.smartexceljsonparser.utils.ValidationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@Controller
public class FileController {
    private final ExcelService excelService;
    private final JsonService jsonService;

    public FileController(ExcelService excelService, JsonService jsonService) {
        this.excelService = excelService;
        this.jsonService = jsonService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/convert")
    public String handleConversion(
            @RequestParam("file") MultipartFile file,
            @RequestParam("conversionType") String conversionType,
            Model model
    ) {
        try {
            ValidationUtils.validateFile(file);
            if (conversionType.equals("excelToJson")) {
                String json = excelService.convertExcelToJson(file);
                model.addAttribute("json", json);
            } else {
                byte[] excelBytes = jsonService.convertJsonToExcel(file.getBytes());
                model.addAttribute("excelBytes", excelBytes);
                model.addAttribute("fileName", "converted.xlsx");
            }
            return "result";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "index";
        }
    }
}