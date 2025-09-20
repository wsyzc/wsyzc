import java.util.regex.Matcher;
import java.util.regex.Pattern;

//文本预处理
public class TextProcess {

    // 正则表达式，用于提取中文字符
    private static final Pattern CHINESE_CHAR_PATTERN = Pattern.compile("[\u4e00-\u9fa5]");
    //对文本进行预处理，去除标点符号和特殊字符
    public String preprocess(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        // 去除所有非中文字符
        StringBuilder processed = new StringBuilder();
        Matcher matcher = CHINESE_CHAR_PATTERN.matcher(text);
        while (matcher.find()) {
            processed.append(matcher.group());
        }
        return processed.toString();
    }
}

