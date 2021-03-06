/*
 * Copyright 2017, Oath Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.datastores.hibernate.hql;

import static org.mockito.Mockito.mock;

import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.filter.FilterPredicate;
import com.yahoo.elide.core.filter.Operator;
import com.yahoo.elide.core.filter.expression.OrFilterExpression;
import com.yahoo.elide.core.hibernate.hql.RootCollectionPageTotalsQueryBuilder;
import com.yahoo.elide.core.pagination.Pagination;
import com.yahoo.elide.core.sort.Sorting;
import example.Author;
import example.Book;
import example.Chapter;
import example.Publisher;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class RootCollectionPageTotalsQueryBuilderTest {
    private EntityDictionary dictionary;

    private static final String TITLE = "title";
    private static final String BOOKS = "books";
    private static final String PUBLISHER = "publisher";

    private final Class<? extends Book> bookProxyClass = new Book() {
    }.getClass();

    @BeforeClass
    public void initialize() {
        dictionary = new EntityDictionary(new HashMap<>());
        dictionary.bindEntity(Book.class);
        dictionary.bindEntity(Author.class);
        dictionary.bindEntity(Publisher.class);
        dictionary.bindEntity(Chapter.class);
    }

    @Test
    public void testRootFetch() {
        RootCollectionPageTotalsQueryBuilder builder = new RootCollectionPageTotalsQueryBuilder(
                bookProxyClass, dictionary, new TestSessionWrapper());

        TestQueryWrapper query = (TestQueryWrapper) builder.build();

        String expected =
            "SELECT COUNT(DISTINCT example_Book)  "
            + "FROM example.Book AS example_Book  ";

        String actual = query.getQueryText();

        Assert.assertEquals(actual, expected);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRootFetchWithSorting() {
        RootCollectionPageTotalsQueryBuilder builder = new RootCollectionPageTotalsQueryBuilder(
                bookProxyClass, dictionary, new TestSessionWrapper());

        Sorting sorting = mock(Sorting.class);

        builder.withPossibleSorting(Optional.of(sorting)).build();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRootFetchWithPagination() {
        Pagination pagination = mock(Pagination.class);

        RootCollectionPageTotalsQueryBuilder builder = new RootCollectionPageTotalsQueryBuilder(
                bookProxyClass, dictionary, new TestSessionWrapper());

        builder.withPossiblePagination(Optional.of(pagination));
    }

    @Test
    public void testRootFetchWithJoinFilter() {
        List<FilterPredicate.PathElement> chapterTitlePath = Arrays.asList(
                new FilterPredicate.PathElement(Author.class, Book.class, BOOKS),
                new FilterPredicate.PathElement(Book.class, Chapter.class, "chapters"),
                new FilterPredicate.PathElement(Chapter.class, String.class, TITLE)
        );

        FilterPredicate titlePredicate = new FilterPredicate(
                chapterTitlePath,
                Operator.IN, Arrays.asList("ABC", "DEF"));

        List<FilterPredicate.PathElement>  publisherNamePath = Arrays.asList(
                new FilterPredicate.PathElement(Author.class, Book.class, BOOKS),
                new FilterPredicate.PathElement(Book.class, Publisher.class, PUBLISHER),
                new FilterPredicate.PathElement(Publisher.class, String.class, "name")
        );

        FilterPredicate publisherNamePredicate = new FilterPredicate(
                publisherNamePath,
                Operator.IN, Arrays.asList("Pub1"));

        OrFilterExpression expression = new OrFilterExpression(titlePredicate, publisherNamePredicate);

        RootCollectionPageTotalsQueryBuilder builder = new RootCollectionPageTotalsQueryBuilder(
                Author.class, dictionary, new TestSessionWrapper());

        TestQueryWrapper query = (TestQueryWrapper) builder
                .withPossibleFilterExpression(Optional.of(expression))
                .build();

        String expected =
                "SELECT COUNT(DISTINCT example_Author)  FROM example.Author AS example_Author  "
                + "LEFT JOIN example_Author.books example_Author_books  "
                + "LEFT JOIN example_Author_books.chapters example_Book_chapters   "
                + "LEFT JOIN example_Author_books.publisher example_Book_publisher  "
                + "WHERE (example_Book_chapters.title IN (:books_chapters_title_XXX) "
                + "OR example_Book_publisher.name IN (:books_publisher_name_XXX))";

        String actual = query.getQueryText();
        actual = actual.replaceFirst(":books_chapters_title_\\w+", ":books_chapters_title_XXX");
        actual = actual.replaceFirst(":books_publisher_name_\\w+", ":books_publisher_name_XXX");

        Assert.assertEquals(actual, expected);
    }
}
