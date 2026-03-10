package com.example.springai.services;

import com.example.springai.text.prompttemplate.dtos.CountryCuisines;
import com.example.springai.tools.WeatherTools;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.moderation.Moderation;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private ChatClient chatClient;
    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private OpenAiImageModel openAiImageModell;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private OpenAiAudioTranscriptionModel audioTranscriptionModel;

    @Autowired
    private OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    @Autowired
    private OpenAiAudioSpeechModel openAiAudioSpeechModel;

    @Autowired
    private OpenAiModerationModel openAiModerationModel;

    public OpenAiService(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.chatClient = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public ChatResponse generateAnswer(String question) {
       return  chatClient.prompt(new Prompt(question)).call().chatResponse();
    }

    public String getTravelGuidance(String city, String month, String language, String budget) {
        PromptTemplate promptTemplate = new PromptTemplate("Welcome to the {city} travel guide!\n"
                + "If you're visiting in {month}, here's what you can do:\n" + "1. Must-visit attractions.\n"
                + "2. Local cuisine you must try.\n" + "3. Useful phrases in {language}.\n"
                + "4. Tips for traveling on a {budget} budget.\n" + "Enjoy your trip!");
        Prompt prompt = promptTemplate
                .create(Map.of("city", city, "month", month, "language", language, "budget", budget));

        return chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getText();
    }

    public CountryCuisines getCuisines(String country, String numCuisines, String language) {

        PromptTemplate promptTemplate = new PromptTemplate("You are an expert in traditional cuisines.\n"
                + "Answer the question: What is the traditional cuisine of {country}?\n"
                + "Return a list of {numCuisines} in {language}.\n" + "You provide information about a specific dish \n"
                + "from a specific country.\n" + "Avoid giving information about fictional places.\n"
                + "If the country is fictional or non-existent \n" + "return the country with out any cuisines.");

        Prompt prompt = promptTemplate
                .create(Map.of("country", country, "numCuisines", numCuisines, "language", language));

        return chatClient.prompt(prompt).call().entity(CountryCuisines.class);
    }

    public String getInterviewPreparation(String company, String jobTitle, String strength, String weakness) {
        PromptTemplate promptTemplate = new PromptTemplate("You are a career coach. Provide tailored interview tips for the\n" +
                "position of {jobTitle} at {company}.\n" +
                "Highlight your strengths in {strength} and prepare for questions\n" +
                "about your weaknesses such as {weakness}.");

        Prompt prompt = promptTemplate
                .create(Map.of("jobTitle", jobTitle, "company", company, "strength", strength, "weakness", weakness));

        return chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getText();
    }


public float[] embed(String input) {
    return embeddingModel.embed(input);
}

    public double findSimilarity(String text1, String text2) {
        List<float[]> response = embeddingModel.embed(List.of(text1, text2));
        return cosineSimilarity(response.get(0), response.get(1));
    }

    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must be of the same length");
        }

        // Initialize variables for dot product and magnitudes
        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        // Calculate dot product and magnitudes
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            magnitudeA += vectorA[i] * vectorA[i];
            magnitudeB += vectorB[i] * vectorB[i];
        }

        // Calculate and return cosine similarity
        return dotProduct / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
    }

    public List<Document> searchJobs(String query){
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(5)
                .build();

        return vectorStore.similaritySearch(request);
    }

    public List<Document> searchTickets(String query){
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .filterExpression("docType == 'support'")
                .topK(5)
                .build();

        return vectorStore.similaritySearch(request);
    }

    public String answer(String query){
        return chatClient.prompt(query).advisors(QuestionAnswerAdvisor.builder(vectorStore).build()).call().content();
    }

    public String answerLegal(String query){
        return chatClient.prompt(query).advisors(QuestionAnswerAdvisor.builder(vectorStore).build()).call().content();
    }

    public String generateImage(String prompt){
        return openAiImageModell.call(new ImagePrompt(prompt, OpenAiImageOptions.builder().
                quality("hd").
                height(1024).
                width(1024).
                N(1).
                build())).getResult().getOutput().getUrl();
    }

    public String analyzeImage(String prompt, String path){
        return chatClient.prompt().user(u->u.text(prompt)
                .media(MimeTypeUtils.IMAGE_JPEG,new FileSystemResource(path))).call().content();
    }

    public String analyzeDietHelperImage(String prompt, String path1,String path2){
        return chatClient.prompt().user(u->u.text(prompt)
                .media(MimeTypeUtils.IMAGE_JPEG,new FileSystemResource(path1))
                .media(MimeTypeUtils.IMAGE_JPEG,new FileSystemResource(path2))).call().content();
    }

    public String speechToText(String path){
        OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .language("fr").responseFormat(OpenAiAudioApi.TranscriptResponseFormat.VTT)
                .build();
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(new FileSystemResource(path),options);
             String output = openAiAudioTranscriptionModel.call(prompt).getResult().getOutput();
        return generateAnswer("Generate multiple choice questions based on the following transcript:\n" +output).
                getResult()
                .getOutput()
                .getText();


    }

    public byte[] textToSpeech(String text){
        return openAiAudioSpeechModel.call(text);
    }

    public String callAgent(String query){
        return chatClient.prompt(query).tools(new WeatherTools()).call().content();
    }

    public ModerationResult moderate(String content){
        Moderation moderation =  openAiModerationModel.call(new ModerationPrompt(content)).getResult().getOutput();
        return moderation.getResults().get(0);
    }

    public Flux<String> streamAnswer(String message) {
        return  chatClient.prompt(new Prompt(message)).stream().content();
    }

}
