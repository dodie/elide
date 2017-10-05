/*
 * Copyright 2016, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package example;

import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Include(rootLevel = true)
@ReadPermission(expression = "allow all")
@CreatePermission(expression = "allow all")
@UpdatePermission(expression = "allow all")
@Entity
public class ResourceWithInvalidRelationship {
    private Long id;

    private String name;

    private NotIncludedResource notIncludedResource;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToOne
    public NotIncludedResource getNotIncludedResource() {
        return notIncludedResource;
    }

    public void setNotIncludedResource(NotIncludedResource notIncludedResource) {
        this.notIncludedResource = notIncludedResource;
    }
}