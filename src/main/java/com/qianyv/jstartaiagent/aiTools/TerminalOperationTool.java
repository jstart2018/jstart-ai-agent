package com.qianyv.jstartaiagent.aiTools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 终端操作
 * TerminalOperationTool 类提供了一个工具方法，用于在终端执行命令并返回输出结果。
 * 该类使用 Spring AI 的 @Tool 注解来标记其功能。
 */
public class TerminalOperationTool {
    @Tool(description = "Execute a command in the terminal")
    public String executeTerminalCommand(@ToolParam(description = "Command to execute in the terminal") String command) {
        StringBuilder output = new StringBuilder();
        try {

            //如果是windows系统要使用cmd.exe /c来执行命令
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            //如果是Linux或Mac系统可以直接使用command
//            Process process = Runtime.getRuntime().exec(command);

            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Command execution failed with exit code: ").append(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            output.append("Error executing command: ").append(e.getMessage());
        }
        return output.toString();
    }
}
