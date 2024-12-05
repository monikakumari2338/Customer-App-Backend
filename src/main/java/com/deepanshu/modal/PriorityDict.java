package com.deepanshu.modal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class PriorityDict {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String priorityType;
	private String priorityValue;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPriorityType() {
		return priorityType;
	}

	public void setPriorityType(String priorityType) {
		this.priorityType = priorityType;
	}

	public String getPriorityValue() {
		return priorityValue;
	}

	public void setPriorityValue(String priorityValue) {
		this.priorityValue = priorityValue;
	}

	public PriorityDict(Long id, String priorityType, String priorityValue) {
		super();
		this.id = id;
		this.priorityType = priorityType;
		this.priorityValue = priorityValue;
	}

	public PriorityDict() {
		super();
		// TODO Auto-generated constructor stub
	}

}
