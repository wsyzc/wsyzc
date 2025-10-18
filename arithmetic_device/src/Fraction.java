import java.util.Objects;
//分数类，支持分数的四则运算、化简、比较、格式输出等操作，实现了Comparable接口以支持分数大小比较
public class Fraction implements Comparable<Fraction> {
    // 分数的分子
    private long numerator;
    // 分数的分母
    private long denominator;

    //构造分数对象，初始化时会自动化简分数
    public Fraction(long numerator, long denominator) {
        if (denominator == 0) {
            throw new IllegalArgumentException("Denominator cannot be zero.");
        }
        this.numerator = numerator;
        this.denominator = denominator;
        simplify(); // 初始化时化简分数
    }

    //由整数构造分数（分母默认为1）
    public Fraction(long integer) {
        this.numerator = integer;
        this.denominator = 1;
    }

    // 化简分数：统一分母为正，并用最大公约数约分
    private void simplify() {
        // 确保分母为正数，若分母为负则同时调整分子符号
        if (denominator < 0) {
            numerator = -numerator;
            denominator = -denominator;
        }
        // 计算分子绝对值与分母的最大公约数
        long gcd = greatestCommonDivisor(Math.abs(numerator), denominator);
        // 分子分母同除以最大公约数完成约分
        numerator /= gcd;
        denominator /= gcd;
    }

    //计算两个数的最大公约数（欧几里得递归算法）
    private long greatestCommonDivisor(long a, long b) {
        return b == 0 ? a : greatestCommonDivisor(b, a % b);
    }

    // 分数加法运算
    public Fraction add(Fraction other) {
        // 通分后分子相加，分母为两分母乘积
        long newNumerator = this.numerator * other.denominator + other.numerator * this.denominator;
        long newDenominator = this.denominator * other.denominator;
        return new Fraction(newNumerator, newDenominator);
    }

    //分数减法运算
    public Fraction subtract(Fraction other) {
        // 通分后分子相减，分母为两分母乘积
        long newNumerator = this.numerator * other.denominator - other.numerator * this.denominator;
        long newDenominator = this.denominator * other.denominator;
        return new Fraction(newNumerator, newDenominator);
    }

    //分数乘法运算
    public Fraction multiply(Fraction other) {
        // 分子相乘为新分子，分母相乘为新分母
        long newNumerator = this.numerator * other.numerator;
        long newDenominator = this.denominator * other.denominator;
        return new Fraction(newNumerator, newDenominator);
    }

    //分数除法运算
    public Fraction divide(Fraction other) {
        if (other.numerator == 0) {
            throw new ArithmeticException("Cannot divide by zero.");
        }
        // 除以一个分数等于乘以其倒数
        long newNumerator = this.numerator * other.denominator;
        long newDenominator = this.denominator * other.numerator;
        return new Fraction(newNumerator, newDenominator);
    }

    //比较两个分数的大小（实现Comparable接口）
    // 逻辑：通过交叉相乘比较分子*对方分母的大小
    @Override
    public int compareTo(Fraction other) {
        return Long.compare(this.numerator * other.denominator, other.numerator * this.denominator);
    }

    //重写equals方法，判断两个分数是否相等（分子和分母都相等）
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fraction fraction = (Fraction) o;
        return numerator == fraction.numerator && denominator == fraction.denominator;
    }

    // 重写hashCode方法，基于分子和分母生成哈希码
    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator);
    }

    /*
     重写toString方法，格式化分数输出：
     分母为1时输出整数形式；
     分子绝对值大于分母时输出带分数形式；
     否则输出真分数形式
     */
    @Override
    public String toString() {
        simplify(); // 确保输出前分数是化简后的
        if (denominator == 1) {
            return String.valueOf(numerator);
        }
        if (Math.abs(numerator) > denominator) {
            long integerPart = numerator / denominator;
            long remainder = Math.abs(numerator % denominator);
            if (remainder == 0) {
                return String.valueOf(integerPart);
            }
            return integerPart + "'" + remainder + "/" + denominator;
        }
        return numerator + "/" + denominator;
    }

    /*
     判断分数是否为负数
     若分子小于0则返回true，否则返回false
     */
    public boolean isNegative() {
        return numerator < 0;
    }

    /*
     判断是否为真分数（分子绝对值小于分母，不含整数部分）
     @return 若分子绝对值小于分母返回true，否则返回false
     */
    public boolean isProperFraction() {
        return Math.abs(numerator) < denominator;
    }
}

