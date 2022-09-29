package com.fqm.framework.web.core.handler;

import static com.fqm.framework.common.core.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static com.fqm.framework.common.core.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static com.fqm.framework.common.core.exception.enums.GlobalErrorCodeConstants.METHOD_NOT_ALLOWED;
import static com.fqm.framework.common.core.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;

import java.util.Enumeration;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fqm.framework.common.core.exception.ServiceException;
import com.fqm.framework.common.core.util.ExceptionUtil;
import com.fqm.framework.common.core.vo.R;


/**
 * 全局异常处理器，将 Exception 翻译成 R + 对应的异常编号
 * @version 
 * @author 傅泉明
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${spring.application.name}")
    private String applicationName;
    
//    @Resource
//    private SystemExceptionLogRpc systemExceptionLogRpc;
    
    /**
     * 处理 SpringMVC 请求参数缺失
     *
     * 例如说，接口上设置了 @RequestParam("xx") 参数，结果并未传递 xx 参数
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public R<?> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException ex) {
        logger.warn("[missingServletRequestParameterExceptionHandler]", ex);
        return R.build(BAD_REQUEST.getCode(), String.format("请求参数缺失:%s", ex.getParameterName()))
                .setDetailMessage(ExceptionUtil.getRootCauseMessage(ex));
    }
    
    /**
     * 处理 SpringMVC 请求参数类型错误
     *
     * 例如说，接口上设置了 @RequestParam("xx") 参数为 Integer，结果传递 xx 参数类型为 String
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<?> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException ex) {
        logger.warn("[missingServletRequestParameterExceptionHandler]", ex);
        return R.build(BAD_REQUEST.getCode(), String.format("请求参数类型错误:%s", ex.getMessage()))
                .setDetailMessage(ExceptionUtil.getRootCauseMessage(ex));
    }
    
    /**
     * 处理 SpringMVC 参数校验不正确
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> methodArgumentNotValidExceptionExceptionHandler(MethodArgumentNotValidException ex) {
        logger.warn("[methodArgumentNotValidExceptionExceptionHandler]", ex);
        FieldError fieldError = ex.getBindingResult().getFieldError();
        assert fieldError != null; // 断言，避免告警
        return R.build(BAD_REQUEST.getCode(), String.format("请求参数不正确:%s", fieldError.getDefaultMessage()))
                .setDetailMessage(ExceptionUtil.getRootCauseMessage(ex));
    }

    /**
     * 处理 SpringMVC 参数绑定不正确，本质上也是通过 Validator 校验
     */
    @ExceptionHandler(BindException.class)
    public R<?> bindExceptionHandler(BindException ex) {
        logger.warn("[handleBindException]", ex);
        FieldError fieldError = ex.getFieldError();
        assert fieldError != null; // 断言，避免告警
        return R.build(BAD_REQUEST.getCode(), String.format("请求参数不正确:%s", fieldError.getDefaultMessage()))
                .setDetailMessage(ExceptionUtil.getRootCauseMessage(ex));
    }
    
    /**
     * 处理 Validator 校验不通过产生的异常
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public R<?> constraintViolationExceptionHandler(ConstraintViolationException ex) {
        logger.warn("[constraintViolationExceptionHandler]", ex);
        ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
        return R.build(BAD_REQUEST.getCode(), String.format("请求参数不正确:%s", constraintViolation.getMessage()))
                .setDetailMessage(ExceptionUtil.getRootCauseMessage(ex));
    }

    /**
     * 处理 SpringMVC 请求地址不存在
     *
     * 注意，它需要设置如下两个配置项：
     * 1. spring.mvc.throw-exception-if-no-handler-found 为 true
     * 2. spring.mvc.static-path-pattern 为 /statics/**
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public R<?> noHandlerFoundExceptionHandler(NoHandlerFoundException ex) {
        logger.warn("[noHandlerFoundExceptionHandler]", ex);
        return R.build(NOT_FOUND.getCode(), String.format("请求地址不存在:%s", ex.getRequestURL()))
                .setDetailMessage(ExceptionUtil.getRootCauseMessage(ex));
    }

    /**
     * 处理 SpringMVC 请求方法不正确
     *
     * 例如说，A 接口的方法为 GET 方式，结果请求方法为 POST 方式，导致不匹配
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<?> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException ex) {
        logger.warn("[httpRequestMethodNotSupportedExceptionHandler]", ex);
        return R.build(METHOD_NOT_ALLOWED.getCode(), String.format("请求方法不正确:%s", ex.getMessage()))
                .setDetailMessage(ExceptionUtil.getRootCauseMessage(ex));
    }
    
    /**
     * 处理 Dubbo Consumer 本地参数校验时，抛出的 ValidationException 异常
     */
    @ExceptionHandler(value = ValidationException.class)
    public R<?> validationException(ValidationException ex) {
        logger.warn("[constraintViolationExceptionHandler]", ex);
        // 无法拼接明细的错误信息，因为 Dubbo Consumer 抛出 ValidationException 异常时，是直接的字符串信息，且人类不可读
        return R.build(BAD_REQUEST.getCode(), "请求参数不正确")
                .setDetailMessage(ExceptionUtil.getRootCauseMessage(ex));
    }
    
    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public R<?> error(ServiceException e) {
        e.printStackTrace();
        return R.fail(e.getMessage());
    }
    
    
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public R<?> defaultExceptionHandler(HttpServletRequest req, Exception e) {
        e.printStackTrace();
        logger.error("[defaultExceptionHandler]", e);
        // 插入异常日志
//        this.createExceptionLog(req, e);
        
//        String queryStr = buildQueryString(req);
//        System.out.println("queryStr=" + queryStr);
//        R<?> r = (R<?>)req.getAttribute("fqm_common_result");
//        if (r != null) {
//            System.out.println("r=" + r);
//        }
        
        // 返回 ERROR CommonResult
        return R.build(INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMessage())
                .setDetailMessage(ExceptionUtil.getRootCauseMessage(e));
    }

    public static String buildQueryString(HttpServletRequest request) {
        Enumeration<String> es = request.getParameterNames();
        if (!es.hasMoreElements()) {
            return "";
        }
        String parameterName, parameterValue;
        StringBuilder params = new StringBuilder();
        while (es.hasMoreElements()) {
            parameterName = es.nextElement();
            parameterValue = request.getParameter(parameterName);
            params.append(parameterName).append("=").append(parameterValue).append("&");
        }
        return params.deleteCharAt(params.length() - 1).toString();
    }
    
//    public void createExceptionLog(HttpServletRequest req, Throwable e) {
//        // 插入异常日志
//        SystemExceptionLogCreateDTO exceptionLog = new SystemExceptionLogCreateDTO();
//        try {
//            // 增加异常计数 metrics TODO 暂时去掉
////            EXCEPTION_COUNTER.increment();
//            // 初始化 exceptionLog
//            initExceptionLog(exceptionLog, req, e);
//            // 执行插入 exceptionLog
//            createExceptionLog(exceptionLog);
//        } catch (Throwable th) {
//            logger.error("[createExceptionLog][插入访问日志({}) 发生异常({})", JsonUtil.toJsonStr(exceptionLog), ExceptionUtil.getRootCauseMessage(th));
//        }
//    }
//
//    
//    // TODO 优化点：后续可以增加事件
//    @Async
//    public void createExceptionLog(SystemExceptionLogCreateDTO exceptionLog) {
//        try {
//            systemExceptionLogRpc.createSystemExceptionLog(exceptionLog);
//        } catch (Throwable th) {
//            logger.error("[addAccessLog][插入异常日志({}) 发生异常({})", JsonUtil.toJsonStr(exceptionLog), ExceptionUtil.getRootCauseMessage(th));
//        }
//    }
//    
//    private void initExceptionLog(SystemExceptionLogCreateDTO exceptionLog, HttpServletRequest request, Throwable e) {
//        // 设置账号编号
//        exceptionLog.setUserId(CommonWebUtil.getUserId(request));
//        exceptionLog.setUserType(CommonWebUtil.getUserType(request));
//        // 设置异常字段
//        exceptionLog.setExceptionName(e.getClass().getName());
//        exceptionLog.setExceptionMessage(ExceptionUtil.getMessage(e));
//        exceptionLog.setExceptionRootCauseMessage(ExceptionUtil.getRootCauseMessage(e));
//        exceptionLog.setExceptionStackTrace(ExceptionUtil.getStackTrace(e));
//        StackTraceElement[] stackTraceElements = e.getStackTrace();
//        Assert.notEmpty(stackTraceElements, "异常 stackTraceElements 不能为空");
//        StackTraceElement stackTraceElement = stackTraceElements[0];
//        exceptionLog.setExceptionClassName(stackTraceElement.getClassName());
//        exceptionLog.setExceptionFileName(stackTraceElement.getFileName());
//        exceptionLog.setExceptionMethodName(stackTraceElement.getMethodName());
//        exceptionLog.setExceptionLineNumber(stackTraceElement.getLineNumber());
//        // 设置其它字段
//        exceptionLog.setTraceId(MallbookUtil.getTraceId())
//                .setApplicationName(applicationName)
//                .setUri(request.getRequestURI()) // TODO 提升：如果想要优化，可以使用 Swagger 的 @ApiOperation 注解。
//                .setQueryString(HttpRequestUtil.buildQueryString(request))
//                .setMethod(request.getMethod())
//                .setUserAgent(HttpRequestUtil.getUserAgent(request))
//                .setIp(HttpRequestUtil.getIp(request))
//                .setExceptionTime(new Date());
//    }

}
