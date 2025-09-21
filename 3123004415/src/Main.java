import java.io.IOException;
import java.text.DecimalFormat;

//主程序入口类，协调各组件完成论文查重流程
public class Main {
    public static void main(String[] args) {
        // 检查命令行参数是否正确（需要3个参数：原文路径、抄袭版路径、输出路径）
        if (args.length != 3) {
            System.err.println("用法: java Main <原文文件绝对路径> <抄袭版文件绝对路径> <输出结果文件绝对路径>");
            System.exit(1);
        }
        // 解析命令行参数
        String origPath = args[0];
        String origAddPath = args[1];
        String outputPath = args[2];
        // 初始化各组件
        FileIO fileIO = new FileIO();
        TextProcess textProcess = new TextProcess();
        Similar similar = new Similar();

        try {
            // 1. 读取文件内容
            String origContent = fileIO.read(origPath);
            String origAddContent = fileIO.read(origAddPath);
            // 2. 文本预处理
            String processedOrig = textProcess.preprocess(origContent);
            String processedOrigAdd = textProcess.preprocess(origAddContent);
            // 3. 计算相似度（重复率）
            double similarity = similar.getSimilarity(processedOrig, processedOrigAdd);
            // 4. 格式化结果（保留两位小数）
            DecimalFormat df = new DecimalFormat("#.##");
            String result = df.format(similarity);
            // 5. 写入结果文件
            fileIO.write(outputPath, result);
            System.out.println("查重完成，结果已写入: " + outputPath);
            System.out.println("重复率: " + result);
        } catch (IOException e) {
            System.err.println("文件操作错误: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
