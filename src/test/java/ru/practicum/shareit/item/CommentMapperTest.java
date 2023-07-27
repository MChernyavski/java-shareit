package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommentMapperTest {

    private Comment comment;
    private CommentDto commentDto;
    private User user;
    private Item item;

    @BeforeEach
    public void setUp() {
        user = new User(1L, "Katya", "katya@user.com");
        item = new Item(1L, "itemNameOne", "itemDescriptionOne", true, user, null);
        comment = new Comment(1L, "text", item, user, LocalDateTime.now());
        commentDto = new CommentDto(1L, "text", user.getName(), LocalDateTime.now(), item.getId());
    }

    @Test
    public void toCommentDtoTest() {
        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertEquals(dto.getId(), comment.getId());
        assertEquals(dto.getText(), comment.getText());
        assertEquals(dto.getItemId(), comment.getItem().getId());
        assertEquals(dto.getAuthorName(), comment.getAuthor().getName());
        assertEquals(dto.getCreated(), comment.getCreated());
    }

    @Test
    public void toCommentTest() {
        Comment comment1 = CommentMapper.toComment(commentDto);

        assertEquals(comment1.getId(), commentDto.getId());
        assertEquals(comment1.getText(), commentDto.getText());
    }
}
