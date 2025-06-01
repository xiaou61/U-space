package com.xiaou.pay.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.xiaou.pay.domain.PayRequestDTO;
import com.xiaou.pay.utils.SignUtils;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PaymentController {

    private static final String PAY_URL = "https://e.heiyu.cc/mapi.php";
    private static final String MERCHANT_KEY = "v9wPbx9Wx85CD7ckA48mZAG4zUfs19J5";

    @PostMapping("/request")
    public ResponseEntity<?> payRequest(@RequestBody PayRequestDTO dto) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("pid", "1436");  // 固定
            params.put("type", "alipay");  // 固定
            params.put("out_trade_no", dto.getOut_trade_no());
            params.put("notify_url", "http://localhost:8080/uapi/pay/notify");  // 固定
            params.put("return_url", "http://localhost:8080/uapi/pay/return");  // 固定
            params.put("name", dto.getName());
            params.put("money", dto.getMoney());
            params.put("clientip", dto.getClientip());
            params.put("sign_type", "MD5");  // 固定

            String sign = SignUtils.generateSign(params, MERCHANT_KEY);
            params.put("sign", sign);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            params.forEach(formData::add);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> resp = restTemplate.postForEntity(PAY_URL, new HttpEntity<>(formData, headers), Map.class);

            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                Map body = resp.getBody();
                if (body.get("code").equals(1) && body.containsKey("qrcode")) {
                    String qrUrl = (String) body.get("qrcode");

                    QRCodeWriter qrCodeWriter = new QRCodeWriter();
                    BitMatrix bitMatrix = qrCodeWriter.encode(qrUrl, BarcodeFormat.QR_CODE, 300, 300);

                    ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
                    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
                    byte[] pngData = pngOutputStream.toByteArray();

                    HttpHeaders responseHeaders = new HttpHeaders();
                    responseHeaders.setContentType(MediaType.IMAGE_PNG);
                    return ResponseEntity.ok().headers(responseHeaders).body(pngData);
                } else {
                    return ResponseEntity.badRequest().body(Map.of("error", "支付失败：" + body.get("msg")));
                }
            } else {
                return ResponseEntity.status(resp.getStatusCode())
                        .body(Map.of("error", "支付接口调用失败，状态码：" + resp.getStatusCode()));
            }
        } catch (WriterException e) {
            return ResponseEntity.status(500).body(Map.of("error", "二维码生成失败：" + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "请求支付接口异常：" + e.getMessage()));
        }
    }


    @GetMapping("/notify")
    public ResponseEntity<String> handleNotify(@RequestParam Map<String, String> params) {
        System.out.println("📥 收到支付异步通知参数: " + params);

        String receivedSign = params.get("sign");
        String tradeStatus = params.get("trade_status");

        if (receivedSign == null || tradeStatus == null) {
            return ResponseEntity.badRequest().body("参数缺失");
        }

        // 验签
        String calculatedSign = SignUtils.generateSign(params, MERCHANT_KEY);
        if (receivedSign.equals(calculatedSign) && "TRADE_SUCCESS".equals(tradeStatus)) {
            String orderNo = params.get("out_trade_no");
            String payAmount = params.get("money");

            // ✅ TODO：在这里处理你的业务逻辑，比如：

            System.out.println("✅ 支付成功！订单号：" + orderNo + "，金额：" + payAmount);

            // 必须返回 success 表示已接收成功
            return ResponseEntity.ok("success");
        } else {
            System.out.println("❌ 支付验证失败或交易未成功！");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("sign error");
        }
    }

}
