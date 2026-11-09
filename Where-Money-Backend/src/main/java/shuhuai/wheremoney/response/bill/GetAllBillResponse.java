package shuhuai.wheremoney.response.bill;

import java.util.List;

/**
 * 获取所有账单响应类
 * 用于返回用户的所有账单信息
 */
public class GetAllBillResponse {
    private List<BaseGetBillResponse> billList;

    /**
     * 构造方法
     *
     * @param billList 账单列表
     */
    public GetAllBillResponse(List<BaseGetBillResponse> billList) {
        this.billList = billList;
    }

    /**
     * 获取账单列表
     *
     * @return 账单列表
     */
    public List<BaseGetBillResponse> getBillList() {
        return billList;
    }

    /**
     * 设置账单列表
     *
     * @param billList 账单列表
     */
    public void setBillList(List<BaseGetBillResponse> billList) {
        this.billList = billList;
    }
}