package com.miao.ai_gen_web.ai.model;

import jdk.jfr.Description;
import lombok.Data;

@Description("生成 HTML 代码文件的结果")
@Data
public class HtmlCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("生成代码的描述")
    private String description;
}
