package io.github.mymonstercat.ocr;

import com.benjaminwan.ocrlibrary.OcrEngine;
import com.benjaminwan.ocrlibrary.OcrResult;
import io.github.mymonstercat.Model;
import io.github.mymonstercat.exception.LoadException;
import io.github.mymonstercat.loader.LibraryLoader;
import io.github.mymonstercat.loader.ModelsLoader;
import io.github.mymonstercat.ocr.config.HardwareConfig;
import io.github.mymonstercat.ocr.config.ParamConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Base64;
import javax.imageio.ImageIO;

/**
 * 推理框架引擎
 * 支持多种图片输入方式：文件路径、字节数组、Base64、URL、BufferedImage
 */
@Slf4j
public class InferenceEngine extends OcrEngine {

    private Model model = Model.ONNX_PPOCR_V3;
    private HardwareConfig hardwareConfig = HardwareConfig.getOnnxConfig();
    private static InferenceEngine inferenceEngine;
    private static volatile LibraryLoader nativeLoader;
    private static volatile ModelsLoader modelsLoader;
    private static final AtomicBoolean isLibraryLoaded = new AtomicBoolean(false);

    private InferenceEngine() {
    }

    @SneakyThrows
    private InferenceEngine(Model model, HardwareConfig hardwareConfig) {
        this.model = model;
        this.hardwareConfig = hardwareConfig;
    }

    public static InferenceEngine getInstance(Model model) {
        return getInstance(model, HardwareConfig.getOnnxConfig());
    }

    public static InferenceEngine getInstance(Model model, HardwareConfig hardwareConfig) {
        if (inferenceEngine == null) {
            inferenceEngine = new InferenceEngine(model, hardwareConfig);
        }
        return inferenceEngine;
    }

    // ==================== 原有接口 ====================

    /**
     * 通过文件路径识别（原有接口）
     */
    public OcrResult runOcr(String imagePath) {
        return runOcr(imagePath, ParamConfig.getDefaultConfig());
    }

    public OcrResult runOcr(String imagePath, ParamConfig config) {
        loadFileIfNeeded(model);
        initEngine(model, hardwareConfig);
        log.info("图片路径：{}， 参数配置：{}", imagePath, config);
        OcrResult result = detect(imagePath, config.getPadding(), config.getMaxSideLen(),
                config.getBoxScoreThresh(), config.getBoxThresh(),
                config.getUnClipRatio(), config.isDoAngle(), config.isMostAngle());
        logOcrResult(result);
        return result;
    }

    // ==================== 新增接口 #IDMUNO #IHMZ1Y ====================

    /**
     * 通过字节数组识别（新增）
     * @param imageBytes 图片字节数组
     */
    public OcrResult runOcr(byte[] imageBytes) {
        return runOcr(imageBytes, ParamConfig.getDefaultConfig());
    }

    /**
     * 通过字节数组识别（新增）
     * @param imageBytes 图片字节数组
     * @param config    识别参数配置
     */
    public OcrResult runOcr(byte[] imageBytes, ParamConfig config) {
        String tempPath = bytesToTempFile(imageBytes);
        try {
            return runOcr(tempPath, config);
        } finally {
            deleteTempFile(tempPath);
        }
    }

    /**
     * 通过 Base64 字符串识别（新增）
     * @param base64Image 图片 Base64 字符串（支持带/不带 data:image/xxx;base64, 前缀）
     */
    public OcrResult runOcrBase64(String base64Image) {
        return runOcrBase64(base64Image, ParamConfig.getDefaultConfig());
    }

    /**
     * 通过 Base64 字符串识别（新增）
     * @param base64Image 图片 Base64 字符串（支持带/不带 data:image/xxx;base64, 前缀）
     * @param config     识别参数配置
     */
    public OcrResult runOcrBase64(String base64Image, ParamConfig config) {
        // 去除 Base64 前缀（如 data:image/jpeg;base64,）
        if (base64Image.contains(",")) {
            base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
        }
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        return runOcr(imageBytes, config);
    }

    /**
     * 通过 URL 识别（新增）
     * @param imageUrl 图片 URL，支持 http/https
     */
    public OcrResult runOcr(URL imageUrl) {
        return runOcr(imageUrl, ParamConfig.getDefaultConfig());
    }

    /**
     * 通过 URL 识别（新增）
     * @param imageUrl 图片 URL，支持 http/https
     * @param config   识别参数配置
     */
    public OcrResult runOcr(URL imageUrl, ParamConfig config) {
        String tempPath = downloadToTempFile(imageUrl);
        try {
            return runOcr(tempPath, config);
        } finally {
            deleteTempFile(tempPath);
        }
    }

    /**
     * 通过 InputStream 识别（新增）
     * @param inputStream 图片输入流
     */
    public OcrResult runOcr(InputStream inputStream) {
        return runOcr(inputStream, ParamConfig.getDefaultConfig());
    }

    /**
     * 通过 InputStream 识别（新增）
     * @param inputStream 图片输入流
     * @param config     识别参数配置
     */
    public OcrResult runOcr(InputStream inputStream, ParamConfig config) {
        String tempPath = inputStreamToTempFile(inputStream);
        try {
            return runOcr(tempPath, config);
        } finally {
            deleteTempFile(tempPath);
        }
    }

    // ==================== BufferedImage 支持（Issue #IHMZ1Y #IDMUNO） ====================

    /**
     * 通过 BufferedImage 识别（新增）
     * @param bufferedImage Java BufferedImage 对象
     */
    public OcrResult runOcr(BufferedImage bufferedImage) {
        return runOcr(bufferedImage, ParamConfig.getDefaultConfig());
    }

    /**
     * 通过 BufferedImage 识别（新增）
     * @param bufferedImage Java BufferedImage 对象
     * @param config       识别参数配置
     */
    public OcrResult runOcr(BufferedImage bufferedImage, ParamConfig config) {
        String tempPath = bufferedImageToTempFile(bufferedImage);
        try {
            return runOcr(tempPath, config);
        } finally {
            deleteTempFile(tempPath);
        }
    }

    /**
     * BufferedImage 写入临时文件
     */
    private String bufferedImageToTempFile(BufferedImage bufferedImage) {
        try {
            Path tempDir = Files.createTempDirectory("rapidocr");
            String fileName = UUID.randomUUID().toString() + ".png";
            Path tempFile = tempDir.resolve(fileName);
            ImageIO.write(bufferedImage, "png", tempFile.toFile());
            return tempFile.toString();
        } catch (IOException e) {
            throw new RuntimeException("BufferedImage 写入临时文件失败", e);
        }
    }

    // ==================== 私有工具方法 ====================

    /**
     * 字节数组写入临时文件
     */
    private String bytesToTempFile(byte[] imageBytes) {
        try {
            Path tempDir = Files.createTempDirectory("rapidocr");
            String fileName = UUID.randomUUID().toString() + ".jpg";
            Path tempFile = tempDir.resolve(fileName);
            Files.write(tempFile, imageBytes);
            return tempFile.toString();
        } catch (IOException e) {
            throw new RuntimeException("写入临时文件失败", e);
        }
    }

    /**
     * URL 下载到临时文件
     */
    private String downloadToTempFile(URL imageUrl) {
        try (InputStream in = imageUrl.openStream()) {
            Path tempDir = Files.createTempDirectory("rapidocr");
            String fileName = UUID.randomUUID().toString() + ".jpg";
            Path tempFile = tempDir.resolve(fileName);
            Files.copy(in, tempFile);
            return tempFile.toString();
        } catch (IOException e) {
            throw new RuntimeException("下载图片失败: " + imageUrl, e);
        }
    }

    /**
     * InputStream 写入临时文件
     */
    private String inputStreamToTempFile(InputStream inputStream) {
        try {
            Path tempDir = Files.createTempDirectory("rapidocr");
            String fileName = UUID.randomUUID().toString() + ".jpg";
            Path tempFile = tempDir.resolve(fileName);
            Files.copy(inputStream, tempFile);
            return tempFile.toString();
        } catch (IOException e) {
            throw new RuntimeException("写入临时文件失败", e);
        }
    }

    /**
     * 删除临时文件
     */
    private void deleteTempFile(String path) {
        if (path != null && !path.isEmpty()) {
            try {
                Files.deleteIfExists(java.nio.file.Paths.get(path));
            } catch (IOException e) {
                log.warn("删除临时文件失败: {}", path, e);
            }
        }
    }

    /**
     * 统一日志输出（降低日志级别，避免生产环境日志爆炸）
     */
    private void logOcrResult(OcrResult result) {
        if (log.isInfoEnabled()) {
            String text = result.getStrRes();
            if (text != null && text.length() > 100) {
                text = text.substring(0, 100) + "...";
            }
            log.info("识别结果：{}，耗时{}ms，文本块数：{}",
                    text, result.getDetectTime(),
                    result.getTextBlocks() != null ? result.getTextBlocks().size() : 0);
        }
        log.debug("文本块详情：{}，DbNet耗时{}ms",
                result.getTextBlocks(), result.getDbNetTime());
    }

    // ==================== 引擎生命周期管理 ====================

    @SneakyThrows
    private static void loadFileIfNeeded(Model model) {
        String modelType = model.getModelType();
        if (InferenceEngine.nativeLoader == null && (isLibraryLoaded.compareAndSet(false, true))) {
            synchronized (InferenceEngine.class) {
                if (InferenceEngine.nativeLoader == null) {
                    LibraryLoader nativeLoader = LoadUtil.findLibLoader(modelType);
                    if (nativeLoader == null) {
                        throw new LoadException("找不到合适的本机加载程序实现，可能的原因：1.运行库可能暂时未适配您的机型! " +
                                "2.使用的模型与引入的jar包不匹配，当前使用的模型为：" + modelType + "，请检查您引入的jar依赖是否正确! " +
                                "3.打包时未正确引入运行库，例如打包的是window依赖却在linux下运行!");
                    }
                    log.debug("当前库加载器: {}", nativeLoader.getClass().getSimpleName());
                    nativeLoader.loadLibrary();
                    isLibraryLoaded.set(true);
                    InferenceEngine.nativeLoader = nativeLoader;
                }
            }
        }
        if (InferenceEngine.modelsLoader == null) {
            synchronized (InferenceEngine.class) {
                if (InferenceEngine.modelsLoader == null) {
                    ModelsLoader modelsLoader = LoadUtil.findModelsLoader(modelType);
                    if (modelsLoader == null) {
                        throw new LoadException("未能成功加载模型!");
                    }
                    log.debug("当前模型加载器: {}", modelsLoader.getClass().getSimpleName());
                    modelsLoader.loadModels(model);
                    InferenceEngine.modelsLoader = modelsLoader;
                }
            }
        }
    }

    /**
     * 销毁引擎，释放 native 资源（新增优雅关闭）
     */
    public static synchronized void destroy() {
        if (nativeLoader != null) {
            nativeLoader = null;
            log.info("Native 库加载器已清理");
        }
        if (modelsLoader != null) {
            modelsLoader = null;
            log.info("模型加载器已清理");
        }
        if (inferenceEngine != null) {
            inferenceEngine = null;
            log.info("推理引擎实例已清理");
        }
        isLibraryLoaded.set(false);
    }
}
