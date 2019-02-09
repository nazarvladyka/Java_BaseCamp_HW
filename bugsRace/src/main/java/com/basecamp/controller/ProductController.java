package com.basecamp.controller;

import com.basecamp.service.impl.ProductServiceImpl;
import com.basecamp.wire.GetHandleProductIdsResponse;
import com.basecamp.wire.GetProductInfoResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductServiceImpl productService;

    // as a case - url could be moved to separate class
    @GetMapping(value = "/product/{productId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GetProductInfoResponse> getProductInfo(
            @PathVariable String productId) {

        return ResponseEntity.ok()
                .body(productService.getProductInfo(productId));
    }

    @PutMapping(value = "/product/handle", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GetHandleProductIdsResponse> handleProductInfo(
            @RequestBody List<String> productIds) {

        return ResponseEntity.ok()
                .body(productService.handleProducts(productIds));
    }

    @GetMapping(value = "/product/stop", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GetProductInfoResponse> stopService() {

        productService.stopProductExecutor();

        return ResponseEntity
                .status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping(value = "/homework", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity homework() throws InterruptedException, ExecutionException {
        productService.bugsRace();
        return ResponseEntity.ok().build();
    }
}
