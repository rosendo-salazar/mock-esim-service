package com.flyroamy.mock.controller;

import com.flyroamy.mock.model.MockEsim;
import com.flyroamy.mock.service.EsimService;
import com.flyroamy.mock.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qr")
@Tag(name = "QR Codes", description = "QR code generation endpoints (public)")
public class QrCodeController {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeController.class);

    private final QrCodeService qrCodeService;
    private final EsimService esimService;

    public QrCodeController(QrCodeService qrCodeService, EsimService esimService) {
        this.qrCodeService = qrCodeService;
        this.esimService = esimService;
    }

    @GetMapping(value = "/{esimId}", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Get QR code image", description = "Get the QR code image for an eSIM (public endpoint)")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String esimId) {
        logger.debug("Generating QR code for eSIM: {}", esimId);

        MockEsim esim = esimService.getEsimById(esimId);
        byte[] qrCode = qrCodeService.generateQrCodeBytes(esim.getQrCodeData());

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(qrCode);
    }

    @GetMapping(value = "/{esimId}/base64")
    @Operation(summary = "Get QR code as Base64", description = "Get the QR code as a Base64 encoded string")
    public ResponseEntity<String> getQrCodeBase64(@PathVariable String esimId) {
        logger.debug("Generating Base64 QR code for eSIM: {}", esimId);

        MockEsim esim = esimService.getEsimById(esimId);
        String base64 = qrCodeService.generateQrCodeBase64(esim.getQrCodeData());

        return ResponseEntity.ok(base64);
    }
}
