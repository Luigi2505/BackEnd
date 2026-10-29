package br.pucpr.authserver.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(message: String = "Recurso não encontrado") : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException(message: String = "Requisição inválida") : RuntimeException(message)

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException(message: String = "Conflito de dados") : RuntimeException(message)

