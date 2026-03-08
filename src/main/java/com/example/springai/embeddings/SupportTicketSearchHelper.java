package com.example.springai.embeddings;

import com.example.springai.services.OpenAiService;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SupportTicketSearchHelper {

    @Autowired
    private OpenAiService service;

    @GetMapping("/showSupportTicketSearchHelper")
    public String showSupportTicketSearchHelper() {
        return "supportTicketSearchHelper";

    }

    @PostMapping("/supportTicketSearchHelper")
    public String supportTicketSearchHelper(@RequestParam String query, Model model) {
        List<Document> response =  service.searchTickets(query);
        model.addAttribute("response",response);
        return "supportTicketSearchHelper";

    }
}
