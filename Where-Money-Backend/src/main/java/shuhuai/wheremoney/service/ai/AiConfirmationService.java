package shuhuai.wheremoney.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import shuhuai.wheremoney.mapper.*;
import shuhuai.wheremoney.response.ai.AiViews.Confirmation;
import shuhuai.wheremoney.service.BillService;
import shuhuai.wheremoney.service.excep.common.*;
import shuhuai.wheremoney.type.BillType;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiConfirmationService {
    public record Draft(String requestId, Integer bookId, BillType type, BigDecimal amount,
                        BigDecimal transferFee, Timestamp billTime, String remark,
                        Integer outAssetId, Integer inAssetId, Integer billCategoryId) {}
    private final AiAccessGuard access;
    private final AssetMapper assets;
    private final BillCategoryMapper categories;
    private final AiInsightMapper mapper;
    private final BillService bills;
    private final ObjectMapper json;

    @Transactional(rollbackFor = Exception.class)
    public Confirmation confirm(Integer userId, Draft draft) {
        if (draft == null || draft.type() == null || draft.type() == BillType.退款
            || draft.billTime() == null || draft.requestId() == null)
            throw new ParamsException("账单字段不完整");
        try { UUID.fromString(draft.requestId()); }
        catch (IllegalArgumentException e) { throw new ParamsException("无效的提交编号"); }
        access.requireBook(userId, draft.bookId());
        checkMoney(draft.amount(), true);
        if (draft.remark() != null && draft.remark().length() > 255) throw new ParamsException("备注最多255字");
        if (draft.type() != BillType.收入) requireAsset(userId, draft.outAssetId());
        if (draft.type() != BillType.支出) requireAsset(userId, draft.inAssetId());
        if (draft.type() == BillType.转账) {
            if (Objects.equals(draft.inAssetId(), draft.outAssetId())) throw new ParamsException("转入转出账户不能相同");
            if (draft.transferFee() != null) {
                checkMoney(draft.transferFee(), false);
                if (draft.transferFee().compareTo(draft.amount()) > 0) throw new ParamsException("手续费不能大于转账金额");
            }
        } else {
            var category = draft.billCategoryId() == null ? null : categories.selectBillCategoryById(draft.billCategoryId());
            if (category == null || !Objects.equals(category.getBookId(), draft.bookId()) || category.getType() != draft.type())
                throw new ParamsException("分类不属于当前账本或类型不匹配");
        }
        String hash;
        try { hash = DigestUtils.md5DigestAsHex(json.writeValueAsBytes(draft)); }
        catch (Exception e) { throw new ParamsException("无法读取账单"); }
        mapper.claim(userId, draft.requestId(), hash);
        if (!hash.equals(mapper.confirmationHash(userId, draft.requestId())))
            throw new ParamsException("此提交编号已用于另一笔账单，请重新生成草稿");
        if (mapper.confirmationSaved(userId, draft.requestId())) {
            return new Confirmation(draft.requestId(), true);
        }
        bills.addBill(draft.bookId(), draft.inAssetId(), draft.outAssetId(), null,
            draft.billCategoryId(), draft.type(), draft.amount(), draft.transferFee(), draft.billTime(),
            draft.remark(), false, null);
        mapper.markSaved(userId, draft.requestId());
        return new Confirmation(draft.requestId(), false);
    }

    private void requireAsset(Integer userId, Integer assetId) {
        var asset = assetId == null ? null : assets.selectAssetById(assetId);
        if (asset == null || !Objects.equals(asset.getUserId(), userId))
            throw new PermissionDeniedException("无权使用该账户");
    }

    private void checkMoney(BigDecimal amount, boolean positive) {
        if (amount == null || amount.scale() > 2 || amount.compareTo(new BigDecimal("99999999.99")) > 0
            || (positive ? amount.signum() <= 0 : amount.signum() < 0))
            throw new ParamsException("金额需在允许范围内，且最多两位小数");
    }
}
