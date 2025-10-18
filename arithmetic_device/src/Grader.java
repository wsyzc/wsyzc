import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/*
  算术题批改器类，用于比对习题文件与用户答案文件的正确性，生成包含批改结果的评分文件
  支持处理整数、真分数、带分数的四则运算题，通过双栈法计算表达式结果，确保运算逻辑准确
*/

public class Grader {

    // 评分文件（Grade.txt）的输出目录路径，指定批改结果的保存位置
    private final String outputPath;


    //构造批改器，初始化评分文件的输出路径
    public Grader(String outputPath) {
        this.outputPath = outputPath;//评分文件的输出目录，Grade.txt将保存到该目录下
    }


    //核心批改方法：读取习题与用户答案，逐题比对正确性，生成评分文件
    public void grade(String exerciseFile, String answerFile) throws IOException {
        List<String> exercises = readLines(exerciseFile); // 读取所有习题行
        List<String> userAnswers = readLines(answerFile); // 读取所有用户答案行

        List<Integer> correctIndices = new ArrayList<>(); // 存储做对的题目序号（从1开始）
        List<Integer> wrongIndices = new ArrayList<>();   // 存储做错的题目序号（从1开始）

        // 取习题和答案的最小数量，避免下标越界（习题与答案数量不匹配时，仅批改到较少的数量）
        int count = Math.min(exercises.size(), userAnswers.size());

        // 逐题批改
        for (int i = 0; i < count; i++) {
            // 提取习题表达式：分割编号（如"1. "）、去除" = "，得到纯表达式（如"3 + 1/2"）
            String exercise = exercises.get(i).split("\\. ")[1].replace(" =", "").trim();
            // 提取用户答案：分割编号，得到纯答案字符串（如"3'1/2"、"7/2"、"3"）
            String userAnswerStr = userAnswers.get(i).split("\\. ")[1].trim();

            try {
                // 计算习题的正确结果（通过双栈法解析表达式）
                Fraction correctAnswer = evaluateExpression(exercise);
                // 解析用户答案为Fraction对象（支持整数、真分数、带分数格式）
                Fraction userAnswer = parseFraction(userAnswerStr);

                // 比对正确结果与用户答案，记录序号
                if (correctAnswer.equals(userAnswer)) {
                    correctIndices.add(i + 1);
                } else {
                    wrongIndices.add(i + 1);
                }
            } catch (Exception e) {
                // 捕获解析/计算异常（如表达式格式错误、用户答案格式错误），此类题目判定为错
                wrongIndices.add(i + 1);
            }
        }

        // 生成评分文件（包含做对/做错的题目数量和序号）
        writeGradeFile(correctIndices, wrongIndices);
    }


    //解析分数字符串为Fraction对象，支持三种格式：带分数（如"2'1/3"）、真分数（如"1/2"）、整数（如"3"）
    private Fraction parseFraction(String s) {
        if (s.contains("'")) { // 处理带分数格式（整数部分'分数部分，如"2'1/3"）
            String[] parts = s.split("'"); // 分割为整数部分（parts[0]）和分数部分（parts[1]）
            long integerPart = Long.parseLong(parts[0]);
            String[] fractionParts = parts[1].split("/"); // 分割分数部分的分子和分母
            long numerator = Long.parseLong(fractionParts[0]);
            long denominator = Long.parseLong(fractionParts[1]);
            // 转换为假分数：整数部分×分母 + 分子 作为新分子，保持原分母
            return new Fraction(integerPart * denominator + numerator, denominator);
        } else if (s.contains("/")) { // 处理真分数格式（如"1/2"）
            String[] parts = s.split("/"); // 分割分子和分母
            long numerator = Long.parseLong(parts[0]);
            long denominator = Long.parseLong(parts[1]);
            return new Fraction(numerator, denominator);
        } else { // 处理整数格式（如"3"，默认分母为1）
            return new Fraction(Long.parseLong(s));
        }
    }


    //读取文件的所有行，返回字符串列表（每行对应一个元素）
    private List<String> readLines(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        // try-with-resources语法：自动关闭BufferedReader，避免资源泄漏
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            // 逐行读取，直到文件末尾（readLine()返回null表示结束）
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }


    //生成评分文件（Grade.txt），写入做对和做错的题目数量及序号
    private void writeGradeFile(List<Integer> correct, List<Integer> wrong) throws IOException {
        // 拼接评分文件的完整路径（输出目录+Grade.txt）
        String gradeFilePath = Paths.get(outputPath, "Grade.txt").toString();

        // try-with-resources语法：自动关闭BufferedWriter
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(gradeFilePath))) {
            // 写入做对的题目信息：格式为"Correct: 数量 (序号1, 序号2...)"
            writer.write("Correct: " + correct.size() + " " + formatIndices(correct));
            writer.newLine(); // 换行
            // 写入做错的题目信息：格式为"Wrong: 数量 (序号1, 序号2...)"
            writer.write("Wrong: " + wrong.size() + " " + formatIndices(wrong));
            writer.newLine();
        }
    }


    //将题目序号列表格式化为字符串，格式为"(序号1, 序号2, ..., 序号n)"
    private String formatIndices(List<Integer> indices) {
        StringBuilder sb = new StringBuilder("("); // 初始化字符串构建器，开头添加"("
        for (int i = 0; i < indices.size(); i++) {
            sb.append(indices.get(i)); // 添加当前序号
            // 非最后一个元素，添加", "分隔
            if (i < indices.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")"); // 结尾添加")"
        return sb.toString();
    }


    //使用双栈法计算中缀表达式的值（支持整数、分数、括号及四则运算）
    //双栈逻辑：values栈存操作数（Fraction），ops栈存运算符（char），按优先级处理运算
    public Fraction evaluateExpression(String expression) {
        Stack<Fraction> values = new Stack<>(); // 存储操作数的栈
        Stack<Character> ops = new Stack<>();   // 存储运算符的栈
        String[] tokens = expression.split(" "); // 按空格分割表达式为token（如["(", "3", "+", "1/2", ")"]）

        // 遍历每个token，处理操作数、括号、运算符
        for (String token : tokens) {
            if (token.isEmpty()) continue; // 跳过空token（避免分割异常导致的空值）

            if (isNumber(token)) { // 若token是数字（整数/分数），解析为Fraction并入栈
                values.push(parseFraction(token));
            } else if (token.equals("(")) { // 若token是左括号，直接入运算符栈
                ops.push('(');
            } else if (token.equals(")")) { // 若token是右括号，计算括号内的表达式
                // 弹出运算符并计算，直到遇到左括号
                while (ops.peek() != '(') {
                    // 弹出栈顶运算符和两个操作数（注意：栈弹出顺序是右操作数先出，左操作数后出）
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.pop(); // 弹出左括号（括号内计算完成，左括号无需保留）
            } else { // 若token是运算符（+、-、×、÷）
                // 处理优先级：若栈顶运算符优先级≥当前运算符，先计算栈顶运算
                while (!ops.empty() && hasPrecedence(token.charAt(0), ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(token.charAt(0)); // 当前运算符入栈
            }
        }

        // 处理栈中剩余的运算符和操作数（此时无括号，按优先级顺序计算）
        while (!ops.empty()) {
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        }

        return values.pop(); // 操作数栈顶即为最终结果
    }


    // 判断token是否为数字（支持整数、真分数、带分数，如"3"、"1/2"、"2'1/3"）
    private boolean isNumber(String token) {
        // 改进判断逻辑：数字类token的首字符必为数字（覆盖整数、分数、带分数的首字符特征）
        return Character.isDigit(token.charAt(0));
    }

    /*
     判断两个运算符的优先级：当前运算符op1的优先级是否≤栈顶运算符op2的优先级
     优先级规则：×、÷ 优先级高于 +、-；括号优先级特殊（单独处理）
     */
    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false; // 栈顶是括号，当前运算符直接入栈（括号内运算单独处理）
        }
        // 仅当op1是×/÷且op2是+/−时，op1优先级更高，返回false（无需先算op2）；其他情况返回true
        if ((op1 == '×' || op1 == '÷') && (op2 == '+' || op2 == '-')) {
            return false;
        }
        return true;
    }

    //应用运算符计算两个操作数的结果（注意操作数顺序：a是左操作数，b是右操作数），
    private Fraction applyOp(char op, Fraction b, Fraction a) {
        switch (op) {
            case '+': return a.add(b);    // 加法：左操作数 + 右操作数
            case '-': return a.subtract(b); // 减法：左操作数 - 右操作数
            case '×': return a.multiply(b); // 乘法：左操作数 × 右操作数
            case '÷': return a.divide(b);   // 除法：左操作数 ÷ 右操作数
        }
        return null; // 理论上不会走到此处（运算符仅支持上述四种）
    }
}
