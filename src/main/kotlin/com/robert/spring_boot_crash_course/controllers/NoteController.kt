package com.robert.spring_boot_crash_course.controllers

import com.robert.spring_boot_crash_course.controllers.NoteController.NoteResponse
import com.robert.spring_boot_crash_course.database.model.Note
import com.robert.spring_boot_crash_course.database.repository.NoteRepository
import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.Instant

// POST http://localhost:8085/notes
// GET http://localhost:8085/notes?ownerId=21234
// DELETE http://localhost:8085/notes/123

@RestController
@RequestMapping("/notes")
class NoteController(
    private val repository: NoteRepository,
    private val noteRepository: NoteRepository
) {

    data class NoteRequest(
        val id: String?,
        @field:NotBlank(message = "Title must not be blank.")
        val title: String,
        val content: String,
        val color: Long
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val content: String,
        val color: Long,
        val createdAt: Instant,
        val ownerId: ObjectId
    )

    @PostMapping
    fun save(
        @RequestBody body: NoteRequest
    ): NoteResponse {
        val ownerId = ObjectId(SecurityContextHolder.getContext().authentication.principal as String)
        val note = repository.save(
            Note(
                id = body.id?.let { ObjectId(it) } ?: ObjectId.get(),
                title = body.title,
                content = body.content,
                color = body.color,
                createdAt = Instant.now(),
                ownerId = ownerId
            )
        )

        return note.toResponse()
    }

    @GetMapping
    fun findByOwnerId(): List<NoteResponse> {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        return repository.findByOwnerId(ObjectId(ownerId)).map {
            it.toResponse()
        }
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteById(@PathVariable id: String) {
        val note = noteRepository.findById(ObjectId(id)).orElseThrow {
            IllegalArgumentException("Note not found.")
        }
        if (note.ownerId.toHexString() != SecurityContextHolder.getContext().authentication.principal) {
            repository.deleteById(ObjectId(id))
        }
    }
}

private fun Note.toResponse(): NoteResponse {
    return NoteResponse(
        id = id.toHexString(),
        title = title,
        content = content,
        color = color,
        createdAt = createdAt,
        ownerId = ObjectId()
    )
}