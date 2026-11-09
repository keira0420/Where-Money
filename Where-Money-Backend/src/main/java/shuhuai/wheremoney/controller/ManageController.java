package shuhuai.wheremoney.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import shuhuai.wheremoney.utils.RequestGetter;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 管理控制器
 * 处理系统管理相关的HTTP请求，包括下载日志文件等操作
 */
@RestController
@RequestMapping("/api/manage")
@Tag(name = "管理管理")
@Slf4j
public class ManageController extends BaseController {
    /**
     * 下载日志文件
     * @param response HTTP响应对象，用于输出日志文件内容
     */
    @RequestMapping(value = "/log", method = RequestMethod.GET)
    @ResponseBody
    public void Download(HttpServletResponse response) {
        // 设置响应头，指定文件名
        response.setHeader("Content-Disposition", "inline;filename=" + java.net.URLEncoder.encode("log.log", StandardCharsets.UTF_8));
        // 缓冲区大小
        byte[] buff = new byte[1024];
        // 输入流对象
        BufferedInputStream bufferedInputStream = null;
        try {
            // 获取响应输出流
            OutputStream outputStream = response.getOutputStream();
            // 获取系统当前工作目录
            String path = System.getProperty("user.dir");
            // 创建输入流，读取日志文件
            bufferedInputStream = new BufferedInputStream(new FileInputStream(path + "/logs/log.log"));
            // 读取文件内容
            int i = bufferedInputStream.read(buff);
            // 循环读取并写入响应
            while (i != -1) {
                outputStream.write(buff, 0, buff.length);
                outputStream.flush();
                i = bufferedInputStream.read(buff);
            }
        } catch (FileNotFoundException fileNotFoundException) {
            // 记录文件未找到错误
            log.error(RequestGetter.getRequestUrl() + "：系统找不到指定的文件。");
        } catch (IOException e) {
            // 记录IO错误
            log.error(RequestGetter.getRequestUrl() + "：获取日志文件失败。");
        } finally {
            // 关闭输入流
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    // 记录关闭流错误
                    log.error(RequestGetter.getRequestUrl() + "：获取日志文件失败。");
                }
            }
        }
        // 记录成功日志
        log.info(RequestGetter.getRequestUrl() + "：获取日志文件成功。");
    }
}