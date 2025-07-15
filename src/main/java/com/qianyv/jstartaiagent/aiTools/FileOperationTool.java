package com.qianyv.jstartaiagent.aiTools;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.qianyv.jstartaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 读取或写入文件的工具类
 */
public class FileOperationTool {

    private String FILE_DIR = FileConstant.AI_TOOLS_FILE_PATH+"/file";

    @Tool(description = "读取文件")
    public String read(@ToolParam(description = "文件名称") String fileName){
        String path = FILE_DIR+"/"+fileName;

        try {
            return FileUtil.readUtf8String(path);
        } catch (IORuntimeException e) {
            return "读取文件失败";
        }
    }

    @Tool(description = "写入文件")
    public String write(@ToolParam(description = "文件名称") String fileName,
                        @ToolParam(description = "文件内容") String context){
        String path = FILE_DIR+"/"+fileName;

        try {
            FileUtil.mkdir(path);
            FileUtil.writeUtf8String(context, path);
            return "文件创建成功到路径："+path;
        } catch (IORuntimeException e) {
            return "文件创建失败";
        }
    }

}
