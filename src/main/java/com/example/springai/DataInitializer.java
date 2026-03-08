package com.example.springai;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.ai.document.Document;

import java.util.List;

@Component
public class DataInitializer {

    @Autowired
    private VectorStore vectorStore;

    private static final String SUPPORT_NAMESPACE = "support-tickets";
    @PostConstruct
    public void init(){
        //addJobListings();
       // addTicketInfo();
       //addProductInfo();
  //     addLegalDocuments();

    }

    private void addTicketInfo() {
        TextReader supportReader = new TextReader(new ClassPathResource("support_tickets.txt"));

        TokenTextSplitter splitter = new TokenTextSplitter(
                100,
                100,
                5,
                1000,
                true
        );
        List<Document> supportDocuments = splitter.split(supportReader.get());
        // optional metadata
        supportDocuments.forEach(doc -> {
            doc.getMetadata().put("docType", "support");
            doc.getMetadata().put("category", "support");
        });
        vectorStore.add(supportDocuments);
    }

    private void addJobListings() {
        TextReader jobListReader =  new TextReader(new ClassPathResource("job_listings.txt"));
        TokenTextSplitter tokenTextSplitter  = new TokenTextSplitter(100,100,5,1000,
               true);
        List<Document> documents = tokenTextSplitter.split(jobListReader.get());
        vectorStore.add(documents);
    }

    private void addProductInfo() {
        TextReader productDataReader =  new TextReader(new ClassPathResource("product-data.txt"));
        TokenTextSplitter tokenTextSplitter  = new TokenTextSplitter(100,100,5,1000,
                true);
        List<Document> productDataDoc= tokenTextSplitter.split(productDataReader.get());
        // optional metadata
        productDataDoc.forEach(doc -> {
            doc.getMetadata().put("docType", "product");
            doc.getMetadata().put("category", "product");
        });
        vectorStore.add(productDataDoc);
    }


private void addLegalDocuments() {
    TextReader legalReader =  new TextReader(new ClassPathResource("Legal_Document_Analysis_Data.txt"));
    TokenTextSplitter tokenTextSplitter  = new TokenTextSplitter(100,100,5,1000,
            true);
    List<Document> legalDataDoc= tokenTextSplitter.split(legalReader.get());
    // optional metadata
    legalDataDoc.forEach(doc -> {
        doc.getMetadata().put("docType", "legalData");
        doc.getMetadata().put("category", "legalData");
    });
    vectorStore.add(legalDataDoc);
}
}

