package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodoSummaries(
            String keyword,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String managerNickname,
            Pageable pageable
    ) {
        List<TodoSearchResponse> content = queryFactory
                .select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,
                        manager.id.countDistinct(),
                        comment.id.countDistinct()
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(todo.comments, comment)
                .where(searchConditions(keyword, startDate, endDate, managerNickname))
                .groupBy(todo.id, todo.title, todo.createdAt)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(todo.id.countDistinct())
                .from(todo)
                .where(searchConditions(keyword, startDate, endDate, managerNickname))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder searchConditions(
            String keyword,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String managerNickname
    ) {
        return new BooleanBuilder()
                .and(titleContains(keyword))
                .and(createdAtGoe(startDate))
                .and(createdAtLoe(endDate))
                .and(managerNicknameContains(managerNickname));
    }

    private BooleanExpression titleContains(String keyword) {
        return StringUtils.hasText(keyword) ? todo.title.contains(keyword) : null;
    }

    private BooleanExpression createdAtGoe(LocalDateTime startDate) {
        return startDate != null ? todo.createdAt.goe(startDate) : null;
    }

    private BooleanExpression createdAtLoe(LocalDateTime endDate) {
        return endDate != null ? todo.createdAt.loe(endDate) : null;
    }

    private BooleanExpression managerNicknameContains(String managerNickname) {
        if (!StringUtils.hasText(managerNickname)) {
            return null;
        }

        QManager managerSearch = new QManager("managerSearch");

        return com.querydsl.jpa.JPAExpressions
                .selectOne()
                .from(managerSearch)
                .where(
                        managerSearch.todo.eq(todo),
                        managerSearch.user.nickname.contains(managerNickname)
                )
                .exists();
    }
}
