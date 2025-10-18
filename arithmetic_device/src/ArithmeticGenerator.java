import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/*
  算术题生成器类，用于生成含整数和真分数的四则运算题（+、-、×、÷）
  支持题目去重、运算约束（减法结果非负、除法结果为真分数或整数），并将习题和答案输出到指定目录
 */
public class ArithmeticGenerator {
    // 数值范围：生成的整数、分数分子/分母均不超过此范围（整数为0~range-1，分母为2~range）
    private final int range;
    // 随机数生成器：用于随机生成操作符数量、数值、运算符
    private final Random random = new Random();
    // 存储已生成题目的规范化字符串：通过表达式等价性去重（如1+2与2+1视为同一题目）
    private final Set<String> generatedExpressions = new HashSet<>();
    // 习题与答案的输出目录路径：指定文件生成的位置
    private final String outputPath;

    // 构造算术题生成器
    public ArithmeticGenerator(int range, String outputPath) {
        this.range = range; //数值范围，控制生成的整数、分数分子/分母的最大值
        this.outputPath = outputPath; //输出目录路径，习题和答案文件将保存到该目录下
    }

    //核心方法：生成指定数量的算术题，并将习题和答案写入对应文件
    public void generate(int n) throws IOException {
        List<String> exercises = new ArrayList<>(); // 存储最终的习题列表（带"="）
        List<String> answers = new ArrayList<>();   // 存储对应习题的答案列表

        // 循环生成题目，直到达到指定数量（过滤无效/重复题目）
        while (exercises.size() < n) {
            // 随机生成操作符数量（1~3个，对应2~4个操作数）
            int operatorCount = random.nextInt(3) + 1;
            // 递归生成表达式树（可能返回null，代表表达式不符合约束）
            Expression expression = generateExpression(operatorCount);

            if (expression != null) {
                // 生成表达式的规范化字符串，用于判断是否重复
                String canonicalString = expression.toCanonicalString();
                // 若未重复，则添加到习题和答案列表
                if (!generatedExpressions.contains(canonicalString)) {
                    generatedExpressions.add(canonicalString);
                    exercises.add(expression.toString() + " ="); // 习题格式："表达式 ="
                    answers.add(expression.evaluate().toString()); // 答案为表达式计算结果
                }
            }
        }

        // 拼接习题和答案的完整文件路径（输出目录+文件名）
        String exercisesPath = Paths.get(outputPath, "Exercises.txt").toString();
        String answersPath = Paths.get(outputPath, "Answers.txt").toString();

        // 将习题和答案写入对应文件
        writeToFile(exercisesPath, exercises);
        writeToFile(answersPath, answers);
    }

    //递归生成表达式树（内部核心方法）
    private Expression generateExpression(int operatorCount) {
        // 操作符数量为0时，生成叶子节点（仅包含一个数值，无操作符）
        if (operatorCount == 0) {
            return new Expression(generateNumber());
        }

        // 拆分操作符到左右子表达式：左子树随机分配0~operatorCount-1个操作符，右子树分配剩余
        int leftOperatorCount = random.nextInt(operatorCount);
        int rightOperatorCount = operatorCount - 1 - leftOperatorCount;

        // 递归生成左右子表达式
        Expression left = generateExpression(leftOperatorCount);
        Expression right = generateExpression(rightOperatorCount);

        // 若任一子表达式无效（返回null），当前表达式也无效
        if (left == null || right == null) return null;

        // 随机选择一个运算符（+、-、×、÷）
        char op = getRandomOperator();

        // 为不同运算符添加约束条件，确保题目符合常见算术逻辑
        if (op == '-') {
            // 减法约束：确保结果非负（小学算术题常见要求）
            if (left.evaluate().compareTo(right.evaluate()) < 0) {
                // 若左值 < 右值，交换左右子表达式，保证结果≥0
                Expression temp = left;
                left = right;
                right = temp;
            }
        } else if (op == '÷') {
            // 除法约束：1. 除数不能为0；2. 结果为真分数或整数（控制题目难度）
            if (right.evaluate().numerator == 0) return null; // 除数为0，表达式无效
            Fraction result = left.evaluate().divide(right.evaluate());
            // 若结果是带分数（非真分数且非整数），表达式无效
            if (!result.isProperFraction() && result.denominator != 1) {
                return null;
            }
        }

        // 生成并返回当前内部节点（包含左右子表达式和运算符）
        return new Expression(left, right, op);
    }

    //生成随机数值（整数或真分数）
    private Fraction generateNumber() {
        if (random.nextBoolean()) {
            // 生成整数：范围为0 ~ range-1（random.nextInt(range)返回0到range-1的整数）
            return new Fraction(random.nextInt(range));
        } else {
            // 生成真分数：分母∈[2, range]，分子∈[1, 分母-1]（确保分子<分母，即真分数）
            int denominator = random.nextInt(range - 1) + 2; // 分母最小为2，避免分母为1（整数）
            int numerator = random.nextInt(denominator - 1) + 1; // 分子最小为1，最大为分母-1
            return new Fraction(numerator, denominator);
        }
    }

   //随机获取一个运算符（+、-、×、÷）
    private char getRandomOperator() {
        char[] ops = {'+', '-', '×', '÷'}; // 支持的四则运算符
        return ops[random.nextInt(ops.length)]; // 随机选择一个运算符
    }

    //将列表内容按序号写入指定文件（格式：1. 内容，2. 内容...）
    private void writeToFile(String filename, List<String> lines) throws IOException {
        // try-with-resources语法：自动关闭BufferedWriter，避免资源泄漏
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int i = 0; i < lines.size(); i++) {
                // 按"序号. 内容"格式写入（序号从1开始，符合题目编号习惯）
                writer.write((i + 1) + ". " + lines.get(i));
                writer.newLine(); // 换行，确保每道题/答案占一行
            }
        }
    }

    /*
      内部类：表达式树节点，用于递归表示算术表达式的结构
      支持两种节点类型：叶子节点（仅数值）、内部节点（含左右子表达式+运算符）
     */
    private static class Expression {
        Expression left, right; // 左右子表达式（内部节点非null，叶子节点为null）
        char operator;          // 运算符（内部节点非0，叶子节点为0），取值：+、-、×、÷
        Fraction value;         // 数值（叶子节点非null，内部节点为null）

        //构造叶子节点（仅包含数值，无操作符）

        Expression(Fraction value) {
            this.value = value;
        }

        //构造内部节点（包含左右子表达式和运算符）

        Expression(Expression left, Expression right, char operator) {
            this.left = left;//左子表达式
            this.right = right;//右子表达式
            this.operator = operator;//运算符
        }

        //递归计算表达式的值
        Fraction evaluate() {
            // 叶子节点：直接返回自身存储的数值
            if (value != null) {
                return value;
            }
            // 内部节点：先计算左右子表达式的值，再执行当前运算符的运算
            Fraction leftVal = left.evaluate();
            Fraction rightVal = right.evaluate();
            switch (operator) {
                case '+': return leftVal.add(rightVal);    // 加法运算
                case '-': return leftVal.subtract(rightVal); // 减法运算
                case '×': return leftVal.multiply(rightVal); // 乘法运算
                case '÷': return leftVal.divide(rightVal);   // 除法运算
                default: throw new IllegalStateException("Unknown operator: " + operator); // 未知运算符异常
            }
        }

        //生成表达式的字符串表示（自动添加必要括号，避免运算顺序歧义）
        @Override
        public String toString() {
            // 叶子节点：直接返回数值的字符串形式（如"3"、"1/2"）
            if (value != null) {
                return value.toString();
            }
            // 内部节点：先获取左右子表达式的字符串
            String leftStr = left.toString();
            String rightStr = right.toString();

            // 左子表达式优先级低于当前运算符：需加括号（避免运算顺序错误）
            if (left.operator != 0 && precedence(this.operator) > precedence(left.operator)) {
                leftStr = "( " + leftStr + " )";
            }
            // 右子表达式优先级低于或等于当前运算符：需加括号（减法/除法不满足交换律，需严格控制顺序）
            if (right.operator != 0 && precedence(this.operator) >= precedence(right.operator)) {
                rightStr = "( " + rightStr + " )";
            }

            // 拼接成"左表达式 运算符 右表达式"格式
            return leftStr + " " + operator + " " + rightStr;
        }

        /*
          生成表达式的规范化字符串（用于题目去重）
          核心逻辑：利用加法/乘法交换律，统一操作数顺序（如1+2与2+1生成同一字符串）
         */
        public String toCanonicalString() {
            // 叶子节点：直接返回数值字符串
            if (value != null) {
                return value.toString();
            }
            // 获取左右子表达式的规范化字符串
            String leftStr = left.toCanonicalString();
            String rightStr = right.toCanonicalString();

            // 加法（+）和乘法（×）满足交换律：按字典序排序操作数，避免等价表达式被判定为不同
            if ((operator == '+' || operator == '×') && leftStr.compareTo(rightStr) > 0) {
                String temp = leftStr;
                leftStr = rightStr;
                rightStr = temp;
            }

            // 用括号包裹，确保表达式结构唯一（如"(1+2)×3"与"1+(2×3)"区分）
            return "(" + leftStr + operator + rightStr + ")";
        }

        //获取运算符优先级（用于判断是否需要添加括号）
        private int precedence(char op) {
            switch (op) {
                case '+':
                case '-':
                    return 1; // 加减优先级低于乘除
                case '×':
                case '÷':
                    return 2; // 乘除优先级高于加减
                default:
                    return 0; // 数值节点无优先级
            }
        }
    }
}