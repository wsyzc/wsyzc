import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CheckTest {
    // 测试类
    private final Similar similar = new Similar();
    private final TextProcess textProcess = new TextProcess();

    // 预处理文本
    private double getProcessedSimilarity(String text1, String text2) {
        String p1 = textProcess.preprocess(text1);
        String p2 = textProcess.preprocess(text2);
        return similar.getSimilarity(p1, p2);
    }

    // 1. 完全相同的中文文本（无特殊格式）
    @Test
    void testIdenticalChineseTexts() {
        String text1 = "人工智能在医疗领域的应用越来越广泛，尤其是在疾病诊断方面。";
        String text2 = "人工智能在医疗领域的应用越来越广泛，尤其是在疾病诊断方面。";
        double similarity = getProcessedSimilarity(text1, text2);
        assertEquals(1.0, similarity, 0.01, "完全相同文本预处理后应完全匹配");
    }

    // 2. 完全不同的中文文本
    @Test
    void testCompletelyDifferentChineseTexts() {
        String text1 = "计算机网络的拓扑结构包括总线型、星型和环型。";
        String text2 = "李白是唐代著名诗人，代表作有《静夜思》《望庐山瀑布》。";
        double similarity = getProcessedSimilarity(text1, text2);
        assertTrue(similarity < 0.1, "完全不同文本相似度应接近0（编辑距离特性）");
    }

    // 3. 文本A为空，文本B正常
    @Test
    void testChineseText1Empty() {
        String text1 = "";
        String text2 = "这是一段正常的中文文本。";
        double similarity = getProcessedSimilarity(text1, text2);
        assertEquals(0.0, similarity, 0.01, "空文本与非空文本相似度应为0");
    }

    // 4. 包含中文特殊标点（验证预处理逻辑）
    @Test
    void testChinesePunctuation() {
        String text1 = "中文标点包括：逗号（，）、句号（。）、书名号（《》）、顿号（、）等！";
        String text2 = "中文标点包括逗号句号书名号顿号等"; // 手动去除标点后与text1预处理结果一致
        double similarity = getProcessedSimilarity(text1, text2);
        assertEquals(1.0, similarity, 0.01, "预处理去除标点后应完全匹配");
    }

    // 5. 纯英文测试：完全相同
    @Test
    void testPureEnglishIdentical() {
        String text1 = "Artificial intelligence applications are growing rapidly in various fields.";
        String text2 = "Artificial intelligence applications are growing rapidly in various fields.";
        double similarity = getProcessedSimilarity(text1, text2);
        assertEquals(1.0, similarity, 0.01, "完全相同的纯英文文本相似度应为100%");
    }

    // 6. 纯英文测试：完全不同（主题无关）
    @Test
    void testPureEnglishDifferent() {
        String text1 = "The quick brown fox jumps over the lazy dog.";
        String text2 = "Astronomy studies celestial objects and phenomena beyond Earth's atmosphere.";
        double similarity = getProcessedSimilarity(text1, text2);
        assertTrue(similarity < 0.3, "完全不同的纯英文文本相似度应接近0");
    }

    // 7. 英文数字结合测试：部分元素相同（非前后重复）
    @Test
    void testMixedPartialElements() {
        String text1 = "Order77 has productA, price $99, date 2024-05-10.";
        String text2 = "Order77 has productB, price $88, date 2024-06-15."; // 仅订单号和年份相同
        double similarity = getProcessedSimilarity(text1, text2);
    }

    // 8. 英文数字结合测试：格式差异（标点/空格）
    @Test
    void testMixedFormatVariations() {
        String text1 = "Code: ABC123; Value=45.67, Time:10:30";
        String text2 = "Code ABC123 Value 4567 Time 1030"; // 去除标点后部分匹配
        double similarity = getProcessedSimilarity(text1, text2);
        assertTrue(similarity > 0.8, "格式差异但核心内容部分相同，相似度应>80%");
    }

    // 9. 中文分词边界测试（预处理去除空格后匹配）
    @Test
    void testChineseWordSegmentation() {
        String text1 = "南京市长江大桥";
        String text2 = "南京市长 江大桥"; // 预处理会去除空格，变为"南京市长江大桥"
        double similarity = getProcessedSimilarity(text1, text2);
        // 编辑距离计算"南京市长江大桥"与"南京市长江大桥"的差异（实际是相同的）
        assertEquals(1.0, similarity, 0.01, "预处理去除空格后应完全匹配");
    }

    // 10. 中英文混合文本（现有代码仅去除标点，不处理繁简）
    @Test
    void testMixedChineseAndEnglish() {
        String text1 = "Java语言是一种跨平台的编程语言，常用于开发企业级应用。";
        String text2 = "Java语言是一种跨平台的编程语言，常用于开发企业级应用。"; // 与text1完全相同（无繁简差异）
        double similarity = getProcessedSimilarity(text1, text2);
        assertEquals(1.0, similarity, 0.01, "完全相同的混合文本应匹配");
    }

    // 11. 相似度阈值测试（基于编辑距离的边界）
    @Test
    void testSimilarityAtThreshold() {
        String text1 = "数据结构是计算机存储、组织数据的方式，常见的有数组、链表、树等。";
        String text2 = "数据结构是计算机存储数据的方式，常见的有数组、树等结构。";
        double similarity = getProcessedSimilarity(text1, text2);
        // 实际计算值约为0.75-0.85（根据编辑距离）
        assertTrue(similarity >= 0.75 && similarity <= 0.85, "阈值附近相似度应在合理范围");
    }
}
    