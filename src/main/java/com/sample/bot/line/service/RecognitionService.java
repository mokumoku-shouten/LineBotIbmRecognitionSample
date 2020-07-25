package com.sample.bot.line.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions.AcceptLanguage;

@Service
public class RecognitionService {

    @Value("${ibm.apiKey}")
    String apiKey;

    public String detect(InputStream is) throws IOException {
        // 一時ファイル作成
        final File tempFile = File.createTempFile("line",".jpg");
        tempFile.deleteOnExit();
        try (FileOutputStream os = new FileOutputStream(tempFile)) {
            StreamUtils.copy(is, os);
        }

        // IBM API
        VisualRecognition recognition = new VisualRecognition("2018-03-19", new IamOptions.Builder().apiKey(apiKey).build());

        // Visual Recognition
        ClassifyOptions classifyOptions = new ClassifyOptions.Builder()
                .imagesFile(tempFile)
                .acceptLanguage(AcceptLanguage.JA)
                .build();

        // 物体検出
        ClassifiedImages result = recognition.classify(classifyOptions).execute();

        List<ClassResult> classes = result.getImages().get(0).getClassifiers().get(0).getClasses();

        String res = "";
        for (ClassResult cls: classes) {
            res += cls.getClassName() + ": " + Math.round(cls.getScore()) + "%" + System.getProperty("line.separator");
        }
        return res;
    }

}