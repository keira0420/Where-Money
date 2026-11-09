package shuhuai.wheremoney.mapper;

import org.apache.ibatis.annotations.Mapper;
import shuhuai.wheremoney.entity.BillCategory;
import shuhuai.wheremoney.type.BillType;

import java.util.List;

/**
 * 账单分类Mapper接口
 * 用于操作数据库中的bill_category表，提供账单分类的CRUD操作
 */
@Mapper
public interface BillCategoryMapper {
    /**
     * 选择性插入账单分类
     * @param billCategory 账单分类实体
     * @return 影响的行数
     */
    Integer insertBillCategorySelective(BillCategory billCategory);

    /**
     * 根据账本ID查询账单分类
     * @param bookId 账本ID
     * @return 账单分类列表
     */
    List<BillCategory> selectBillCategoryByBook(Integer bookId);

    /**
     * 根据账本ID和账单类型查询账单分类
     * @param bookId 账本ID
     * @param type 账单类型
     * @return 账单分类列表
     */
    List<BillCategory> selectBillCategoryByBookType(Integer bookId, BillType type);

    /**
     * 根据ID查询账单分类
     * @param id 账单分类ID
     * @return 账单分类实体
     */
    BillCategory selectBillCategoryById(Integer id);

    /**
     * 删除账单分类
     * @param id 账单分类ID
     * @return 影响的行数
     */
    Integer deleteBillCategory(Integer id);

    /**
     * 选择性更新账单分类
     * @param billCategory 账单分类实体
     * @return 影响的行数
     */
    Integer updateBillCategorySelective(BillCategory billCategory);

    /**
     * 根据账本ID、分类名称和账单类型查询账单分类
     * @param bookId 账本ID
     * @param billCategoryName 分类名称
     * @param type 账单类型
     * @return 账单分类实体
     */
    BillCategory selectBillCategoryByBookIdNameType(Integer bookId, String billCategoryName, BillType type);

    /**
     * 根据账本ID删除账单分类
     * @param bookId 账本ID
     * @return 影响的行数
     */
    Integer deleteBillCategoryByBookId(Integer bookId);
}