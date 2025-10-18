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
  支持处理整数、真分数、带分数的四则运算题，通过双栈法计算表达式结果确保运算逻辑准确
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

        int count = Math.min(exercises.size(), userAnswers.size()); // 取习题和答案的最小数量

        // 逐题批改
        for (int i = 0; i < count; i++) {
            // 提取习题表达式：分割编号、去除" = "，得到纯表达式
            String exercise = exercises.get(i).split("\\. ")[1].replace(" =", "").trim();
            // 提取用户答案：分割编号，得到纯答案字符串
            String userAnswerStr = userAnswers.get(i).split("\\. ")[1].trim();

            try {
                Fraction correctAnswer = evaluateExpression(exercise); // 计算习题的正确结果
                Fraction userAnswer = parseFraction(userAnswerStr);   // 解析用户答案为分数

                // 比对结果，记录题目序号
                if (correctAnswer.equals(userAnswer)) {
                    correctIndices.add(i + 1);
                } else {
                    wrongIndices.add(i + 1);
                }
            } catch (Exception e) {
                // 解析/计算异常的题目判定为错误
                wrongIndices.add(i + 1);
            }
        }

        writeGradeFile(correctIndices, wrongIndices); // 生成评分文件
    }

    //解析分数字符串为Fraction对象，支持带分数、真分数、整数三种格式
    private Fraction parseFraction(String s) {
        if (s.contains("'")) { // 处理带分数格式（如"2'1/3"）
            String[] parts = s.split("'");
            long integerPart = Long.parseLong(parts[0]);
            String[] fractionParts = parts[1].split("/");
            long numerator = Long.parseLong(fractionParts[0]);
            long denominator = Long.parseLong(fractionParts[1]);
            return new Fraction(integerPart * denominator + numerator, denominator);
        } else if (s.contains("/")) { // 处理真分数格式（如"1/2"）
            String[] parts = s.split("/");
            long numerator = Long.parseLong(parts[0]);
            long denominator = Long.parseLong(parts[1]);
            return new Fraction(numerator, denominator);
        } else { // 处理整数格式（如"3"）
            return new Fraction(Long.parseLong(s));
        }
    }

    //读取文件的所有行，返回字符串列表
    private List<String> readLines(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }


    //生成评分文件（Grade.txt），写入做对和做错的题目数量及序号
    private void writeGradeFile(List<Integer> correct, List<Integer> wrong) throws IOException {
        String gradeFilePath = Paths.get(outputPath, "Grade.txt").toString();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(gradeFilePath))) {
            writer.write("Correct: " + correct.size() + " " + formatIndices(correct));
            writer.newLine();
            writer.write("Wrong: " + wrong.size() + " " + formatIndices(wrong));
            writer.newLine();
        }
    }

    //将题目序号列表格式化为字符串，格式为"(序号1, 序号2, ..., 序号n)"
    private String formatIndices(List<Integer> indices) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < indices.size(); i++) {
            sb.append(indices.get(i));
            if (i < indices.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    //使用双栈法计算中缀表达式的值（支持整数、分数、括号及四则运算）
    //双栈逻辑：values栈存操作数（Fraction），ops栈存运算符（char），按优先级处理运算
    public Fraction evaluateExpression(String expression) {
        Stack<Fraction> values = new Stack<>(); // 存储操作数的栈
        Stack<Character> ops = new Stack<>();   // 存储运算符的栈
        String[] tokens = expression.split(" "); // 按空格分割表达式为token

        // 遍历每个token，处理操作数、括号、运算符
        for (String token : tokens) {
            if (token.isEmpty()) continue;

            if (isNumber(token)) { // 若token是数字，解析为Fraction并入栈
                values.push(parseFraction(token));
            } else if (token.equals("(")) { // 左括号直接入运算符栈
                ops.push('(');
            } else if (token.equals(")")) { // 右括号：计算括号内的表达式
                while (ops.peek() != '(') {
                    Fraction b = values.pop();
                    Fraction a = values.pop();
                    char op = ops.pop();
                    values.push(applyOp(op, b, a));
                }
                ops.pop();
            } else { // 运算符：处理优先级后入栈
                while (!ops.empty() && hasPrecedence(token.charAt(0), ops.peek())) {
                    Fraction b = values.pop();
                    Fraction a = values.pop();
                    char op = ops.pop();
                    values.push(applyOp(op, b, a));
                }
                ops.push(token.charAt(0));
            }
        }

        // 处理栈中剩余的运算符和操作数
        while (!ops.empty()) {
            Fraction b = values.pop();
            Fraction a = values.pop();
            char op = ops.pop();
            values.push(applyOp(op, b, a));
        }

        return values.pop(); // 操作数栈顶即为最终结果
    }

    //判断token是否为数字（支持整数、真分数、带分数的首字符特征）
    private boolean isNumber(String token) {
        return Character.isDigit(token.charAt(0));
    }

    //获取运算符的优先级（×、÷优先级高于+、-）
    private int getPrecedence(char op) {
        switch (op) {
            case '+':
            case '-':
                return 1;
            case '×':
            case '÷':
                return 2;
            default:
                return 0;
        }
    }

    // 判断两个运算符的优先级：当前运算符op1的优先级是否≤栈顶运算符op2的优先级

    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        return getPrecedence(op2) >= getPrecedence(op1);
    }

    //用运算符计算两个操作数的结果（注意操作数顺序：a是左操作数，b是右操作数）
    private Fraction applyOp(char op, Fraction b, Fraction a) {
        switch (op) {
            case '+': return a.add(b);
            case '-': return a.subtract(b);
            case '×': return a.multiply(b);
            case '÷': return a.divide(b);
        }
        return null;
    }
}