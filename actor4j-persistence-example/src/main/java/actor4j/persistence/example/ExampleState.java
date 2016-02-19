/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.persistence.example;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.eclipse.persistence.nosql.annotations.NoSql;

import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;

@Entity
@NoSql(dataFormat=DataFormatType.MAPPED)
public class ExampleState implements Serializable {
	protected static final long serialVersionUID = -4370022948852154439L;
	
	@Id
	@GeneratedValue
	@Field(name="_id")
	public String _id;
	@Basic
	public String id;
	@Basic
	public String name;
	@Basic
	public Date date;
	@Basic
	public int tag;
	
	public ExampleState() {
		super();
	}
	
	public ExampleState(String id, String name, Date date, int tag) {
		super();
		this.id = id;
		this.name = name;
		this.date = date;
		this.tag = tag;
	}
}
