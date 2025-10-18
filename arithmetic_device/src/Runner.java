import java.io.File;
import java.io.IOException;

//程序主入口类，负责解析命令行参数、调度两种核心模式（题目生成模式/答案批改模式）
//并处理输出目录的创建，同时提供帮助信息指引用户正确使用参数

public class Runner {
    public static void main(String[] args) {
        int n = -1; // 题目数量（仅生成模式有效，初始值-1表示未指定）
        int r = -1; // 数值范围（仅生成模式有效，初始值-1表示未指定，控制生成数值的最大值）
        String exerciseFile = null; // 习题文件路径（仅批改模式有效，初始值null表示未指定）
        String answerFile = null;   // 答案文件路径（仅批改模式有效，初始值null表示未指定）
        String outputPath = ".";    // 输出目录路径，默认值为当前目录（"."表示当前工作目录）

        // 解析命令行参数：遍历参数数组，根据参数标识分配对应值
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-n": // 参数-n：指定生成模式的题目数量
                    if (i + 1 < args.length) { // 检查后续是否有参数值
                        try {
                            n = Integer.parseInt(args[++i]); // 解析为整数（i自增，指向参数值）
                        } catch (NumberFormatException e) { // 捕获非整数输入异常
                            System.err.println("错误：-n 参数需要一个有效的整数。");
                            printHelp(); // 打印帮助信息
                            return; // 异常退出，终止程序
                        }
                    }
                    break;
                case "-r": // 参数-r：指定生成模式的数值范围（生成数值小于此值）
                    if (i + 1 < args.length) {
                        try {
                            r = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("错误：-r 参数需要一个有效的整数。");
                            printHelp();
                            return;
                        }
                    }
                    break;
                case "-e": // 参数-e：指定批改模式的习题文件路径
                    if (i + 1 < args.length) {
                        exerciseFile = args[++i]; // 直接赋值文件路径字符串
                    }
                    break;
                case "-a": // 参数-a：指定批改模式的答案文件路径
                    if (i + 1 < args.length) {
                        answerFile = args[++i];
                    }
                    break;
                case "-o": // 参数-o：指定输出目录路径（可选，默认当前目录）
                    if (i + 1 < args.length) {
                        outputPath = args[++i];
                    }
                    break;
                default: // 未知参数：遇到未定义的参数标识
                    System.err.println("未知参数: " + args[i]);
                    printHelp();
                    return;
            }
        }

        try {
            // 处理输出目录：检查目录是否存在，不存在则创建（包括多级目录）
            File outputDir = new File(outputPath);
            if (!outputDir.exists()) {
                System.out.println("输出目录未找到，正在创建: " + outputPath);
                outputDir.mkdirs(); // mkdirs()创建多级目录，mkdir()仅创建单级目录
            }

            // 分支1：生成题目模式（需同时指定-n和-r参数，且r>1）
            if (n != -1 && r != -1) {
                if (r <= 1) { // 数值范围必须大于1（避免分母为1或0，符合真分数生成逻辑）
                    System.err.println("错误：-r 参数必须是大于1的自然数。");
                    printHelp();
                    return;
                }
                System.out.println("正在生成 " + n + " 道题目，数值范围为 " + r + "...");
                // 创建题目生成器实例，传入数值范围和输出目录
                ArithmeticGenerator generator = new ArithmeticGenerator(r, outputPath);
                generator.generate(n); // 调用生成方法，生成n道题目
                System.out.println("生成完毕。文件已在 '" + outputPath + "' 目录中创建。");

                // 分支2：答案批改模式（需同时指定-e和-a参数）
            } else if (exerciseFile != null && answerFile != null) {
                System.out.println("正在批改作业...");
                // 创建批改器实例，传入输出目录（评分文件Grade.txt将存于此目录）
                Grader grader = new Grader(outputPath);
                grader.grade(exerciseFile, answerFile); // 调用批改方法，比对习题与答案
                System.out.println("批改完毕。'Grade.txt' 文件已在 '" + outputPath + "' 目录中创建。");

                // 分支3：参数不完整（未满足任一模式的参数要求）
            } else {
                printHelp(); // 打印帮助信息，指导用户正确传参
            }
        } catch (IOException e) { // 捕获IO异常（如文件读写失败、目录创建权限不足等）
            System.err.println("发生IO错误: " + e.getMessage());
            e.printStackTrace(); // 打印异常堆栈信息，便于调试
        } catch (Exception e) { // 捕获其他未知异常（兜底处理）
            System.err.println("发生未知错误: " + e.getMessage());
            e.printStackTrace();
        }
    }


    //打印程序使用帮助信息，包括两种模式的命令格式、示例及参数说明
    private static void printHelp() {
        System.out.println("\n--- 帮助信息 ---");
        System.out.println("用法: java -jar MyApp.jar [模式] [参数]");
        System.out.println("\n模式一：生成题目与答案");
        System.out.println("  java -jar MyApp.jar -n <题目数量> -r <数值范围> [-o <输出路径>]");
        System.out.println("  示例: java -jar MyApp.jar -n 10 -r 10 -o ./output\n");
        System.out.println("模式二：检查答案对错");
        System.out.println("  java -jar MyApp.jar -e <题目文件> -a <答案文件> [-o <输出路径>]");
        System.out.println("  示例: java -jar MyApp.jar -e Exercises.txt -a Answers.txt -o ./output\n");
        System.out.println("参数说明:");
        System.out.println("  -n : 需要生成的题目数量 (自然数)。");
        System.out.println("  -r : 题目中数值的范围，小于此值 (需 > 1)。");
        System.out.println("  -e : 【模式二】指定的题目文件路径。");
        System.out.println("  -a : 【模式二】指定的答案文件路径。");
        System.out.println("  -o : (可选) 指定输出文件的存放目录，默认为当前目录。");
    }
}