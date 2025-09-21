import java.util.regex.Pattern;

// 文本预处理
public class TextProcess {

    // 正则表达式：匹配标点符号、特殊字符和空白字符（保留中文字符、字母、数字等）
    // 包含：中英文标点、空格、制表符、换行符等
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile(
            "[\\p{P}\\s\\uFF01-\\uFF5E\\u2000-\\u206F\\u3000-\\u303F]"
    );

    public String preprocess(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        // 替换匹配到的字符为空字符串（即去除）
        return PUNCTUATION_PATTERN.matcher(text).replaceAll("");
    }
}
