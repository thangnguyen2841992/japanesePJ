package com.japanese.project.service.sentence;

import jakarta.annotation.PostConstruct;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.*;

@Service
public class SentenceServiceImpl implements ISentenceService{

    @Value("${tesseract.datapath:C:/Program Files/Tesseract-OCR}")
    private String tessDataPath;

    @Value("${tesseract.language:jpn+vie+eng}")
    private String language;

    // Timeout cho mỗi tác vụ OCR (tùy chỉnh)
    @Value("${tesseract.ocr.timeout.seconds:30}")
    private long ocrTimeoutSeconds;

    // ThreadLocal Tesseract instance: mỗi thread có 1 instance riêng (Tránh tạo mới cho mỗi request)
    private ThreadLocal<ITesseract> tessInstance;

    // Executor để chạy OCR có timeout (tách thread để tránh block main thread lâu)
    private final ExecutorService ocrExecutor = Executors.newCachedThreadPool();

    @PostConstruct
    private void init() {
        tessInstance = ThreadLocal.withInitial(() -> {
            Tesseract t = new Tesseract();
            // setDatapath tới thư mục cha chứa tessdata folder (thử điều chỉnh nếu cần)
            t.setDatapath(tessDataPath);
            t.setLanguage(language);

            // Tối ưu mặc định:
            // Gán DPI giả định để Tesseract xử lý đúng kích thước ký tự
            try {
            // setTessVariable có thể ném lỗi nếu không hỗ trợ; bọc try-catch an toàn
                t.setVariable("user_defined_dpi", "300");
                // Bật preserve interword spaces nếu muốn giữ khoảng cách
                t.setVariable("preserve_interword_spaces", "1");
            } catch (Exception ignored) { }

            return t;
        });
    }

    @Override
    public String testSentence(String imageUrl) {
        // Đọc ảnh từ URL
        BufferedImage img;
        try {
            URL url = new URL(imageUrl);
            img = ImageIO.read(url);
            if (img == null) {
                throw new IOException("Không thể đọc ảnh từ URL");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Tiền xử lý: chỉ làm những gì cần thiết để cải thiện OCR và giảm kích thước dư thừa
        BufferedImage processed = preprocessForOCR(img);

        // Thực hiện OCR trong thread riêng với timeout
        Callable<String> ocrTask = () -> {
            ITesseract t = tessInstance.get();
            return t.doOCR(processed);
        };

        Future<String> future = ocrExecutor.submit(ocrTask);
        try {
            return future.get(ocrTimeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            future.cancel(true);
            throw new RuntimeException("OCR timed out after " + ocrTimeoutSeconds + " seconds", te);
        } catch (ExecutionException ee) {
            throw new RuntimeException(ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("OCR interrupted", ie);
        }
    }

    private BufferedImage preprocessForOCR(BufferedImage src) {
        // 1) Fix alpha channel: convert to RGB if image has alpha to avoid issues
        BufferedImage img = src;
        if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR || img.getColorModel().hasAlpha()) {
            BufferedImage rgb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.setColor(Color.WHITE); // fill background white
            g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            g.drawImage(img, 0, 0, null);
            g.dispose();
            img = rgb;
        }

        // 2) Nếu DPI gốc rất thấp (ví dụ Tesseract báo 1), scale lên một mức hợp lý.
        // Thay vì dựa vào metadata (phức tạp), ta dùng heuristics: nếu chiều nhỏ (<1000) thì scale 2x/3x.
        int w = img.getWidth();
        int h = img.getHeight();
        int scale = 1;
        if (Math.max(w, h) < 800) scale = 3;        // small images -> scale more
        else if (Math.max(w, h) < 1600) scale = 2;  // medium images -> moderate scale

        BufferedImage scaled = img;
        if (scale > 1) {
            int tw = w * scale;
            int th = h * scale;
            BufferedImage tmp = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(img, 0, 0, tw, th, null);
            g2.dispose();
            scaled = tmp;
        }

        // 3) Convert to grayscale (TYPE_BYTE_GRAY) — nhẹ và thường đủ cho OCR
        BufferedImage gray = new BufferedImage(scaled.getWidth(), scaled.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g3 = gray.createGraphics();
        g3.drawImage(scaled, 0, 0, null);
        g3.dispose();

        // 4) Optional: adaptive thresholding or simple Otsu — thêm nếu chữ mờ/nền lòe
        // Bạn có thể thử binaryThreshold(gray) nếu cần, nhưng threshold nhị phân đôi khi làm mất dấu nhấn trong chữ Nhật.
        return gray;
    }

    // Example binary threshold (nếu cần thử nghiệm)
    @SuppressWarnings("unused")
    private BufferedImage binaryThreshold(BufferedImage gray, int threshold) {
        int w = gray.getWidth(), h = gray.getHeight();
        BufferedImage bin = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = gray.getRGB(x, y) & 0xFF;
                int v = (rgb < threshold) ? 0x00 : 0xFFFFFF;
                bin.setRGB(x, y, v);
            }
        }
        return bin;
    }
}
