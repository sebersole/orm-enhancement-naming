package org.hibernate.bytecode.enhance.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Steve Ebersole
 */
@Entity(name = "AnEntity")
@Table(name = "AnEntity")
public class SimpleFieldEntity {
	@Id
	private Integer id;
	private String name;
}
