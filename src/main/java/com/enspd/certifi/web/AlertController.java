package com.enspd.certifi.web;

import com.enspd.certifi.dto.response.AlertDto;
import com.enspd.certifi.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public List<AlertDto> list() {
        return alertService.listAll();
    }

    @PostMapping("/{id}/acknowledge")
    public void acknowledge(@PathVariable UUID id) {
        alertService.acknowledge(id);
    }
}
