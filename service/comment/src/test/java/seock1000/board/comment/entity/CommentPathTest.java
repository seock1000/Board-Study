package seock1000.board.comment.entity;

import org.antlr.v4.runtime.misc.Triple;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.util.Pair;

import static org.junit.jupiter.api.Assertions.*;

class CommentPathTest {

    @ParameterizedTest
    @MethodSource("pathDescendantsExpected")
    void createChildCommentTest(
            Triple<String, String, String> pathDescendantsExpected
    ) {
        // given
        String path = pathDescendantsExpected.a;
        String descendants = pathDescendantsExpected.b;
        String expected = pathDescendantsExpected.c;

        // when
        String actual = CommentPath.create(path)
                .createChildCommentPath(descendants)
                .getPath();

        // then
        assertEquals(expected, actual);
    }

    static Triple<String, String, String>[] pathDescendantsExpected() {
        return new Triple[]{
                new Triple("", null, "00000"),
                new Triple("00000", null, "0000000000"),
                new Triple("", "00001", "00002"),
                new Triple("0000z", "0000zabcdzzzzzz", "0000zabce0"),
        };
    }

    @Test
    @DisplayName("깊이 5를 초과하는 댓글 경로 생성 시 예외 발생")
    void createChildCommentPath_maxDepth() {
        // given
        String path = "00000".repeat(5); // depth 5

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            CommentPath.create(path)
                    .createChildCommentPath(null);
        });
    }

    @Test
    @DisplayName("chunk overflow일 때 예외 발생")
    void increase_overflow() {
        // given
        CommentPath path = CommentPath.create("");

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            path.createChildCommentPath("zzzzz");
        });
    }

}