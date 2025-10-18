import java.util.Objects;

/*
 分数类，用于表示和处理分数相关逻辑，支持分数的四则运算、自动化简、大小比较、格式转换等功能
 实现Comparable<Fraction>接口，可直接对两个分数对象进行大小比较
 */
public class Fraction implements Comparable<Fraction> {
    // 分数的分子，使用long类型避免整数溢出问题
    long numerator;
    // 分数的分母，使用long类型，且始终保持为正数（化简时统一处理）
    long denominator;


    // 构造方法：通过分子和分母创建分数对象
    public Fraction(long numerator, long denominator) {
        if (denominator == 0) {
            throw new IllegalArgumentException("Denominator cannot be zero.");
        }
        this.numerator = numerator;
        this.denominator = denominator;
        simplify();
    }

    /*
     构造方法：通过整数创建分数对象（默认分母为1）
     作用：方便将整数直接视为分母为1的分数，简化整数与分数的混合运算
     */
    public Fraction(long integer) {
        this.numerator = integer;
        this.denominator = 1;
    }

    /**
      私有方法：化简分数，确保分数处于最简形式且分母为正
      化简逻辑：
      1. 若分母为负，调整分子符号为负、分母符号为正（统一分母为正，避免负号位置混乱）
      2. 计算分子绝对值与分母的最大公约数（GCD）
      3. 分子和分母同时除以最大公约数，完成约分
     */
    private void simplify() {
        if (denominator < 0) {
            numerator = -numerator;
            denominator = -denominator;
        }
        long gcd = greatestCommonDivisor(Math.abs(numerator), denominator);
        numerator /= gcd;
        denominator /= gcd;
    }

    /*
      私有方法：计算两个非负整数的最大公约数（GCD）
      实现逻辑：采用欧几里得递归算法，当b为0时，a即为最大公约数
     */
    private long greatestCommonDivisor(long a, long b) {
        return b == 0 ? a : greatestCommonDivisor(b, a % b);
    }

    /*
      分数加法运算
      运算逻辑：通分后分子相加（分子1*分母2 + 分子2*分母1），分母为两分母乘积
     */
    public Fraction add(Fraction other) {
        long newNumerator = this.numerator * other.denominator + other.numerator * this.denominator;
        long newDenominator = this.denominator * other.denominator;
        return new Fraction(newNumerator, newDenominator);
    }

    /*
      分数减法运算
      运算逻辑：通分后分子相减（分子1*分母2 - 分子2*分母1），分母为两分母乘积
     */
    public Fraction subtract(Fraction other) {
        long newNumerator = this.numerator * other.denominator - other.numerator * this.denominator;
        long newDenominator = this.denominator * other.denominator;
        return new Fraction(newNumerator, newDenominator);
    }

    /*
      分数乘法运算
      运算逻辑：分子与分子相乘作为新分子，分母与分母相乘作为新分母
     */
    public Fraction multiply(Fraction other) {
        long newNumerator = this.numerator * other.numerator;
        long newDenominator = this.denominator * other.denominator;
        return new Fraction(newNumerator, newDenominator);
    }

    /*
      分数除法运算
     运算逻辑：除以一个分数等于乘以其倒数（分子*除数分母，分母*除数分子）
     */
    public Fraction divide(Fraction other) {
        if (other.numerator == 0) {
            throw new ArithmeticException("Cannot divide by zero.");
        }
        long newNumerator = this.numerator * other.denominator;
        long newDenominator = this.denominator * other.numerator;
        return new Fraction(newNumerator, newDenominator);
    }

    /*
      实现Comparable接口的方法，比较当前分数与另一个分数的大小
      比较逻辑：交叉相乘比较（当前分子*other分母 vs other分子*当前分母），避免浮点数精度误差
     */
    @Override
    public int compareTo(Fraction other) {
        return Long.compare(this.numerator * other.denominator, other.numerator * this.denominator);
    }

    /*
      重写equals方法，判断两个分数对象是否相等
      判断逻辑：1. 先判断是否为同一对象；2. 判断对象是否为Fraction类型；3. 分子和分母分别相等（已化简前提下）
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fraction fraction = (Fraction) o;
        return numerator == fraction.numerator && denominator == fraction.denominator;
    }

    /*
      重写hashCode方法，为Fraction对象生成哈希值
      作用：保证equals为true的两个对象，hashCode一定相等（符合哈希集合的使用规范）
     */
    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator);
    }

    /*
      重写toString方法，将分数格式化为易读的字符串
      格式化规则：
      1. 先化简分数（确保格式统一）；
      2. 分母为1时，直接输出分子（整数形式）；
      3. 分子绝对值大于分母时，输出带分数（整数部分'分子余数/分母'）；
      4. 其他情况输出真分数（分子/分母）
     */
    @Override
    public String toString() {
        simplify();
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
      判断当前分数是否为负数
      判断依据：化简后分子为负（分母已确保为正，分子符号即分数整体符号）
     */
    public boolean isNegative() {
        return numerator < 0;
    }

    /*
      判断是否为真分数（不含整数部分，即分子绝对值小于分母）
      注：真分数的定义是值在-1到1之间（不包含-1和1），此处通过分子绝对值与分母的大小关系判断
     */
    public boolean isProperFraction() {
        return Math.abs(numerator) < denominator;
    }
}
