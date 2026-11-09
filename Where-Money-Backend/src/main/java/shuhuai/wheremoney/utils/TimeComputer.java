package shuhuai.wheremoney.utils;

import java.sql.Timestamp;

/**
 * 时间计算工具类
 * 提供时间戳的各种计算方法
 */
public class TimeComputer {
    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     */
    public static Timestamp getNow() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 获取下一天的时间戳
     *
     * @param timestamp 原始时间戳
     * @return 下一天的时间戳
     */
    public static Timestamp nextDay(Timestamp timestamp) {
        return new Timestamp(timestamp.getTime() + 24 * 60 * 60 * 1000);
    }

    /**
     * 获取前一天的时间戳
     *
     * @param timestamp 原始时间戳
     * @return 前一天的时间戳
     */
    public static Timestamp prevDay(Timestamp timestamp) {
        return new Timestamp(timestamp.getTime() - 24 * 60 * 60 * 1000);
    }

    /**
     * 获取当天开始的时间戳（00:00:00）
     *
     * @param timestamp 原始时间戳
     * @return 当天开始的时间戳
     */
    public static Timestamp getDay(Timestamp timestamp) {
        return new Timestamp(timestamp.getTime() - (timestamp.getTime() + 60 * 60 * 8 * 1000) % (24 * 60 * 60 * 1000));
    }

    /**
     * 获取当天结束的时间戳（23:59:59）
     *
     * @param timestamp 原始时间戳
     * @return 当天结束的时间戳
     */
    public static Timestamp getDayEnd(Timestamp timestamp) {
        return new Timestamp(getDay(timestamp).getTime() + 24 * 60 * 60 * 1000 - 1);
    }

    /**
     * 将天数转换为秒数
     *
     * @param day 天数
     * @return 秒数
     */
    public static Long dayToSecond(Long day) {
        return day * 24 * 60 * 60;
    }
}