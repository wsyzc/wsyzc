// 相似度计算类
public class Similar {
    //计算两个字符串的编辑距离
    public int calculateEditDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        // 创建动态规划表
        int[][] dp = new int[m + 1][n + 1];
        // 初始化边界：空字符串到长度i/j的编辑距离为i/j
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        // 填充动态规划表
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                // 若当前字符相同，无需修改；否则需要1次替换操作
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                // 取三种操作（删除、插入、替换）的最小值
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1,    // 删除s1的第i个字符
                                dp[i][j - 1] + 1),   // 插入s2的第j个字符
                        dp[i - 1][j - 1] + cost       // 替换（或匹配）
                );
            }
        }
        return dp[m][n];
    }
    //将编辑距离转换为相似度（0~1之间，值越大越相似）
    public double getSimilarity(String s1, String s2) {
        if (s1.isEmpty() && s2.isEmpty()) {
            return 1.0; // 两个空字符串视为完全相似
        }
        int editDistance = calculateEditDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        // 相似度 = 1 - （编辑距离 / 最长字符串长度）
        return 1.0 - (double) editDistance / maxLength;
    }
}

