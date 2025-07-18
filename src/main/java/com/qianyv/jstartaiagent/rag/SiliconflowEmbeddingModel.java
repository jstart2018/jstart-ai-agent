package com.qianyv.jstartaiagent.rag;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SiliconflowEmbeddingModel extends AbstractEmbeddingModel {

    private static final String API_URL = "https://api.siliconflow.cn/v1/embeddings";

    private final String apiToken = "sk-ungdjmzrwyneukkjihwvhcspqwoplfvebolaohytwykggmja"; // 替换为实际Token

    /**
     * call方法负责执行具体的嵌入逻辑，可以自定义了，这里call方法直接发送http请求到Siliconflow的API
     */
    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        // 构建请求JSON
        JSONObject requestJson = new JSONObject();
        requestJson.put("model", "BAAI/bge-m3");
        requestJson.put("input", request.getInstructions());

        // 发送HTTP请求
        HttpResponse response = null;
        try {
            response = HttpRequest.post(API_URL)
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Content-Type", "application/json")
                    .body(requestJson.toString())
                    .execute();
        } catch (Exception e) {
            log.error("连接硅基流动Embedding超时或发生错误");
            throw new RuntimeException(e);
        }finally{
            if (response != null) {
                response.close(); // 确保响应被关闭
            }
        }

        // 处理不同状态码的响应
        int status = response.getStatus();
        String responseBody = response.body();

        ApiResponse result;
        if (status == 200) {
            result = JSONUtil.toBean(responseBody, ApiResponse.class);
        } else if (status == 400) {
            ErrorResponse error = JSONUtil.toBean(responseBody, ErrorResponse.class);
            throw new ApiException(error.getCode(), error.getMessage(), status);
        } else {
            // 其他状态码返回原始字符串
            throw new ApiException(4000, responseBody,status);
        }
        return convertToEmbeddingResponse(result);

    }

    @Override
    public float[] embed(Document document) {
        Assert.notNull(document, "Document must not be null");

        //EMBED模式下获取内容，该模式下会处理文档的元数据过滤，过滤的方法应该是Document类中已经写好了。我们只需要获取过滤后文档的内容即可
        String text = document.getFormattedContent(MetadataMode.EMBED);

         //调用父类的embed方法，传入处理后的文本内容，父类的embed方法会调用call方法
         //call方法是一个抽象方法，需要在子类中实现
         return this.embed(text);


    }


    private EmbeddingResponse convertToEmbeddingResponse(ApiResponse apiResponse) {
        List<Embedding> embeddings = apiResponse.getData().stream()
                .map(dataItem -> {
                    // 创建Embedding对象，包含向量和索引
                    return new Embedding( dataItem.getEmbedding(), dataItem.getIndex());
                })
                .collect(Collectors.toList());

        // 从API响应中提取元数据
        DefaultUsage defaultUsage = new DefaultUsage(
                apiResponse.getUsage().getPrompt_tokens(),
                apiResponse.getUsage().getCompletion_tokens(),
                apiResponse.getUsage().getTotal_tokens()
        );
        EmbeddingResponseMetadata metadata = new EmbeddingResponseMetadata(
                apiResponse.getModel(),defaultUsage);

        return new EmbeddingResponse(embeddings, metadata);
    }


    // 成功响应实体
    public static class ApiResponse {
        private String model;
        private List<DataItem> data;
        private Usage usage;

        // Getters and setters
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public List<DataItem> getData() { return data; }
        public void setData(List<DataItem> data) { this.data = data; }
        public Usage getUsage() { return usage; }
        public void setUsage(Usage usage) { this.usage = usage; }
    }


    // 数据项实体
    public static class DataItem {
        private String object;
        private float[] embedding;
        private int index;

        // Getters and setters
        public String getObject() { return object; }
        public void setObject(String object) { this.object = object; }
        public float[] getEmbedding() { return embedding; }
        public void setEmbedding(float[] embedding) { this.embedding = embedding; }
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
    }

    // 使用统计实体
    public static class Usage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;

        // Getters and setters
        public Integer getPrompt_tokens() { return prompt_tokens; }
        public void setPrompt_tokens(Integer prompt_tokens) { this.prompt_tokens = prompt_tokens; }
        public Integer getCompletion_tokens() { return completion_tokens; }
        public void setCompletion_tokens(Integer completion_tokens) { this.completion_tokens = completion_tokens; }
        public Integer getTotal_tokens() { return total_tokens; }
        public void setTotal_tokens(Integer total_tokens) { this.total_tokens = total_tokens; }
    }

    // 错误响应实体
    public static class ErrorResponse  {
        private int code;
        private String message;
        private String data;

        // Getters and setters
        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    // 字符串响应实体
    public static class StringResponse {
        private final String content;
        private final int statusCode;

        public StringResponse(String content, int statusCode) {
            this.content = content;
            this.statusCode = statusCode;
        }

        public String getContent() { return content; }
        public int getStatusCode() { return statusCode; }
    }

    // 自定义异常类
    public static class ApiException extends RuntimeException {
        private final int errorCode;
        private final int httpStatus;

        public ApiException(int errorCode, String message, int httpStatus) {
            super(message);
            this.errorCode = errorCode;
            this.httpStatus = httpStatus;
        }

        public int getErrorCode() { return errorCode; }
        public int getHttpStatus() { return httpStatus; }
    }






}
