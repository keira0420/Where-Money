package shuhuai.wheremoney.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shuhuai.wheremoney.response.Response;
import shuhuai.wheremoney.response.user.ChangeUserNameResponse;
import shuhuai.wheremoney.response.user.LoginResponse;
import shuhuai.wheremoney.service.UserService;
import shuhuai.wheremoney.utils.TokenValidator;

/**
 * 用户管理控制器
 * 处理用户相关的HTTP请求，包括注册、登录、修改用户名、修改密码等操作
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理")
@Slf4j
public class UserController extends BaseController {
    /**
     * 用户服务实例，用于处理用户相关的业务逻辑
     */
    @Resource
    private UserService userService;
    /**
     * 令牌验证器，用于生成和验证用户令牌
     */
    @Resource
    private TokenValidator tokenValidator;

    /**
     * 用户注册
     * @param userName 用户名
     * @param password 密码
     * @return 注册结果
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @Operation(summary = "注册")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "用户名已被占用"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public Response<Object> register(@RequestParam String userName, @RequestParam String password) {
        // 调用服务层进行注册
        userService.register(userName, password);
        return new Response<>(200, "注册成功", null);
    }

    /**
     * 用户登录
     * @param userName 用户名
     * @param password 密码
     * @return 登录结果，包含token
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @Operation(summary = "登录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "401", description = "账户或密码错误"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    public Response<LoginResponse> login(@RequestParam String userName, @RequestParam String password) {
        // 调用服务层进行登录验证
        userService.login(userName, password);
        // 生成token
        Integer userId = userService.getUserId(userName);
        String token = tokenValidator.getToken(userId);
        return new Response<>(200, "登录成功", new LoginResponse(token));
    }

    /**
     * 修改用户名
     * @param userName 新用户名
     * @return 修改结果，包含新token
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "修改用户名成功"),
            @ApiResponse(responseCode = "400", description = "用户名已被占用"),
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @RequestMapping(value = "/user-name", method = RequestMethod.PATCH)
    @Operation(summary = "修改用户名")
    public Response<ChangeUserNameResponse> changeUserName(@RequestParam String userName) {
        // 从token中获取用户ID
        Integer userId = Integer.parseInt(TokenValidator.getUser().get("userId"));
        // 调用服务层修改用户名
        userService.changeUsername(userId, userName);
        // 生成新token
        String token = tokenValidator.getToken(userId);
        return new Response<>(200, "修改用户名成功", new ChangeUserNameResponse(token));
    }

    /**
     * 修改密码
     * @param password 新密码
     * @return 修改结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "修改用户名成功"),
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @RequestMapping(value = "/password", method = RequestMethod.PATCH)
    @Operation(summary = "修改密码")
    public Response<Object> changePassword(@RequestParam String password) {
        // 从token中获取用户ID
        Integer userId = Integer.parseInt(TokenValidator.getUser().get("userId"));
        // 调用服务层修改密码
        userService.changePassword(userId, password);
        return new Response<>(200, "修改密码成功", null);
    }

    /**
     * 获取用户协议
     * @return 用户协议HTML内容
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取协议成功"),
    })
    @Operation(summary = "获取协议")
    @RequestMapping(value = "/protocol", method = RequestMethod.GET)
    public Response<String> protocol() {
        // 构建用户协议HTML内容
        String protocolHtml = """
                <h1>Where-Money 用户协议</h1>
                <p><strong>生效日期：</strong>2026-02-14</p>
                <p>欢迎使用 Where-Money。使用本服务即视为您已阅读并同意本协议全部条款。</p>
                <h2>1. 账号与安全</h2>
                <ul>
                  <li>您应妥善保管账号与登录凭证，不得出借、出租或转让。</li>
                  <li>因您保管不当导致的风险由您自行承担。</li>
                </ul>
                <h2>2. 数据与隐私</h2>
                <ul>
                  <li>我们仅在提供服务所必需范围内处理您的数据。</li>
                  <li>未经您授权，除法律法规要求外，不会向第三方披露您的个人信息。</li>
                </ul>
                <h2>3. 使用规范</h2>
                <ul>
                  <li>不得利用本服务实施违法违规行为。</li>
                  <li>不得干扰、破坏平台正常运行或进行未授权访问。</li>
                </ul>
                <h2>4. 服务变更与终止</h2>
                <p>我们有权在必要时对服务内容进行调整、维护或终止，并尽可能提前告知。</p>
                <h2>5. 协议更新</h2>
                <p>本协议可能根据业务或法律要求更新，更新后版本发布即生效。</p>
                <p>如您继续使用服务，视为接受更新后的协议内容。</p>
                """;
        return new Response<>(200, protocolHtml);
    }
}