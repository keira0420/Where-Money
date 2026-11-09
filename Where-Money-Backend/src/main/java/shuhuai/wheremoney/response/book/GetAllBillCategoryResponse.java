package shuhuai.wheremoney.response.book;

import shuhuai.wheremoney.entity.BillCategory;

import java.util.List;

/**
 * 获取所有账单分类响应类
 * 用于返回账本的所有账单分类信息
 */
public class GetAllBillCategoryResponse {
    private List<BillCategory> billCategoryList;

    /**
     * 构造方法
     *
     * @param billCategoryList 账单分类列表
     */
    public GetAllBillCategoryResponse(List<BillCategory> billCategoryList) {
        this.billCategoryList = billCategoryList;
    }

    /**
     * 获取账单分类列表
     *
     * @return 账单分类列表
     */
    public List<BillCategory> getBillCategoryList() {
        return billCategoryList;
    }

    /**
     * 设置账单分类列表
     *
     * @param billCategoryList 账单分类列表
     */
    public void setBillCategoryList(List<BillCategory> billCategoryList) {
        this.billCategoryList = billCategoryList;
    }
}