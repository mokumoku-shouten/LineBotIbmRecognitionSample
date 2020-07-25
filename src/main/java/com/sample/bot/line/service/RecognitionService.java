package com.sample.bot.line.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.visual_recognition.v3.model.ClassResult;
import com.ibm.watson.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.visual_recognition.v3.model.ClassifyOptions;
import com.ibm.watson.visual_recognition.v3.VisualRecognition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class RecognitionService {

    @Value("${ibm.apiKey}")
    String apiKey;

    @Value("${ibm.url}")
    String url;

    public String detect(InputStream is) throws IOException {
        // 一時ファイル作成
        final File tempFile = File.createTempFile("line",".jpg");
        tempFile.deleteOnExit();
        try (FileOutputStream os = new FileOutputStream(tempFile)) {
            StreamUtils.copy(is, os);
        }

        // IBM API
        IamAuthenticator authenticator = new IamAuthenticator(apiKey);
        VisualRecognition visualRecognition = new VisualRecognition("2018-03-19", authenticator);
        visualRecognition.setServiceUrl(url);

        // Visual Recognition
        ClassifyOptions classifyOptions = new ClassifyOptions.Builder()
                .imagesFile(tempFile)
                .acceptLanguage(ClassifyOptions.AcceptLanguage.JA)
                .build();

        // 物体検出
        ClassifiedImages result = visualRecognition.classify(classifyOptions).execute().getResult();

        List<ClassResult> classes = result.getImages().get(0).getClassifiers().get(0).getClasses();

        String res = "";
        for (ClassResult cls: classes) {
            res += cls.getXClass() + ": " + Math.round(cls.getScore()) + "%" + System.getProperty("line.separator");
        }
        return res;
    }

}