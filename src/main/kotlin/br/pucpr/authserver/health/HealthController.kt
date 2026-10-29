package br.pucpr.authserver.health

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
class HealthController {

    @GetMapping
    fun ping(): ResponseEntity<Map<String, String>> =
        ResponseEntity.ok(mapOf("status" to "ok"))
}
