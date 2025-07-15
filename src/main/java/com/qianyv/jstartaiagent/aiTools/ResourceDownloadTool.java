package com.qianyv.jstartaiagent.aiTools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.qianyv.jstartaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

/**
 * 资源下载
 * ResourceDownloadTool 类提供了一个工具方法，用于从指定 URL 下载资源并保存到本地。
 * 该类使用 Spring AI 的 @Tool 注解来标记其功能。
 */
public class ResourceDownloadTool {

    @Tool(description = "Download a resource from a given URL")
    public String downloadResource(@ToolParam(description = "URL of the resource to download") String url, @ToolParam(description = "Name of the file to save the downloaded resource") String fileName) {
        String fileDir = FileConstant.AI_TOOLS_FILE_PATH + "/download";
        String filePath = fileDir + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 使用 Hutool 的 downloadFile 方法下载资源
            HttpUtil.downloadFile(url, new File(filePath));
            return "Resource downloaded successfully to: " + filePath;
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }
}
