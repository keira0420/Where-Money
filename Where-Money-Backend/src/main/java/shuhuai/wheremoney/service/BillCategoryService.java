package shuhuai.wheremoney.service;

import shuhuai.wheremoney.entity.BillCategory;
import shuhuai.wheremoney.type.BillType;

import java.util.List;

/**
 * 账单分类服务接口
 * 提供账单分类相关的业务逻辑操作，包括分类的创建、查询、更新、删除，以及默认分类的添加功能
 */
public interface BillCategoryService {
    /**
     * 添加默认账单分类
     * @param bookId 账本ID
     */
    void addDefaultBillCategory(Integer bookId);

    /**
     * 获取指定账单分类
     * @param id 分类ID
     * @return 账单分类实体
     */
    BillCategory getBillCategory(Integer id);

    /**
     * 获取账本的所有账单分类
     * @param bookId 账本ID
     * @return 账单分类列表
     */
    List<BillCategory> getBillCategoriesByBook(Integer bookId);

    /**
     * 获取账本指定类型的账单分类
     * @param bookId 账本ID
     * @param type 账单类型
     * @return 账单分类列表
     */
    List<BillCategory> getBillCategoriesByBookType(Integer bookId, BillType type);

    /**
     * 添加账单分类
     * @param bookId 账本ID
     * @param name 分类名称
     * @param svg 分类图标
     * @param type 分类类型
     */
    void addBillCategory(Integer bookId, String name, String svg, BillType type);

    /**
     * 删除账单分类
     * @param billCategoryId 分类ID
     */
    void deleteBillCategory(Integer billCategoryId);

    /**
     * 更新账单分类
     * @param id 分类ID
     * @param name 分类名称
     * @param svg 分类图标
     * @param type 分类类型
     * @param bookId 账本ID
     */
    void updateBillCategory(Integer id, String name, String svg, BillType type, Integer bookId);
}