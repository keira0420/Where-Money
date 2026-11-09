package shuhuai.wheremoney.service.impl;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import shuhuai.wheremoney.entity.*;
import shuhuai.wheremoney.mapper.AssetMapper;
import shuhuai.wheremoney.mapper.BookMapper;
import shuhuai.wheremoney.mapper.UserMapper;
import shuhuai.wheremoney.service.AssetService;
import shuhuai.wheremoney.service.BillService;
import shuhuai.wheremoney.service.excep.common.ParamsException;
import shuhuai.wheremoney.service.excep.common.ServerException;
import shuhuai.wheremoney.type.AssetType;
import shuhuai.wheremoney.utils.BeanGetter;
import shuhuai.wheremoney.utils.RedisConnector;
import shuhuai.wheremoney.utils.TimeComputer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资产服务实现类
 * 实现AssetService接口，提供资产相关的业务逻辑操作
 */
@Service
public class AssetServiceImpl implements AssetService {
    /**
     * Redis资产过期时间
     */
    @Value("${redis.asset.expire}")
    private Long assetExpire;
    /**
     * 资产Mapper
     */
    @Resource
    private AssetMapper assetMapper;
    /**
     * 用户Mapper
     */
    @Resource
    private UserMapper userMapper;
    /**
     * 账本Mapper
     */
    @Resource
    private BookMapper bookMapper;
    /**
     * Redis连接器
     */
    @jakarta.annotation.Resource
    private RedisConnector redisConnector;

    /**
     * 添加资产
     * @param userId 用户ID
     * @param assetName 资产名称
     * @param balance 资产余额
     * @param type 资产类型
     * @param billDate 账单日
     * @param repayDate 还款日
     * @param quota 额度
     * @param inTotal 是否计入总资产
     * @param svg 资产图标
     * @throws ParamsException 参数错误异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void addAsset(Integer userId, String assetName, BigDecimal balance, AssetType type, Integer billDate,
                         Integer repayDate, BigDecimal quota, Boolean inTotal, String svg) {
        // 参数校验
        if (userId == null || assetName == null || balance == null || type == null || inTotal == null) {
            throw new ParamsException("参数错误");
        }
        // 创建资产实体
        Asset asset = new Asset(userId, type, balance, assetName, billDate, repayDate, quota, inTotal, svg);
        // 插入资产
        Integer result = assetMapper.insertAssetSelective(asset);
        if (result != 1) {
            throw new ServerException("服务器错误");
        }
        // 将资产写入Redis缓存
        redisConnector.writeObject("asset:" + asset.getId(), asset, TimeComputer.dayToSecond(assetExpire));
    }

    /**
     * 获取用户的所有资产
     * @param userId 用户ID
     * @return 资产列表
     * @throws ParamsException 参数错误异常
     */
    @Override
    public List<Asset> getAllAsset(Integer userId) {
        // 参数校验
        if (userId == null) {
            throw new ParamsException("参数错误");
        }
        // 查询用户的所有资产
        return assetMapper.selectAssetByUserId(userId);
    }

    /**
     * 获取指定资产
     * @param id 资产ID
     * @return 资产实体
     * @throws ParamsException 参数错误异常
     */
    @Override
    public Asset getAsset(Integer id) {
        // 参数校验
        if (id == null) {
            throw new ParamsException("参数错误");
        }
        // 先从Redis缓存中获取资产
        Object cached = redisConnector.readObject("asset:" + id);
        if (cached instanceof Asset asset) return asset;
        // 从数据库中获取资产
        Asset result = assetMapper.selectAssetById(id);
        // 如果资产存在，将其写入Redis缓存
        if (result != null) {
            redisConnector.writeObject("asset:" + id, result, TimeComputer.dayToSecond(assetExpire));
        }
        return result;
    }

    /**
     * 更新资产
     * @param asset 资产实体
     * @throws ServerException 服务器错误异常
     */
    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateAsset(Asset asset) {
        // 更新资产
        Integer result = assetMapper.updateAssetSelectiveById(asset);
        if (result != 1) {
            throw new ServerException("服务器错误");
        }
        // 更新Redis缓存中的资产
        redisConnector.writeObject("asset:" + asset.getId(), asset, TimeComputer.dayToSecond(assetExpire));
    }

    /**
     * 获取资产日统计
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日统计数据
     * @throws ParamsException 参数错误异常
     */
    @Override
    public List<Map<String, Object>> getDayStatistic(Integer userId, Timestamp startTime, Timestamp endTime) {
        // 参数校验
        if (userId == null || startTime == null || endTime == null) {
            throw new ParamsException("参数错误");
        }
        // 查询用户是否存在
        User user = userMapper.selectUserByUserId(userId);
        if (user == null) {
            throw new ParamsException("参数错误");
        }
        // 获取用户的总资产
        BigDecimal curTotal = assetMapper.selectTotalAssetByUserId(user.getId());
        if (curTotal == null) {
            curTotal = BigDecimal.ZERO;
        }
        // 查询用户的所有账本
        List<Book> bookList = bookMapper.selectBookByUser(user);
        if (bookList == null) {
            bookList = new ArrayList<>();
        }
        // 结果列表
        List<Map<String, Object>> result = new ArrayList<>();
        // 账单列表
        List<BaseBill> billTimeList = new ArrayList<>();
        // 获取BillService实例
        BillService billService = BeanGetter.getBean(BillService.class);
        // 计算回溯结束时间：从当前时间的下一天开始
        Timestamp backtrackEndTime = TimeComputer.nextDay(TimeComputer.getDay(TimeComputer.getNow()));
        // 获取所有账本在指定时间范围内的账单
        for (Book book : bookList) {
            billTimeList.addAll(billService.getBillByBookTime(book.getId(), startTime, backtrackEndTime));
        }
        // 将账单按日期分组
        Map<Timestamp, List<BaseBill>> billTimeMap = new HashMap<>();
        for (BaseBill bill : billTimeList) {
            Timestamp day = TimeComputer.getDay(bill.getBillTime());
            if (billTimeMap.containsKey(day)) {
                billTimeMap.get(day).add(bill);
            } else {
                List<BaseBill> temp = new ArrayList<>();
                temp.add(bill);
                billTimeMap.put(day, temp);
            }
        }
        // 从当前日期开始，向前回溯到开始日期
        for (Timestamp curTime = TimeComputer.getDay(TimeComputer.getNow()); !curTime.before(TimeComputer.getDay(startTime)); curTime = TimeComputer.prevDay(curTime)) {
            BigDecimal dayTotal = BigDecimal.ZERO;
            // 获取当天的账单
            List<BaseBill> dayBillList = billTimeMap.get(curTime);
            if (dayBillList != null) {
                // 计算当天的收支
                for (BaseBill bill : dayBillList) {
                    if (bill instanceof IncomeBill || bill instanceof RefundBill) {
                        dayTotal = dayTotal.add(bill.getAmount());
                    } else if (bill instanceof PayBill) {
                        dayTotal = dayTotal.subtract(bill.getAmount());
                    }
                }
            }
            Timestamp finalTime = curTime;
            BigDecimal finalDayTotal = curTotal;
            // 如果当前日期不晚于结束日期，将结果添加到列表中
            if (!curTime.after(TimeComputer.getDay(endTime))) {
                result.add(new HashMap<>(2) {{
                    put("time", TimeComputer.getDayEnd(finalTime));
                    put("total", finalDayTotal);
                }});
                // 更新当前总资产
                curTotal = curTotal.subtract(dayTotal);
            }
        }
        return result;
    }

    /**
     * 更新资产余额相对值
     * @param id 资产ID
     * @param relativeValue 相对值
     * @throws ParamsException 参数错误异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void changeBalanceRelative(Integer id, BigDecimal relativeValue) {
        // 参数校验
        if (id == null || relativeValue == null) {
            throw new ParamsException("参数错误");
        }
        // 更新资产余额
        Integer result = assetMapper.updateBalanceRelativeById(id, relativeValue);
        if (result != 1) {
            throw new ServerException("服务器错误");
        }
        // 如果Redis缓存中存在该资产，更新缓存
        redisConnector.deleteObject("asset:" + id);
    }
}
