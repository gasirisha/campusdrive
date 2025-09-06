package com.campusdrive.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"college_id", "student_reg_no"})
})
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name = "college_id")
    private College college;

    @Column(name = "student_reg_no", nullable=false)
    private String studentRegNo;

    @Column(nullable=false)
    private String name;

    private String email;

    public Student() {}

    // getters & setters
    public Long getId() { return id; }
    public College getCollege() { return college; }
    public void setCollege(College college) { this.college = college; }
    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
