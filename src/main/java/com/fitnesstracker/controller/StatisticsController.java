package com.fitnesstracker.controller;

import com.fitnesstracker.dto.ActivityResponse;
import com.fitnesstracker.security.CustomUserDetails;
import com.fitnesstracker.service.StatisticsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public String statistics(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam(name = "range", defaultValue = "30d") String range,
                             Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        Map<String, Object> stats = statisticsService.getStatistics(userDetails.getUser(), range);
        model.addAttribute("stats", stats);
        model.addAttribute("currentRange", range);
        model.addAttribute("availableRanges", Arrays.asList("7d", "30d", "3m", "1y"));

        return "statistics/index";
    }

    @GetMapping("/api")
    @ResponseBody
    public Map<String, Object> statisticsApi(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestParam(name = "range", defaultValue = "30d") String range) {
        if (userDetails == null) {
            return Collections.emptyMap();
        }
        return statisticsService.getStatistics(userDetails.getUser(), range);
    }
}