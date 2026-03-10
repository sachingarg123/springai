package com.example.springai.moderations;

import org.springframework.ai.moderation.ModerationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.springai.services.OpenAiService;

@Controller
public class ModerationController {

	@Autowired
    private OpenAiService chatService;

    @GetMapping("/showModeration")
    public String showChatPage() {
         return "moderation";
    }

    @PostMapping("/moderation")
    public String getChatResponse(@RequestParam("text") String text, Model model) {
        ModerationResult result = chatService.moderate(text);
        model.addAttribute("response", result);
        return "moderation";
    }
}