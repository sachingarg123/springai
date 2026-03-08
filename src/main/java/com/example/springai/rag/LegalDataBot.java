package com.example.springai.rag;

import com.example.springai.services.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LegalDataBot {

	@Autowired
	private OpenAiService service;

	@GetMapping("/showLegalDataBot")
	public String showLegalDataBot() {
		return "legalDataBot";

	}

	@PostMapping("/legalDataBot")
	public String legalDataBot(@RequestParam String query, Model model) {
		String response = service.answerLegal(query);
		model.addAttribute("response", response);
		return "legalDataBot";

	}

}